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

    val currentPage = MutableLiveData<Int>(0)
    val bScrollAnim = AtomicBoolean(true)

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
     * In regard to the limitation above, we assume that reading files takes much time than drawing ui.
     *
     * @param width Screen width of the paged view in px.
     * @param height Screen height of the paged view in px.
     * @param paint TextPaint object of the paged view.
     * @param spacingMult LineSpacingMultiplier of the paged view.
     * @param spacingExtra LineSpacingExtra of the paged view.
     * @param includePad IncludeFontPadding of the paged view.
     */
    suspend fun paginateBook(
        width: Int, height: Int, paint: TextPaint,
        spacingMult: Float, spacingExtra: Float, includePad: Boolean, charIndex: Long = 0L)
            = withContext(Dispatchers.Default) {

        chunkedText.value?.let { text ->
            // Find first chunk.
            var sumChars = 0L
            var chunkStart = 0
            var charIndexInChunk = 0L
            for (i in text.indices) {
                sumChars += text[i].length
                if (sumChars >= charIndex ) {
                    chunkStart = i
                    charIndexInChunk = charIndex - sumChars + text[i].length
                    break
                }
            }

            val pagedSequence = mutableListOf<CharSequence>()

            val chunkSequence: Sequence<Int>
            if (chunkStart > 0) {
                chunkSequence = (chunkStart until text.size).asSequence() + ((chunkStart - 1) downTo 0).asSequence()
            }
            else {
                chunkSequence = (chunkStart until text.size).asSequence()
            }
            chunkSequence.forEach { chunkIndex ->
                // 1. Build a StaticLayout to measure the text.
                val layout: StaticLayout = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    @Suppress("DEPRECATION")
                    StaticLayout(text[chunkIndex], paint, width, Layout.Alignment.ALIGN_NORMAL, spacingMult, spacingExtra, includePad)
                } else {
                    StaticLayout.Builder
                        .obtain(text[chunkIndex], 0, text[chunkIndex].length, paint, width)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(spacingExtra, spacingMult)
                        .setIncludePad(includePad)
                        .build()
                }

                // 2. Split the text in the page.
                var beginOffset = 0
                var heightThreshold = height
                for (i in 0 until layout.lineCount) {
                    // When the line has been exceeded single page,
                    if (heightThreshold < layout.getLineBottom(i)) {
                        pagedSequence.add(text[chunkIndex].subSequence(beginOffset until layout.getLineStart(i)))
                        beginOffset = layout.getLineStart(i)
                        heightThreshold = layout.getLineTop(i) + height
                    }
                }

                // Add rest of the sequence.
                if (beginOffset != layout.getLineEnd(layout.lineCount - 1)) {
                    pagedSequence
                        .add(text[chunkIndex].subSequence(beginOffset until layout.getLineEnd(layout.lineCount - 1)))
                }

                if (chunkIndex == chunkStart) {
                    // After first chunk had been processed, fire the alarm.
                    val list = LinkedList<CharSequence>()
                    list.addAll(pagedSequence.toList())
                    _pagedBook.postValue(list)

                    // If the user load the book with bookmark, go to the bookmark.
                    if (charIndex != 0L) {
                        setCurrentPageToTextIndex(charIndexInChunk)
                    }
                }
                else {
                    if (chunkIndex > chunkStart) {
                        _pagedBook.value?.addAll(pagedSequence.toList())
                        _chunkPaged.postCall()
                    } else {
                        pagedSequence.reversed().forEach { _pagedBook.value?.addFirst(it.toString()) }
                        _chunkPaged.postCall()
                        bScrollAnim.set(false)
                        currentPage.postValue(currentPage.value?.plus(pagedSequence.size))
                    }
                }

                pagedSequence.clear()
            }
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
    fun getCurrentTextIndex(): Long = pagedBook.value
        ?.filterIndexed { index, _ -> index < (currentPage.value ?: 0) }
        ?.sumBy { it.length }
        ?.toLong()
        ?: 0L

    /**
     * Move to the corresponding page with given character position.
     *
     * @param index Character position.
     */
    suspend fun setCurrentPageToTextIndex(index: Long) = withContext(Dispatchers.Default) {
        pagedBook.value?.let {
            var page = pagedBook.value?.size?.minus(1) ?: 0
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
}
