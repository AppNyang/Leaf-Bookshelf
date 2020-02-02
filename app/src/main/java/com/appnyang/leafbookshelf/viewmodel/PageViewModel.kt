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

/**
 * Page View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class PageViewModel : ViewModel() {

    // Private live data.
    private val _chunkedText = MutableLiveData<List<CharSequence>>()
    private val _pagedBook = MutableLiveData<List<CharSequence>>()
    private val _chunkPaged = SingleLiveEvent<Any>()

    private val _showMenu = MutableLiveData<Boolean>(false)

    // Public live data.
    val chunkedText: LiveData<List<CharSequence>> = _chunkedText
    val pagedBook: LiveData<List<CharSequence>> = _pagedBook
    val chunkPaged: LiveData<Any> = _chunkPaged

    val currentPage = MutableLiveData<Int>(0)

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
        spacingMult: Float, spacingExtra: Float, includePad: Boolean)
            = withContext(Dispatchers.Default) {

        val pagedSequence = mutableListOf<CharSequence>()

        chunkedText.value!!.forEachIndexed { index, it ->
            // 1. Build a StaticLayout to measure the text.
            val layout: StaticLayout = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                StaticLayout(it, paint, width, Layout.Alignment.ALIGN_NORMAL, spacingMult, spacingExtra, includePad)
            } else {
                StaticLayout.Builder
                    .obtain(it, 0, it.length, paint, width)
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
                    pagedSequence.add(it.subSequence(beginOffset until layout.getLineStart(i)))
                    beginOffset = layout.getLineStart(i)
                    heightThreshold = layout.getLineTop(i) + height
                }
            }

            // Add rest of the sequence.
            if (beginOffset != layout.getLineEnd(layout.lineCount - 1)) {
                pagedSequence
                    .add(it.subSequence(beginOffset until layout.getLineEnd(layout.lineCount - 1)))
            }

            if (index == 0) {
                // After first chunk had been processed, fire the alarm.
                _pagedBook.postValue(pagedSequence)
            }
            else {
                _chunkPaged.postCall()
            }
        }
    }

    /**
     * Move page to the given page.
     *
     * @param page
     */
    fun goToPage(page: Int) {
        if (page in _pagedBook.value!!.indices && page != currentPage.value) {
            currentPage.value = page
        }
    }

    fun showMenu() {
        _showMenu.value = !_showMenu.value!!
    }
}
