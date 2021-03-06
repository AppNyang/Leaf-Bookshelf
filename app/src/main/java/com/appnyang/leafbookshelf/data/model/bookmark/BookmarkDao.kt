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

    @Query("SELECT * FROM bookmark WHERE ownerBookId = :bookId")
    fun getBookmarks(bookId: Long): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark WHERE ownerBookId = :bookId AND type = \"LAST_READ\"")
    fun getLastRead(bookId: Long): Bookmark?

    @Transaction
    fun upsertLastRead(bookmark: Bookmark) {
        if (bookmark.type == BookmarkType.LAST_READ.name) {
            val lastRead = getLastRead(bookmark.ownerBookId)
            if (lastRead != null) {
                bookmark.bookmarkId = lastRead.bookmarkId
            }
            insert(bookmark)
        }
    }

    @Update
    fun update(bookmark: Bookmark)

    @Delete
    fun delete(bookmark: Bookmark)

    /*@Query("DELETE FROM bookmarks WHERE uri = :uri AND title = :title AND \"index\" = :index")
    fun deleteByIndex(uri: String, title: String, index: Long)

    @Query("DELETE FROM bookmarks")
    fun deleteAll()*/
}
