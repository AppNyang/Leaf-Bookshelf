package com.appnyang.leafbookshelf.data.model.bookmark

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

/**
 * Bookmark Data Access Object.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-14.
 */
@Dao
interface BookmarkDao {
    @Insert(onConflict = REPLACE)
    fun insert(bookmark: Bookmark)

    @Update
    fun update(bookmark: Bookmark)

    @Query("SELECT * FROM bookmarks")
    fun getBookmarks(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE uri = :uri")
    fun getBookmarks(uri: String): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE uri = :uri AND type = \"LAST_READ\"")
    fun getLastRead(uri: String): Bookmark?

    @Transaction
    fun upsertLastRead(bookmark: Bookmark) {
        val lastRead = getLastRead(bookmark.uri)
        if (lastRead != null) {
            bookmark.id = lastRead.id
        }
        insert(bookmark)
    }

    @Query("DELETE FROM bookmarks WHERE uri = :uri AND \"index\" = :index")
    fun deleteByIndex(uri: String, index: Long)

    @Query("DELETE FROM bookmarks")
    fun deleteAll()
}
