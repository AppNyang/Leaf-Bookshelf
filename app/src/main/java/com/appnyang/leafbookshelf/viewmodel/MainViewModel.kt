package com.appnyang.leafbookshelf.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.appnyang.leafbookshelf.data.repository.BookRepository
import com.appnyang.leafbookshelf.data.repository.CollectionRepository
import com.appnyang.leafbookshelf.view.main.OnRecentsItemClickListener
import com.google.android.gms.ads.formats.UnifiedNativeAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime

/**
 * Main View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class MainViewModel(
    bookRepo: BookRepository,
    collectionRepo: CollectionRepository
) : ViewModel() {

    private val _historyClicked = MutableLiveData<Pair<Uri, Long>>()
    val historyClicked: LiveData<Pair<Uri, Long>> = _historyClicked

    val recents = MediatorLiveData<List<Recents>>()
    val recentPromos = MutableLiveData<List<RecentPromo>>()

    val collectionsWithBooks = collectionRepo.getCollectionsWithBooks()
        .asLiveData(Dispatchers.Default + viewModelScope.coroutineContext)

    init {
        recents.addSource(
            bookRepo.getRecentBooks()
                .asLiveData(Dispatchers.Default + viewModelScope.coroutineContext)
        ) { recents.value = it }
        recents.addSource(recentPromos) {
            // Called when all books and ads has been loaded.
            recents.value = sequence {
                // recents should contain recent books.
                recents.value?.let { files ->
                    val promo = it.iterator()
                    files.forEachIndexed { index, recentFile ->
                        yield(recentFile)
                        // Add promo for every multiple of 3. eg) 3, 6 ...
                        if ((index + 1) % 3 == 0 && promo.hasNext()) {
                            yield(promo.next())
                        }
                    }
                }
            }.toList()
        }
    }

    /**
     * On recent files item clicked.
     */
    val onRecentsClickListener = OnRecentsItemClickListener { recents ->
        viewModelScope.launch(Dispatchers.Default) {
            when (recents) {
                is RecentFile -> {
                    _historyClicked.postValue(Pair(recents.uri, -1))
                }
            }
        }
    }
}

sealed class Recents

data class RecentFile(
    val uri: Uri,
    val displayName: String,
    val coverUri: Uri,
    val quote: String,
    val readTime: Int,
    val lastOpenedAt: DateTime,
    val readingProgress: Float
) : Recents()

data class RecentPromo(
    val unifiedNativeAd: UnifiedNativeAd
) : Recents()
