package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.ViewModel
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.data.repository.HistoryRepository

/**
 * Main View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class MainViewModel(historyRepo: HistoryRepository, bookmarkRepo: BookmarkRepository) : ViewModel() {

    val history = historyRepo.loadHistory()
    val bookmarks = bookmarkRepo.loadBookmarks()
}
