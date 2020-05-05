package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.ViewModel
import com.appnyang.leafbookshelf.data.repository.BookRepository

/**
 * Book View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-06.
 */
class BookViewModel(private val bookRepo: BookRepository) : ViewModel() {
}
