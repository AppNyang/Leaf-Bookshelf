package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.data.repository.HistoryRepository
import com.appnyang.leafbookshelf.view.bookshelf.OnHistoryItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Bookshelf View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-03-19.
 */
class BookshelfViewModel(historyRepo: HistoryRepository, bookmarkRepo: BookmarkRepository) : ViewModel()  {

    val histories = historyRepo.loadHistory()

    private val _historyClicked = MutableLiveData<Pair<String, Long>>()
    val historyClicked: LiveData<Pair<String, Long>> = _historyClicked

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
