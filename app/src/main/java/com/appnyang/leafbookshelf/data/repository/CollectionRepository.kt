package com.appnyang.leafbookshelf.data.repository

import androidx.lifecycle.LiveData
import com.appnyang.leafbookshelf.data.model.CollectionWithBooks
import com.appnyang.leafbookshelf.data.model.CollectionWithBooksDao
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.data.model.collection.CollectionDao
import kotlinx.coroutines.flow.Flow

/**
 * Collection Repository.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-04-28.
 */
class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val collectionWithBooksDao: CollectionWithBooksDao
) {

    /**
     * Fetch all collections form DB.
     *
     * @return A LiveData list of all book collections.
     */
    fun loadCollections(): LiveData<List<Collection>> = collectionDao.getCollections()

    /**
     * Fetch a collection given id.
     *
     * @return A Collection.
     */
    fun loadCollection(id: Long): LiveData<Collection> = collectionDao.getCollection(id)

    /**
     * Fetch all collections with books form the database.
     *
     * @return A Flow list of CollectionWithBooks if exist or null.
     */
    fun getCollectionsWithBooks(): Flow<List<CollectionWithBooks>?> = collectionWithBooksDao.getCollectionsWithBooks()

    /**
     * Create a new collection to DB.
     *
     * @param collection The collection object to create.
     */
    fun createCollection(collection: Collection) = collectionDao.insert(collection)

    /**
     * Update the collection.
     *
     * @param collection A collection to update.
     */
    fun updateCollection(collection: Collection) = collectionDao.update(collection)

    /**
     * Delete the collection.
     *
     * @param collection A collection to delete.
     */
    fun deleteCollection(collection: Collection) = collectionDao.delete(collection)
}
