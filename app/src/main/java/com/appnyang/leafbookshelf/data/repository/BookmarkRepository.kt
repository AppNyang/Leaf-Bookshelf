package com.appnyang.leafbookshelf.data.repository

import androidx.lifecycle.LiveData
import com.appnyang.leafbookshelf.data.model.bookmark.Bookmark
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkDao

/**
 * Bookmark Repository.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-14.
 */
class BookmarkRepository(private val bookmarkDao: BookmarkDao) {

    /**
     * Fetch all bookmarks from the database.
     *
     * @return A list of all bookmarks.
     */
    fun loadBookmarks(): LiveData<List<Bookmark>> = bookmarkDao.getBookmarks()

    /**
     * Save a bookmark to the database.
     *
     * @param bookmark A Bookmark to save.
     */
    fun saveBookmark(bookmark: Bookmark) {
        bookmarkDao.insert(bookmark)
    }

    /**
     * Delete a bookmark from the database.
     *
     * @param index Character index of the bookmark.
     */
    fun deleteBookmark(index: Long) {
        bookmarkDao.deleteByIndex(index)
    }
}
