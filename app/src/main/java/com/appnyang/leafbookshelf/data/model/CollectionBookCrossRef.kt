package com.appnyang.leafbookshelf.data.model

import androidx.lifecycle.LiveData
import androidx.room.*
import com.appnyang.leafbookshelf.data.model.book.Book
import com.appnyang.leafbookshelf.data.model.collection.Collection

/**
 * Relationship Collection and Book.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-04.
 */

@Entity(primaryKeys = ["collectionId", "bookId"])
data class CollectionBookCrossRef(
    val collectionId: Long,
    val bookId: Long
)

data class CollectionWithBooks(
    @Embedded val collection: Collection,
    @Relation(
        parentColumn = "collectionId",
        entityColumn = "bookId",
        associateBy = Junction(CollectionBookCrossRef::class)
    )
    val books: List<Book>
)

@Dao
interface CollectionWithBooksDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(collectionBookCrossRef: CollectionBookCrossRef)

    @Transaction
    @Query("SELECT * FROM collection WHERE collectionId = :id")
    fun getCollectionWithBooks(id: Long): LiveData<CollectionWithBooks>
}
