package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.*
import com.appnyang.leafbookshelf.data.model.history.History
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.data.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class MainViewModel(historyRepo: HistoryRepository, private val bookmarkRepo: BookmarkRepository) : ViewModel() {

    val history = MediatorLiveData<List<History>>()
    val bookmarks = bookmarkRepo.loadBookmarks()

    init {
        // This empty live data prevents 'Failed to call observer method' error.
        history.addSource(MutableLiveData<List<History>>(listOf())) { history.value = it }
        history.addSource(historyRepo.loadHistory()) { history.value = it }
    }

    /**
     * Delete a bookmark.
     *
     * @param uri Uri of the bookmark.
     * @param index Char index of the bookmark.
     */
    fun deleteBookmark(uri: String, index: Long) {
        viewModelScope.launch(Dispatchers.Default) { bookmarkRepo.deleteBookmark(uri, index) }
    }
}
