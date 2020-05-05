package com.appnyang.leafbookshelf.data.model.book

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Book Data Access Object.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-04.
 */
@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(book: Book): Long

    @Query("SELECT * FROM book")
    fun getBooks(): LiveData<List<Book>>

    @Query("SELECT * FROM book WHERE bookId = :id")
    fun getBook(id: Long): LiveData<Book>

    @Transaction
    @Query("SELECT * FROM book WHERE bookId = :id")
    fun getBookWithBookmarks(id: Long): LiveData<BookWithBookmarks>

    @Transaction
    @Query("SELECT * FROM book WHERE uri = :uri")
    fun getBookWithBookmarks(uri: Uri): Flow<BookWithBookmarks?>

    @Update
    fun update(book: Book)

    @Delete
    fun delete(book: Book)
}
