package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.*
import com.appnyang.leafbookshelf.data.model.history.History
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.data.repository.HistoryRepository
import com.appnyang.leafbookshelf.view.main.OnHistoryItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class MainViewModel(historyRepo: HistoryRepository, private val bookmarkRepo: BookmarkRepository) : ViewModel() {

    private val _historyClicked = MutableLiveData<Pair<String, Long>>()
    val historyClicked: LiveData<Pair<String, Long>> = _historyClicked

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
     * @param title Title of the bookmark to delete.
     * @param index Char index of the bookmark.
     */
    fun deleteBookmark(uri: String, title: String, index: Long) {
        viewModelScope.launch(Dispatchers.Default) { bookmarkRepo.deleteBookmark(uri, title, index) }
    }

    /**
     * On recent files item clicked.
     */
    val onHistoryClickListener = OnHistoryItemClickListener { history ->
        viewModelScope.launch(Dispatchers.Default) {
            val charIndex = bookmarkRepo.loadLastRead(history.uri)?.index ?: 0L
            _historyClicked.postValue(Pair(history.uri, charIndex))
        }
    }
}

sealed class RecentFile

data class RecentHistory(
    val uri: String,
    val title: String,
    val readTime: Int,
    val lastOpen: String,
    val quote: String,
    val cover: String
) : RecentFile()

data class RecentPromo(
    val unifiedNativeAd: UnifiedNativeAd
) : RecentFile()
