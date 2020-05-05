package com.appnyang.leafbookshelf.viewmodel

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
}
