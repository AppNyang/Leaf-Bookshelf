package com.appnyang.leafbookshelf.data.model.collection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

/**
 * Collections Data Access Object.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-04-26.
 */
@Dao
interface CollectionDao {

    @Insert(onConflict = REPLACE)
    fun insert(collection: Collection)

    @Query("SELECT * FROM collections")
    fun getCollections(): List<Collection>

    @Query("SELECT books FROM collections WHERE id = :id")
    fun getBooks(id: Long): List<String>

    @Query("DELETE FROM collections WHERE id = :id")
    fun delete(id: Long)
}
