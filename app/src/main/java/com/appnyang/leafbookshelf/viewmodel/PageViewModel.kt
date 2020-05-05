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
import androidx.annotation.WorkerThread
import androidx.core.text.toSpanned
import androidx.lifecycle.*
import com.appnyang.leafbookshelf.core.LeafApp
import com.appnyang.leafbookshelf.data.model.book.Book
import com.appnyang.leafbookshelf.data.model.book.BookWithBookmarks
import com.appnyang.leafbookshelf.data.model.bookmark.Bookmark
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkType
import com.appnyang.leafbookshelf.data.model.history.History
import com.appnyang.leafbookshelf.data.repository.BookRepository
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.data.repository.HistoryRepository
import com.appnyang.leafbookshelf.service.TtsService
import com.appnyang.leafbookshelf.util.SharedPreferenceLiveData
import com.appnyang.leafbookshelf.util.SingleLiveEvent
import com.appnyang.leafbookshelf.util.icu.CharsetDetector
import com.appnyang.leafbookshelf.util.styler.DefaultStyler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.ISODateTimeFormat
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
    private val historyRepository: HistoryRepository,
    val sharedPreferenceLiveData: SharedPreferenceLiveData,
    application: Application
) : AndroidViewModel(application) {

    private val _bookWithBookmarks = MutableLiveData<BookWithBookmarks>()
    val bookWithBookmarks: LiveData<BookWithBookmarks> = _bookWithBookmarks

    private val _pagedBook = MutableLiveData<LinkedList<Spanned>>()
    val pagedBook: LiveData<LinkedList<Spanned>> = _pagedBook

    // This event is called after each chunk of text has been paginated.
    private val _chunkPaged = SingleLiveEvent<Any>()
    val chunkPaged: LiveData<Any> = _chunkPaged

    // Showing menu flags.
    private val _showMenu = MutableLiveData(false)
    val showMenu: LiveData<Boolean> = _showMenu
    private val _showSettings = MutableLiveData(false)
    val showSettings: LiveData<Boolean> = _showSettings
    private val _showBookmark = MutableLiveData(false)
    val showBookmark: LiveData<Boolean> = _showBookmark

    private lateinit var bookmarksDbSource: LiveData<List<Bookmark>>
    private val _bookmarks = MediatorLiveData<List<Bookmark>>()
    val bookmarks: LiveData<List<Bookmark>> = _bookmarks

    val currentPage = MutableLiveData(0)
    val bScrollAnim = AtomicBoolean(true)
    val isPaginating = AtomicBoolean(false)

    val bTts = MutableLiveData(false)
    val bAuto = MutableLiveData(false)

    private lateinit var openTime: DateTime

    // TTS Service.
    private lateinit var ttsService: TtsService
    private var isBound = false
    private val ttsServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            ttsService = (binder as TtsService.LocalBinder).getService()
            isBound = true

            setOnUserCancelReadListener()

            val title = ""
            pagedBook.value?.let { book ->
                ttsService.read(title, book, currentPage)
            }
        }
        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            bTts.value = false
        }
    }

    // Preferences.
    // These values are set in readPreferences@PageActivity
    lateinit var fontFamily: Typeface
    var fontSize = 0f
    var fontColor = 0
    var lineSpacing = 1.0f

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
                    // Create a new Book.
                    val displayName = fetchBookNameFromUri(uri, contentResolver)
                    val book = Book(uri, displayName, Uri.parse("color:red"), "", 0, DateTime.now())
                    bookRepo.saveBook(book)
                }
                else {
                    if (!isBookLoaded.getAndSet(true)) {
                        launch {
                            it.book.lastOpenedAt = DateTime.now()

                            val chunkedText = fetchBookFromUri(uri, contentResolver)
                            var loadIndex = charIndex
                            // The charIndex less than 0 means "Open recently read page".
                            if (charIndex < 0) {
                                loadIndex = 0
                                bookWithBookmarks.value?.let { bookWithBookmarks ->
                                    loadIndex = bookWithBookmarks.bookmarks
                                        .firstOrNull { bookmark -> bookmark.type == BookmarkType.LAST_READ.name }?.index
                                        ?: 0
                                }
                            }

                            paginateBook(chunkedText, layoutParam, loadIndex)
                        }
                    }

                    _bookWithBookmarks.postValue(it)
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
                    bScrollAnim.set(false)
                    postCurrentPageToIndex(list, charIndexInChunk)
                }
            }
            else {
                if (chunkIndex > chunkStart) {
                    _pagedBook.value?.addAll(pagedCharSequence.toList())
                    _chunkPaged.postCall()
                } else {
                    pagedCharSequence.reversed().asSequence().forEach { _pagedBook.value?.addFirst(it) }
                    _chunkPaged.postCall()
                    bScrollAnim.set(false)
                    currentPage.postValue(currentPage.value?.plus(pagedCharSequence.size))
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
    private fun postCurrentPageToIndex(list: List<CharSequence> ,index: Long) {
        var page = list.size - 1
        var sum = 0

        list.asSequence()
            .filter { sum - 1 < index }
            .forEachIndexed { i, text ->
                page = i
                sum += text.length
            }

        currentPage.postValue(page)
    }

    /**
     * Move page to the given page.
     * Called when the user click the left or right side of the page.
     *
     * @param page
     */
    fun goToPage(page: Int) {
        if (page in _pagedBook.value!!.indices && page != currentPage.value) {
            currentPage.value = page
        }
    }

    /**
     * Show menu. It is called when the user click the middle of the page.
     */
    fun onShowMenuClicked() {
        if (isAnyMenuOpened()) {
            displayMenu()
        }
        else {
            displayMenu(menu = _showMenu.value?.not() ?: false)
        }
    }

    /**
     * Show settings menu.
     */
    fun onSettingsClicked() {
        displayMenu(settings = true)
    }

    /**
     * Show bookmarks menu.
     */
    fun onBookmarkClicked() {
        displayMenu(bookmark = true)
    }

    /**
     * Return true if any menu is opened.
     */
    fun isAnyMenuOpened(): Boolean = showMenu.value?.or(showSettings.value?.or(showBookmark.value ?: false) ?: false) ?: false

    /**
     * Open menu panel.
     */
    fun displayMenu(menu: Boolean = false, settings: Boolean = false, bookmark: Boolean = false) {
        _showMenu.value = menu
        _showSettings.value = settings
        _showBookmark.value = bookmark
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
            .filterIndexed { index, _ -> index < (currentPage.value ?: 0) }
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

            currentPage.postValue(page)
        }
    }

    /**
     * Go to the next page.
     *
     * @return true if current page is not the last page.
     */
    @WorkerThread
    fun goToNextPage(): Boolean {
        val hasNext = currentPage.value ?: 0 < pagedBook.value?.size?.minus(1) ?: -1

        if (hasNext) {
            currentPage.postValue(currentPage.value?.plus(1))
        }

        return hasNext
    }

    /**
     * Bookmark the current page.
     *
     * @param title Title of the bookmark.
     * @param type BookmarkType.
     */
    fun saveCurrentBookmark(title: String, type: BookmarkType = BookmarkType.CUSTOM) {
        // TODO: Make it work!!
        //saveBookmark(Bookmark(currentUri, title, getCurrentTextIndex(), type.name, getCurrentDateTimeAsString()))
    }

    /**
     * Save the given bookmark to the database.
     *
     * @param bookmark A bookmark.
     */
    private fun saveBookmark(bookmark: Bookmark) {
        if (!isPaginating.get()) {
            viewModelScope.launch(Dispatchers.Default) {
                // TODO: Make it work!!
                //bookmarkRepo.saveBookmark(bookmark)
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
     * Append current file to open history.
     * This called only when the activity is closed.
     */
    fun saveHistory() {
        /*if (::openTime.isInitialized) {
            viewModelScope.launch(Dispatchers.Default) {
                val readTime = Interval(openTime, DateTime.now()).toDuration().standardMinutes.toInt() + lastReadTime

                historyRepository.saveHistory(
                    History(
                        currentUri,
                        openedFileName.value?.toString() ?: "",
                        readTime,
                        getCurrentDateTimeAsString(),
                        getQuote()
                    )
                )
            }
        }*/
    }

    /**
     * Return date-time as ISO date time format string.
     *
     * @return ISO date-time format string.
     */
    private fun getCurrentDateTimeAsString(): String = DateTime.now().toString(ISODateTimeFormat.dateTime())

    /**
     * Return two lines string of current page.
     *
     * @return Two lines string of current page.
     */
    private fun getQuote(): String =
        (pagedBook.value?.get(currentPage.value ?: 0)?.toString() ?: "")
            .trim()
            .splitToSequence("\n", limit = 3)
            .filterIndexed { index, _ -> index < 2 }
            .joinToString("\n")

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
}
