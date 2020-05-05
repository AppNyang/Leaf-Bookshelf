package com.appnyang.leafbookshelf.data.repository

import android.net.Uri
import com.appnyang.leafbookshelf.data.model.book.Book
import com.appnyang.leafbookshelf.data.model.book.BookDao
import com.appnyang.leafbookshelf.data.model.book.BookWithBookmarks
import com.appnyang.leafbookshelf.viewmodel.RecentFile
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Books.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-05.
 */
class BookRepository(private val bookDao: BookDao) {

    /**
     * Fetch all books from the database.
     *
     * @return A list of books.
     */
    suspend fun getBooksAsync(): List<Book> = bookDao.getBooksAsync()

    /**
     * Fetch a book with bookmarks given id from the database.
     *
     * @param bookId Primary key for searching a book.
     * @return A BookWithBookmarks if exist or null.
     */
    fun getBookWithBookmarks(bookId: Long): Flow<BookWithBookmarks?> = bookDao.getBookWithBookmarks(bookId)

    /**
     * Fetch a book with bookmarks given uri from the database.
     *
     * @param uri Primary key for searching a book.
     * @return A BookWithBookmarks if exist or null.
     */
    fun getBookWithBookmarks(uri: Uri): Flow<BookWithBookmarks?> = bookDao.getBookWithBookmarks(uri)

    /**
     * Fetch books sorted in descending order by lastOpenedAt with given limit.
     *
     * @param limit The number of rows to fetch.
     * @return Flow of Book if exist or null.
     */
    fun getRecentBooks(limit: Int = 6): Flow<List<RecentFile>?> = bookDao.getRecentBooks(limit)

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

    /**
     * Delete the given book from the database.
     *
     * @param book Book to delete.
     */
    fun deleteBook(book: Book) = bookDao.delete(book)
}
