package com.appnyang.leafbookshelf.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnyang.leafbookshelf.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Page View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class PageViewModel : ViewModel() {

    // Private live data.
    private val _chunkedText = MutableLiveData<List<CharSequence>>()
    private val _pagedBook = MutableLiveData<LinkedList<CharSequence>>()
    private val _chunkPaged = SingleLiveEvent<Any>()

    private val _showMenu = MutableLiveData<Boolean>(false)

    // Public live data.
    val chunkedText: LiveData<List<CharSequence>> = _chunkedText
    val pagedBook: LiveData<LinkedList<CharSequence>> = _pagedBook
    val chunkPaged: LiveData<Any> = _chunkPaged

    val currentPage = MutableLiveData(0)
    val bScrollAnim = AtomicBoolean(true)
    val isPaginating = AtomicBoolean(false)

    val showMenu: LiveData<Boolean> = _showMenu

    /**
     * Read text file from uri.
     *
     * @param uri
     * @param contentResolver
     */
    fun readBookFromUri(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            fetchBookFromUri(uri, contentResolver)
        }
    }

    /**
     * Fetch from the file using coroutine.
     *
     * @param uri
     * @param contentResolver
     */
    private suspend fun fetchBookFromUri(uri: Uri, contentResolver: ContentResolver) = withContext(Dispatchers.IO) {
        val builder = StringBuilder()

        val chunkedText = mutableListOf<CharSequence>()
        val chunkSize = 2048 // lines
        var chunkCount = 0
        contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
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

        _chunkedText.postValue(chunkedText)
    }

    /**
     * Paginating function should be called AFTER the view is laid out,
     * because it needs the view's height to get list of pages.
     *
     * @param layoutParam Layout parameters to build StaticLayout.
     * @param charIndex The char position the user last read.
     */
    suspend fun paginateBook(layoutParam: StaticLayoutParam, charIndex: Long = 0L) = withContext(Dispatchers.Default) {
        chunkedText.value?.let { text ->
            isPaginating.set(true)
            val (chunkStart, charIndexInChunk) = getChunkCharIndices(charIndex, text)

            val pagedCharSequence = mutableListOf<CharSequence>()

            buildChunkSequence(chunkStart, text.size).forEach { chunkIndex ->
                // 1. Build a StaticLayout to measure the text.
                val layout = buildStaticLayout(layoutParam, text[chunkIndex])

                // 2. Split the text in the page.
                var beginOffset = 0
                var heightThreshold = layoutParam.height
                for (i in 0 until layout.lineCount) {
                    // When the line has been exceeded single page,
                    if (heightThreshold < layout.getLineBottom(i)) {
                        pagedCharSequence.add(text[chunkIndex].subSequence(beginOffset until layout.getLineStart(i)))
                        beginOffset = layout.getLineStart(i)
                        heightThreshold = layout.getLineTop(i) + layoutParam.height
                    }
                }

                // Add rest of the sequence.
                if (beginOffset != layout.getLineEnd(layout.lineCount - 1)) {
                    pagedCharSequence
                        .add(text[chunkIndex].subSequence(beginOffset until layout.getLineEnd(layout.lineCount - 1)))
                }

                // 3. Set paged data.
                if (chunkIndex == chunkStart) {
                    // After first chunk had been processed, fire the alarm to show the contents.
                    val list = LinkedList<CharSequence>()
                    list.addAll(pagedCharSequence.toList())
                    _pagedBook.postValue(list)

                    // If the user load the book with bookmark, go to the bookmark.
                    if (charIndexInChunk != 0L) {
                        bScrollAnim.set(false)
                        postCurrentPageToIndex(charIndexInChunk)
                    }
                }
                else {
                    if (chunkIndex > chunkStart) {
                        _pagedBook.value?.addAll(pagedCharSequence.toList())
                        _chunkPaged.postCall()
                    } else {
                        pagedCharSequence.reversed().asSequence().forEach { _pagedBook.value?.addFirst(it.toString()) }
                        _chunkPaged.postCall()
                        bScrollAnim.set(false)
                        currentPage.postValue(currentPage.value?.plus(pagedCharSequence.size))
                    }
                }

                pagedCharSequence.clear()
            }
            isPaginating.set(false)
        }
    }

    /**
     * Find the index of the first chunk to be processed
     * using the charIndex where the user last read.
     *
     * @param charIndex The char position the user last read.
     * @param text A list of chunks.
     * @return The index of the chunk to be processed and charIndex in the chunk.
     */
    private suspend fun getChunkCharIndices(charIndex: Long, text: List<CharSequence>): Pair<Int, Long> = withContext(Dispatchers.Default) {
        var sumChars = 0L
        var chunkStart = 0
        var charIndexInChunk = 0L
        for (i in text.indices) {
            sumChars += text[i].length
            if (sumChars >= charIndex) {
                chunkStart = i
                charIndexInChunk = charIndex - sumChars + text[i].length
                break
            }
        }

        Pair(chunkStart, charIndexInChunk)
    }

    /**
     * Returns the order in which the chunks will be processed.
     * If chunkStart is 3 and chunkLength is 5, the sequence will be
     * [3, 4, 0, 1, 2]
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
     * @param index Character position.
     */
    private suspend fun postCurrentPageToIndex(index: Long) = withContext(Dispatchers.Default) {
        pagedBook.value?.let {
            var page = it.size - 1
            var sum = 0

            it.asSequence()
                .filter { sum - 1 < index }
                .forEachIndexed { i, text ->
                    page = i
                    sum += text.length
                }

            currentPage.postValue(page)
        }
    }

    /**
     * Move page to the given page.
     * Called when the user click the left or right side of the page.
     *
     * @param page
     */
    fun goToPage(page: Int) {
        if (_showMenu.value!!) {
            _showMenu.value = false
        }
        else if (page in _pagedBook.value!!.indices && page != currentPage.value) {
            currentPage.value = page
        }
    }

    fun showMenu() {
        _showMenu.value = !_showMenu.value!!
    }

    /**
     * Get current character position of pagedBook.
     */
    fun getCurrentTextIndex(): Long = pagedBook.value?.run {
        filterIndexed { index, _ -> index < (currentPage.value ?: 0) }
        sumBy { it.length }
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
