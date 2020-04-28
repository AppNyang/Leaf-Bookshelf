package com.appnyang.leafbookshelf.data.model.collection

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.ABORT

/**
 * Collections Data Access Object.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-04-26.
 */
@Dao
interface CollectionDao {

    @Insert(onConflict = ABORT)
    fun insert(collection: Collection)

    @Query("SELECT * FROM collections")
    fun getCollections(): LiveData<List<Collection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    fun getCollection(id: Long): LiveData<Collection>

    @Query("SELECT books FROM collections WHERE id = :id")
    fun getBooks(id: Long): LiveData<List<String>>

    @Update
    fun update(collection: Collection)

    @Delete
    fun delete(collection: Collection)

    @Query("DELETE FROM collections WHERE id = :id")
    fun delete(id: Long)
}
