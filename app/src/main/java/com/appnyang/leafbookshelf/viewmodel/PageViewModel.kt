package com.appnyang.leafbookshelf.viewmodel

import android.app.Application
import android.content.*
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.OpenableColumns
import android.text.*
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.core.text.toSpanned
import androidx.lifecycle.*
import com.appnyang.leafbookshelf.core.LeafApp
import com.appnyang.leafbookshelf.data.model.book.Book
import com.appnyang.leafbookshelf.data.model.book.BookWithBookmarks
import com.appnyang.leafbookshelf.data.model.bookmark.Bookmark
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkType
import com.appnyang.leafbookshelf.data.repository.BookRepository
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.service.TtsService
import com.appnyang.leafbookshelf.util.SharedPreferenceLiveData
import com.appnyang.leafbookshelf.util.icu.CharsetDetector
import com.appnyang.leafbookshelf.util.styler.DefaultStyler
import com.appnyang.leafbookshelf.view.page.PageAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.collect
import org.joda.time.DateTime
import org.joda.time.Interval
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Page View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class PageViewModel(
    private val bookRepo: BookRepository,
    private val bookmarkRepo: BookmarkRepository,
    val sharedPreferenceLiveData: SharedPreferenceLiveData,
    application: Application
) : AndroidViewModel(application) {

    private val _bookWithBookmarks = MutableLiveData<BookWithBookmarks>()
    val bookWithBookmarks: LiveData<BookWithBookmarks> = _bookWithBookmarks

    private val _pageTextAppearance = MutableLiveData<PageTextAppearance>()
    val pageTextAppearance: LiveData<PageTextAppearance> = _pageTextAppearance

    private val _pagedBook = MutableLiveData<LinkedList<Spanned>>()
    val pagedBook: LiveData<LinkedList<Spanned>> = _pagedBook

    val currentPage = MutableLiveData(CurrentPage(0))

    // Chips.
    val bHorizontal = MutableLiveData(true)
    val bTts = MutableLiveData(false)
    val bAuto = MutableLiveData(false)

    // Menu state.
    private val _menuState = MutableLiveData(MenuState.Default)
    val menuState: LiveData<MenuState> = _menuState

    private val _bookmarks = MediatorLiveData<List<Bookmark>>()
    val bookmarks: LiveData<List<Bookmark>> = _bookmarks

    val isPaginating = AtomicBoolean(false)

    // Page touch listener.
    val onTouchUpListener: (touchUpPosition: PageAdapter.TouchUpPosition) -> Unit = { touchUpPosition ->
        // Hide menus if any menu is displayed.
        if (menuState.value != MenuState.Default) {
            _menuState.value = MenuState.Default
        }
        else {
            when (touchUpPosition) {
                PageAdapter.TouchUpPosition.LEFT -> goToPage(currentPage.value!!.page - 1)
                PageAdapter.TouchUpPosition.MIDDLE -> _menuState.value = MenuState.TopBottom
                PageAdapter.TouchUpPosition.RIGHT -> goToPage(currentPage.value!!.page + 1)
            }
        }
    }

    // Job that manages auto-reading.
    private var autoReadJob: Job = Job()
    private val _autoReadTick = MutableLiveData(0f)
    val autoReadTick: LiveData<Float> = _autoReadTick

    // TTS Service.
    private lateinit var ttsService: TtsService
    private var isBound = false
    private val ttsServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            /*ttsService = (binder as TtsService.LocalBinder).getService()
            isBound = true

            setOnUserCancelReadListener()

            val title = ""
            pagedBook.value?.let { book ->
                ttsService.read(title, book, _currentPage)
            }*/
        }
        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            bTts.value = false
        }
    }

    /**
     * Update text appearances like text-size, type-face, color ...
     *
     * @param pageTextAppearance PageTextAppearance.
     */
    fun updatePageTextAppearance(pageTextAppearance: PageTextAppearance) {
        _pageTextAppearance.value = pageTextAppearance
    }

    /**
     * Read text file from uri.
     *
     * @param uri URI of the file to read.
     * @param contentResolver Android Content Resolver.
     * @param layoutParam Layout parameters to build StaticLayout.
     * @param charIndex The char position the user last read. If it is lower than 0, read from last-read position.
     */
    fun readBookFromUri(uri: Uri, contentResolver: ContentResolver, layoutParam: StaticLayoutParam, charIndex: Long = 0L) {
        // TODO: Add wrong uri handler.

        // True when BookWithBookmarks has been loaded.
        val isBookLoaded = AtomicBoolean(false)

        viewModelScope.launch(Dispatchers.Default) {
            // Read a Book with Bookmarks.
            bookRepo.getBookWithBookmarks(uri).collect {
                if (it == null) {
                    if (!isBookLoaded.get()) {
                        // Create a new Book.
                        val displayName = fetchBookNameFromUri(uri, contentResolver)
                        val book = Book(uri, displayName, Uri.parse("color:red"), "", 0, DateTime.now())
                        bookRepo.saveBook(book)
                    }
                    else {
                        // Book had been loaded and deleted.
                        _bookWithBookmarks.postValue(null)
                    }
                }
                else {
                    launch(Dispatchers.Main) {
                        _bookWithBookmarks.value = it
                    }

                    if (!isBookLoaded.getAndSet(true)) {
                        launch(Dispatchers.Main) {
                            setLastOpenedToNow()
                        }

                        launch {
                            val chunkedText = fetchBookFromUri(uri, contentResolver)
                            var loadIndex = charIndex
                            // The charIndex less than 0 means "Open recently read page".
                            if (charIndex < 0) {
                                loadIndex = it.bookmarks
                                    .firstOrNull { bookmark -> bookmark.type == BookmarkType.LAST_READ.name }?.index
                                    ?: 0
                            }

                            paginateBook(chunkedText, layoutParam, loadIndex)
                        }
                    }
                }
            }
        }
    }

    /**
     * Fetch the display name of the file and set it to openedFileName
     *
     * @param uri URI of the file to read.
     * @param contentResolver Android Content Resolver.
     * @return File name.
     */
    @WorkerThread
    private suspend fun fetchBookNameFromUri(uri: Uri, contentResolver: ContentResolver): String = withContext(Dispatchers.IO) {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null, null)
        var displayName = ""

        cursor?.use {
            if (it.moveToFirst()) {
                displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }

        displayName.reversed()
            .split(".", limit = 2).let {
                if (it.size >= 2) {
                    it[1]
                }
                else {
                    it[0]
                }
            }.reversed()
    }

    /**
     * Fetch from the file using coroutine.
     *
     * @param uri URI of the file to read.
     * @param contentResolver Android Content Resolver.
     *
     * @return A list of chunked text.
     */
    private suspend fun fetchBookFromUri(uri: Uri, contentResolver: ContentResolver): List<CharSequence> = withContext(Dispatchers.IO) {
        val builder = StringBuilder()

        val chunkedText = mutableListOf<CharSequence>()
        val chunkSize = 2048 // lines
        var chunkCount = 0
        contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, detectCharSet(uri, contentResolver))).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    builder.append(line + "\n")
                    if (chunkCount >= chunkSize) {
                        chunkedText.add(builder.toString())
                        builder.clear()
                        chunkCount = 0
                    }

                    line = reader.readLine()
                    chunkCount++
                }
                // Add rest chunk.
                if (builder.isNotEmpty()) {
                    chunkedText.add(builder.toString())
                }
            }
        }

        chunkedText
    }

    /**
     * Detect charset of the file.
     *
     * @param uri URI of the file to read.
     * @param contentResolver Android Content Resolver.
     *
     * @return The name of CharSet.
     */
    private suspend fun detectCharSet(uri: Uri, contentResolver: ContentResolver): String = withContext(Dispatchers.IO) {
        val byteArray = ByteArray(100)
        contentResolver.openInputStream(uri)?.use {
            it.read(byteArray, 0, 100)
        }

        if (CharsetDetector().setText(byteArray).detect() != null) {
            CharsetDetector().setText(byteArray).detect().name
        }
        else {
            Charset.defaultCharset().name()
        }
    }

    /**
     * Paginate text to fit the screen using chunkedText and layoutParam.
     * It should be processed AFTER the view is laid out,
     * because it needs the view's height to get list of pages.
     *
     * @param chunkedText A list of chunked raw text.
     * @param layoutParam Layout parameters to build StaticLayout.
     * @param charIndex The char position the user last read.
     */
    private suspend fun paginateBook(chunkedText: List<CharSequence>, layoutParam: StaticLayoutParam, charIndex: Long = 0L) = withContext(Dispatchers.Default) {
        // Paginating is a time-consuming work. So we need to notify the status using isPaginating.
        isPaginating.set(true)

        val (chunkStart, charIndexInChunk) = getChunkCharIndices(charIndex, chunkedText)

        val pagedCharSequence = mutableListOf<Spanned>()

        buildChunkSequence(chunkStart, chunkedText.size).forEach { chunkIndex ->
            // 0. Style the chunk.
            val spannedText = styleTheChunk(chunkedText[chunkIndex])

            // 1. Build a StaticLayout to measure the text.
            val layout = buildStaticLayout(layoutParam, spannedText)

            // 2. Split the text in the page.
            var beginOffset = 0
            var heightThreshold = layoutParam.height
            for (i in 0 until layout.lineCount) {
                // When the line has been exceeded single page,
                if (heightThreshold < layout.getLineBottom(i)) {
                    pagedCharSequence.add(spannedText.subSequence(beginOffset until layout.getLineStart(i)).toSpanned())
                    beginOffset = layout.getLineStart(i)
                    heightThreshold = layout.getLineTop(i) + layoutParam.height
                }
            }

            // Add rest of the sequence.
            if (beginOffset != layout.getLineEnd(layout.lineCount - 1)) {
                pagedCharSequence
                    .add(spannedText.subSequence(beginOffset until layout.getLineEnd(layout.lineCount - 1)).toSpanned())
            }

            // 3. Set paged data.
            if (chunkIndex == chunkStart) {
                // After first chunk had been processed, fire the alarm to show the contents.
                val list = LinkedList<Spanned>()
                list.addAll(pagedCharSequence.toList())

                _pagedBook.postValue(list)

                // If the user load the book with bookmark, go to the bookmark.
                if (charIndexInChunk != 0L) {
                    postCurrentPageToIndex(list, charIndexInChunk)
                }
            }
            else {
                if (chunkIndex > chunkStart) {
                    _pagedBook.value?.addAll(pagedCharSequence.toList())
                    _pagedBook.notify()
                } else {
                    pagedCharSequence.reversed().asSequence().forEach { _pagedBook.value?.addFirst(it) }
                    _pagedBook.notify()
                    currentPage.postValue(CurrentPage(currentPage.value!!.page.plus(pagedCharSequence.size), false))
                }
            }

            pagedCharSequence.clear()
        }
        isPaginating.set(false)
    }

    /**
     * Style with styler.
     *
     * @param text CharSequence of a chunk.
     */
    private suspend fun styleTheChunk(text: CharSequence) = withContext(Dispatchers.Default) {
        val spannableText = SpannableString(text)
        val styler = DefaultStyler()

        styler.listRegex.forEachIndexed { index, regex ->
            regex
                .findAll(spannableText)
                .forEach {
                    styler.listSpans[index](it, spannableText)
                }
        }

        spannableText
    }

    /**
     * Find the index of the first chunk to be processed
     * using the charIndex where the user last read.
     *
     * @param charIndex The char position the user last read.
     * @param chunkedText A list of chunks.
     * @return The index of the chunk to be processed and charIndex in the chunk.
     */
    private suspend fun getChunkCharIndices(charIndex: Long, chunkedText: List<CharSequence>): Pair<Int, Long> = withContext(Dispatchers.Default) {
        var sumChars = 0L
        var chunkStart = 0
        var charIndexInChunk = 0L
        for (i in chunkedText.indices) {
            sumChars += chunkedText[i].length
            if (sumChars >= charIndex) {
                chunkStart = i
                charIndexInChunk = charIndex - sumChars + chunkedText[i].length
                break
            }
        }

        Pair(chunkStart, charIndexInChunk)
    }

    /**
     * Returns the order in which the chunks will be processed.
     * If chunkStart is 3 and chunkLength is 5, the sequence will be
     * [3, 4, 2, 1, 0]
     *
     * @param chunkStart Starting index of the chunk.
     * @param chunkLength Total size of the list of chunks.
     * @return The sequence that the chunks will be processed.
     */
    private suspend fun buildChunkSequence(chunkStart: Int, chunkLength: Int): Sequence<Int> = withContext(Dispatchers.Default) {
        val chunkSequence = if (chunkStart > 0) {
            (chunkStart until chunkLength).asSequence() + ((chunkStart - 1) downTo 0).asSequence()
        } else {
            (chunkStart until chunkLength).asSequence()
        }

        chunkSequence
    }

    /**
     * Build a StaticLayout.
     *
     * @param layoutParam Layout parameter from the Activity.
     * @param chunk Chunked text.
     * @return StaticLayout
     */
    private suspend fun buildStaticLayout(layoutParam: StaticLayoutParam, chunk: CharSequence): StaticLayout = withContext(Dispatchers.Default) {
        val layout: StaticLayout = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            StaticLayout(chunk, layoutParam.paint, layoutParam.width, Layout.Alignment.ALIGN_NORMAL, layoutParam.spacingMult, layoutParam.spacingExtra, layoutParam.includePad)
        } else {
            StaticLayout.Builder
                .obtain(chunk, 0, chunk.length, layoutParam.paint, layoutParam.width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(layoutParam.spacingExtra, layoutParam.spacingMult)
                .setIncludePad(layoutParam.includePad)
                .build()
        }

        layout
    }

    /**
     * Move to the corresponding page with given character position.
     *
     * @param list A first list of paginated text.
     * @param index Character position.
     */
    private fun postCurrentPageToIndex(list: List<CharSequence>, index: Long) {
        var page = list.size - 1
        var sum = 0

        list.asSequence()
            .filter { sum - 1 < index }
            .forEachIndexed { i, text ->
                page = i
                sum += text.length
            }

        currentPage.postValue(CurrentPage(page, false))
    }

    /**
     * Move page to the given page.
     * Called when the user click the left or right side of the page.
     *
     * @param page
     * @return true if the book has next page.
     */
    @MainThread
    fun goToPage(page: Int): Boolean {
        var hasNextPage = false
        if (page in _pagedBook.value!!.indices && page != currentPage.value?.page) {
            currentPage.value = CurrentPage(page)
            hasNextPage = true
        }

        return hasNextPage
    }

    /**
     * Set lastOpenedAt to now. This function called when open the file or resume reading.
     */
    @MainThread
    fun setLastOpenedToNow() {
        bookWithBookmarks.value?.let {
            it.book.lastOpenedAt = DateTime.now()
        }
    }

    /**
     * Set menuState to Default to close all menu.
     */
    fun closeAllMenu() {
        _menuState.value = MenuState.Default
    }

    /**
     * Change menu state to given value.
     *
     * @param menuState Menu state to set.
     */
    fun changeMenuState(menuState: MenuState) {
        _menuState.value = menuState
    }

    /**
     * Bind TTS service.
     *
     * @param bStart True when TTS chip is checked.
     */
    fun startTtsService(bStart: Boolean) {
        if (bStart && !isBound) {
            Intent(getApplication<LeafApp>(), TtsService::class.java).also {
                getApplication<LeafApp>().bindService(it, ttsServiceConnection, Context.BIND_AUTO_CREATE)
            }
        }
        else if (!bStart && isBound) {
            isBound = false
            getApplication<LeafApp>().unbindService(ttsServiceConnection)
        }
    }

    /**
     * Unbind the TtsService and uncheck TTS chip.
     * This is called when the user stop service on the notification.
     */
    private fun setOnUserCancelReadListener() {
        ttsService.userCancelReadListener = {
            isBound = false
            getApplication<LeafApp>().unbindService(ttsServiceConnection)
            bTts.value = false
        }
    }

    /**
     * Get current character position of pagedBook.
     *
     * @return The current text index of current page.
     */
    fun getCurrentTextIndex(): Long = pagedBook.value?.run {
        asSequence()
            .filterIndexed { index, _ -> index < (currentPage.value!!.page ?: 0) }
            .sumBy { it.length }
            .toLong()
    } ?: 0L

    /**
     * Move to the corresponding page with given character position.
     *
     * @param index Character position.
     */
    fun setCurrentPageToTextIndex(index: Long) {
        pagedBook.value?.let {
            var page = it.size - 1
            var sum = 0

            for (i in it.indices) {
                if (sum >= index) {
                    page = i
                    break
                }
                sum += it[i].length
            }

            // After for-loop, variable 'sum' has number of all characters in pagedBook.
            // If sum is small to index, it may mean that the chunks have not been processed yet.
            if (sum < index) {
                // Show loading screen or sth.
            }

            currentPage.postValue(CurrentPage(page))
        }
    }

    /**
     * Bookmark the current page.
     *
     * @param displayName Title of the bookmark.
     * @param type BookmarkType.
     */
    fun bookmarkCurrentPage(displayName: String, type: BookmarkType = BookmarkType.CUSTOM) {
        bookWithBookmarks.value?.let { bookWithBookmarks ->
            saveBookmark(Bookmark(bookWithBookmarks.book.bookId, displayName, getCurrentTextIndex(), type.name, DateTime.now()))
        }
    }

    /**
     * Save the given bookmark to the database.
     *
     * @param bookmark A bookmark.
     */
    private fun saveBookmark(bookmark: Bookmark) {
        if (!isPaginating.get()) {
            viewModelScope.launch(Dispatchers.Default) {
                bookmarkRepo.saveBookmark(bookmark)
            }
        }
    }

    /**
     * Delete the given bookmark of this document from the database.
     *
     * @param title Title of the bookmark to delete.
     * @param index A character index.
     */
    fun deleteBookmark(title: String, index: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            // TODO: Make it work!!
            //bookmarkRepo.deleteBookmark(currentUri, title, index)
        }
    }

    /**
     * Update read info from book before close the activity.
     */
    fun updateBookBeforeClose() {
        bookWithBookmarks.value?.let { bookWithBookmarks ->
            val currentReadTime = Interval(bookWithBookmarks.book.lastOpenedAt, DateTime.now()).toDuration().standardMinutes.toInt()
            bookWithBookmarks.book.readTime += currentReadTime
            bookWithBookmarks.book.quote = getQuote()
            bookWithBookmarks.book.readingProgress = getReadingProgress()
            bookWithBookmarks.book.lastOpenedAt = DateTime.now()

            viewModelScope.launch(Dispatchers.Default) {
                bookRepo.updateBook(bookWithBookmarks.book)
            }
        }
    }

    /**
     * Return two lines string of current page.
     *
     * @return Two lines string of current page.
     */
    private fun getQuote(): String =
        (pagedBook.value?.get(currentPage.value!!.page)?.toString() ?: "")
            .trim()
            .splitToSequence("\n", limit = 3)
            .filterIndexed { index, _ -> index < 2 }
            .joinToString("\n")

    /**
     * Return reading progress using ratio between current page and size.
     *
     * @return Float value of reading progress.
     */
    private fun getReadingProgress(): Float =
        ((currentPage.value!!.page.toFloat()) / (pagedBook.value?.size?.toFloat() ?: 1f))
            .coerceAtMost(1.0f)

    /**
     * Run auto-read feature.
     *
     * @param bStart If true, start the auto-read.
     */
    fun runAutoRead(bStart: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            if (bStart) {
                autoReadJob = launch {
                    val tickTime = 500L
                    val tickerChannel = ticker(delayMillis = tickTime)
                    try {
                        var totalTime = pagedBook.value?.get(currentPage.value?.page ?: 0)?.length?.times(200)?.toLong() ?: 30000L
                        var timePassed = 0L
                        for (event in tickerChannel) {
                            // Goto next page or stop auto read.
                            if (totalTime <= timePassed) {
                                launch(Dispatchers.Main) {
                                    val currentPage = currentPage.value?.page ?: 0
                                    if (!goToPage(currentPage + 1)) {
                                        bAuto.value = false
                                    }
                                    else {
                                        // Set the totalTime with the number of characters ot the next page.
                                        totalTime = pagedBook.value?.get(currentPage)?.length?.times(200)?.toLong() ?: 30000L
                                        timePassed = 0
                                    }
                                }
                            }
                            else {
                                timePassed += tickTime
                                _autoReadTick.postValue(timePassed.toFloat() / totalTime.toFloat())
                            }
                        }
                    } finally {
                        tickerChannel.cancel()
                    }
                }
            }
            else {
                if (autoReadJob.isActive) {
                    autoReadJob.cancelAndJoin()
                }
            }
        }
    }

    /**
     * Extension function to notify the data is changed.
     * eg) List item has been added.
     */
    @WorkerThread
    private fun <T> MutableLiveData<T>.notify() {
        this.postValue(this.value)
    }

    /**
     * Data class for currentPage.
     */
    data class CurrentPage(
        val page: Int,
        val bSmoothScroll: Boolean = true
    )

    /**
     * This data class used for build StaticLayout.
     *
     * @param width Screen width of the paged view in px.
     * @param height Screen height of the paged view in px.
     * @param paint TextPaint object of the paged view.
     * @param spacingMult LineSpacingMultiplier of the paged view.
     * @param spacingExtra LineSpacingExtra of the paged view.
     * @param includePad IncludeFontPadding of the paged view.
     */
    data class StaticLayoutParam(
        val width: Int,
        val height: Int,
        val paint: TextPaint,
        val spacingMult: Float,
        val spacingExtra: Float,
        val includePad: Boolean
    )

    /**
     * Text appearance class to decorate page text view.
     */
    data class PageTextAppearance(
        val fontFamily: Typeface,
        val fontSize: Float = 0f,
        val fontColor: Int = 0,
        val lineSpacing: Float = 1.0f
    )

    enum class MenuState {
        Default,
        TopBottom,
        Bookmarks,
        Settings
    }
}
