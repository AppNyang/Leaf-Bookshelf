package com.appnyang.leafbookshelf.data.model.bookmark

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

/**
 * Bookmark Data Access Object.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-14.
 */
@Dao
interface BookmarkDao {
    @Insert(onConflict = REPLACE)
    fun insert(bookmark: Bookmark)

    @Query("SELECT * FROM bookmarks")
    fun getBookmarks(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE uri = :uri")
    fun getBookmarks(uri: String): LiveData<List<Bookmark>>

    @Query("DELETE FROM bookmarks WHERE uri = :uri AND \"index\" = :index")
    fun deleteByIndex(uri: String, index: Long)

    @Query("DELETE FROM bookmarks")
    fun deleteAll()
}
