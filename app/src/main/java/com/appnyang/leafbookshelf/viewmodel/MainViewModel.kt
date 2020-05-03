package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.*
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.data.repository.CollectionRepository
import com.appnyang.leafbookshelf.data.repository.HistoryRepository
import com.appnyang.leafbookshelf.view.main.OnHistoryItemClickListener
import com.google.android.gms.ads.formats.UnifiedNativeAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class MainViewModel(
    historyRepo: HistoryRepository,
    private val bookmarkRepo: BookmarkRepository,
    collectionRepo: CollectionRepository
) : ViewModel() {

    private val _historyClicked = MutableLiveData<Pair<String, Long>>()
    val historyClicked: LiveData<Pair<String, Long>> = _historyClicked

    val recentFiles = MediatorLiveData<List<RecentFile>>()
    val recentFilePromos = MutableLiveData<List<RecentPromo>>()

    val collections = collectionRepo.loadCollections()

    // TODO: Make it work!!
    val bookmarks = null// bookmarkRepo.loadBookmarks()

    init {
        // This empty live data prevents 'Failed to call observer method' error.
        recentFiles.addSource(MutableLiveData<List<RecentFile>>(listOf())) { recentFiles.value = it }
        recentFiles.addSource(historyRepo.loadAsRecentHistory()) { recentFiles.value = it }
        recentFiles.addSource(recentFilePromos) {
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
        }
    }

    /**
     * Delete a bookmark.
     *
     * @param uri Uri of the bookmark.
     * @param title Title of the bookmark to delete.
     * @param index Char index of the bookmark.
     */
    fun deleteBookmark(uri: String, title: String, index: Long) {
        // TODO: Make it work!!
        //viewModelScope.launch(Dispatchers.Default) { bookmarkRepo.deleteBookmark(uri, title, index) }
    }

    /**
     * On recent files item clicked.
     */
    val onHistoryClickListener = OnHistoryItemClickListener { recentFile ->
        viewModelScope.launch(Dispatchers.Default) {
            when (recentFile) {
                is RecentHistory -> {
                    // TODO: Make it work!!
                    /*val charIndex = bookmarkRepo.loadLastRead(recentFile.uri)?.index ?: 0L
                    _historyClicked.postValue(Pair(recentFile.uri, charIndex))*/
                }
            }
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
