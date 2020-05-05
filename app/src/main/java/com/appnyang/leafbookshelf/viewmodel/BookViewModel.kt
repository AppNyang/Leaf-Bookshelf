package com.appnyang.leafbookshelf.viewmodel

import android.net.Uri
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnyang.leafbookshelf.data.model.book.BookWithBookmarks
import com.appnyang.leafbookshelf.data.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Book View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-06.
 */
class BookViewModel(private val bookRepo: BookRepository) : ViewModel() {

    private val _bookWithBookmarks = MutableLiveData<BookWithBookmarks>()
    val bookWithBookmarks: LiveData<BookWithBookmarks> = _bookWithBookmarks

    private val _buttonClicked = MutableLiveData<View>()
    val buttonClicked: LiveData<View> = _buttonClicked

    /**
     * Get flow of book with given bookId and collect.
     *
     * @param bookId Id of a book to read from database.
     */
    fun loadBook(bookId: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            bookRepo.getBookWithBookmarks(bookId).collect {
                _bookWithBookmarks.postValue(it)
            }
        }
    }

    /**
     * Called when the buttons clicked.
     *
     * @param view View of the button.
     */
    fun buttonClicked(view: View) {
        _buttonClicked.value = view
    }

    /**
     * Set new book cover.
     *
     * @param uri Uri of the image.
     */
    fun setBookCover(uri: Uri) {
        bookWithBookmarks.value?.let {
            it.book.coverUri = uri

            viewModelScope.launch(Dispatchers.Default) {
                bookRepo.updateBook(it.book)
            }
        }
    }

    /**
     * Delete this book form the database.
     */
    fun deleteBook() {
        bookWithBookmarks.value?.let {
            val book = it.book

            viewModelScope.launch(Dispatchers.Default) {
                bookRepo.deleteBook(book)
            }
        }
    }
}
