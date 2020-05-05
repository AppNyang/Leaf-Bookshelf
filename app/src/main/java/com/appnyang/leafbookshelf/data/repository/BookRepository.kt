package com.appnyang.leafbookshelf.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.appnyang.leafbookshelf.data.model.book.Book
import com.appnyang.leafbookshelf.data.model.book.BookDao
import com.appnyang.leafbookshelf.data.model.book.BookWithBookmarks
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Books.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-05.
 */
class BookRepository(private val bookDao: BookDao) {

    /**
     * Fetch a book with bookmarks given uri from the database.
     *
     * @param uri Primary key for searching a book.
     * @return A BookWithBookmarks if exist or null.
     */
    fun getBookWithBookmarks(uri: Uri): Flow<BookWithBookmarks?> = bookDao.getBookWithBookmarks(uri)

    /**
     * Insert a new Book.
     *
     * @param book Book to save.
     * @return Index of inserted Book.
     */
    fun saveBook(book: Book): Long = bookDao.insert(book)

    /**
     * Update the given book.
     *
     * @param book Book to update.
     */
    fun updateBook(book: Book) = bookDao.update(book)
}
