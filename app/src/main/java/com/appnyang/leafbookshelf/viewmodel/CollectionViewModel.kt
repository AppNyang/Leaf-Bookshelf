package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.*
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.data.repository.CollectionRepository
import com.appnyang.leafbookshelf.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Collection View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-04-28.
 */
class CollectionViewModel(private val collectionRepo: CollectionRepository) : ViewModel() {

    private val _collection = MediatorLiveData<Collection>()
    val collection: LiveData<Collection> = _collection

    private val _collectionDeleted = SingleLiveEvent<Any>()
    val collectionDeleted: LiveData<Any> = _collectionDeleted

    /**
     * Read given collection from DB.
     *
     * @param id Id of a collection.
     */
    fun loadCollection(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            val dbSource = collectionRepo.loadCollection(id)
            _collection.addSource(dbSource) {
                _collection.postValue(it)
                _collection.removeSource(dbSource)
            }
        }
    }

    fun updateCollectionTitle(title: String) {
        viewModelScope.launch(Dispatchers.Default) {
            collection.value?.let {
                it.title = title
                collectionRepo.updateCollection(it)
            }
        }
    }

    /**
     * Remove all contained books of this collection.
     * TODO: Make it work.
     */
    fun emptyCollection() {
        /*viewModelScope.launch(Dispatchers.Default) {
            collection.value?.let {
                it.books.clear()
                collectionRepo.updateCollection(it)
            }
        }*/
    }

    /**
     * Delete this collection.
     */
    fun deleteCollection() {
        viewModelScope.launch(Dispatchers.Default) {
            collection.value?.let {
                collectionRepo.deleteCollection(it)

                _collectionDeleted.postCall()
            }
        }
    }
}
