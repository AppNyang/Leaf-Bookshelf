package com.appnyang.leafbookshelf.data.model.book

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Book Data Access Object.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-04.
 */
@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(book: Book)

    @Query("SELECT * FROM book")
    fun getBooks(): List<LiveData<Book>>

    @Update
    fun update(book: Book)

    @Delete
    fun delete(book: Book)
}
