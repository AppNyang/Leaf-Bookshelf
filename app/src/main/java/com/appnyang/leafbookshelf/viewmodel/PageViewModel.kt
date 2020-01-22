package com.appnyang.leafbookshelf.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _rawText = MutableLiveData<String>()

    val rawText: LiveData<String> = _rawText

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

        contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    builder.append(line + "\n")
                    line = reader.readLine()
                }
            }
        }

        _rawText.postValue(builder.toString())
    }
}
