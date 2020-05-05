package com.appnyang.leafbookshelf.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.appnyang.leafbookshelf.data.repository.BookRepository
import com.appnyang.leafbookshelf.data.repository.CollectionRepository
import com.appnyang.leafbookshelf.view.main.OnHistoryItemClickListener
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

    private val _historyClicked = MutableLiveData<Pair<String, Long>>()
    val historyClicked: LiveData<Pair<String, Long>> = _historyClicked

    val recents = MediatorLiveData<List<Recents>>()
    val recentFilePromos = MutableLiveData<List<RecentPromo>>()

    val collections = collectionRepo.loadCollections()

    init {
        /*recents.addSource(
            bookRepo.getRecentBooks()
                .asLiveData(Dispatchers.Default + viewModelScope.coroutineContext)
        ) { recents.value = it }*/
        /*recentFiles.addSource(recentFilePromos) {
            // Called when all ads has been loaded.
            recentFiles.value = sequence {
                recentFiles.value?.let { files ->
                    val promo = it.iterator()
                    files.forEachIndexed { index, recentFile ->
                        // Add promo for every multiple of 3.
                        if (index % 3 == 0 && promo.hasNext()) {
                            yield(promo.next())
                        }
                        yield(recentFile)
                    }
                }
            }.toList()
        }*/
    }

    /**
     * On recent files item clicked.
     */
    val onHistoryClickListener = OnHistoryItemClickListener { recentFile ->
        viewModelScope.launch(Dispatchers.Default) {
            when (recentFile) {
                is RecentFile -> {
                    // TODO: Make it work!!
                    /*val charIndex = bookmarkRepo.loadLastRead(recentFile.uri)?.index ?: 0L
                    _historyClicked.postValue(Pair(recentFile.uri, charIndex))*/
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
    val lastOpenedAt: DateTime
) : Recents()

data class RecentPromo(
    val unifiedNativeAd: UnifiedNativeAd
) : Recents()
