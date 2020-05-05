package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.*
import com.appnyang.leafbookshelf.data.model.book.Book
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.data.repository.BookRepository
import com.appnyang.leafbookshelf.data.repository.CollectionRepository
import com.appnyang.leafbookshelf.view.bookshelf.OnBookshelfItemClickListener
import com.appnyang.leafbookshelf.view.bookshelf.OnBookshelfItemLongClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Bookshelf View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-03-19.
 */
class BookshelfViewModel(
    private val collectionRepo: CollectionRepository,
    private val bookRepo: BookRepository
) : ViewModel()  {

    val collectionsWithBooks = collectionRepo.getCollectionsWithBooks()
        .asLiveData(Dispatchers.Default + viewModelScope.coroutineContext)

    // List of books to display given collection.
    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books

    private val _historyClicked = MutableLiveData<Pair<String, Long>>()
    val historyClicked: LiveData<Pair<String, Long>> = _historyClicked

    private val _state = MutableLiveData(State.Default)
    val state: LiveData<State> = _state

    /**
     * Find books from collectionWithBooks and set books to _books live data to show.
     *
     * @param collectionId Collection id to find.
     */
    fun requestBooks(collectionId: Long) {
        viewModelScope.launch {
            if (collectionId < 0) {
                launch(Dispatchers.Default) {
                    _books.postValue(bookRepo.getBooksAsync())
                }
            }
            else {
                collectionsWithBooks.value
                    ?.firstOrNull { it.collection.collectionId == collectionId }
                    ?.let { _books.value = it.books }
            }
        }
    }

    /**
     * On bookshelf book item clicked.
     */
    val onBookshelfClickListener = OnBookshelfItemClickListener { card, history ->
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
