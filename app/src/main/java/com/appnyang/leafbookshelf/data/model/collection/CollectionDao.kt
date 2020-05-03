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
    fun insert(collection: Collection): Long

    @Query("SELECT * FROM collection")
    fun getCollections(): LiveData<List<Collection>>

    @Query("SELECT * FROM collection WHERE collectionId = :id")
    fun getCollection(id: Long): LiveData<Collection>

    @Update
    fun update(collection: Collection)

    @Delete
    fun delete(collection: Collection)
}
