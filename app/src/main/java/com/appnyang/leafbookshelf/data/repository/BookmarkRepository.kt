package com.appnyang.leafbookshelf.data.repository

import androidx.lifecycle.LiveData
import com.appnyang.leafbookshelf.data.model.bookmark.Bookmark
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkDao
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkType

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
     * Fetch all bookmarks given uri from the database.
     *
     * @return A list of bookmarks given uri.
     */
    fun loadBookmarks(uri: String): LiveData<List<Bookmark>> = bookmarkDao.getBookmarks(uri)

    /**
     * Fetch last-read bookmark.
     *
     * @return A last-read bookmark.
     */
    fun loadLastRead(uri: String): Bookmark? = bookmarkDao.getLastRead(uri)

    /**
     * Save a bookmark to the database.
     *
     * @param bookmark A Bookmark to save.
     */
    fun saveBookmark(bookmark: Bookmark) {
        if (bookmark.type == BookmarkType.LAST_READ.name) {
            bookmarkDao.upsertLastRead(bookmark)
        }
        else {
            bookmarkDao.insert(bookmark)
        }
    }

    /**
     * Delete a bookmark from the database.
     *
     * @param uri A URI of the file.
     * @param title Title of the bookmark to delete.
     * @param index Character index of the bookmark.
     */
    fun deleteBookmark(uri: String, title: String, index: Long) {
        bookmarkDao.deleteByIndex(uri, title, index)
    }
}
