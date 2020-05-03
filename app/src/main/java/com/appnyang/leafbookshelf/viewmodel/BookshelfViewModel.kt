package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.data.repository.CollectionRepository
import com.appnyang.leafbookshelf.data.repository.HistoryRepository
import com.appnyang.leafbookshelf.view.bookshelf.OnBookshelfItemLongClickListener
import com.appnyang.leafbookshelf.view.bookshelf.OnHistoryItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Bookshelf View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-03-19.
 */
class BookshelfViewModel(
    historyRepo: HistoryRepository,
    bookmarkRepo: BookmarkRepository,
    private val collectionRepo: CollectionRepository
) : ViewModel()  {

    val books = historyRepo.loadHistory()
    val collections = collectionRepo.loadCollections()

    private val _historyClicked = MutableLiveData<Pair<String, Long>>()
    val historyClicked: LiveData<Pair<String, Long>> = _historyClicked

    private val _state = MutableLiveData(State.Default)
    val state: LiveData<State> = _state

    /**
     * On recent files item clicked.
     */
    val onHistoryClickListener = OnHistoryItemClickListener { card, history ->
        when (state.value) {
            State.Default -> {
                viewModelScope.launch(Dispatchers.Default) {
                    // TODO: Make it work!!
                    /*val charIndex = bookmarkRepo.loadLastRead(history.uri)?.index ?: 0L
                    _historyClicked.postValue(Pair(history.uri, charIndex))*/
                }
            }
            State.Checked -> {
                card.isChecked = !card.isChecked
            }
        }
    }

    val onBookshelfLongClickListener = OnBookshelfItemLongClickListener {
        when (state.value) {
            State.Default -> { _state.value = State.Checked }
            State.Checked -> { _state.value = State.Default }
        }
    }

    /**
     * Create a new collection to DB.
     *
     * @param collection The collection object to create.
     */
    fun createCollection(collection: Collection) {
        viewModelScope.launch(Dispatchers.Default) {
            collectionRepo.createCollection(collection)
        }
    }

    /**
     * Set state. States could have two values, Default and Selected.
     *
     * @param newState New state to set.
     */
    fun setState(newState: State) {
        if (state.value != newState) {
            _state.value = newState
        }
    }

    enum class State {
        Default,
        Checked
    }
}
