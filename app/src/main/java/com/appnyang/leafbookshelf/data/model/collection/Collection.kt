package com.appnyang.leafbookshelf.data.model.collection

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Collection entity.
 * Collection has one-to-many relationship with User.
 * Collection has many-to-many relationship with Book.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-04-25.
 */
@Entity(indices = [Index(value= ["title"], unique = true)])
data class Collection(
    val ownerId: Long,
    var title: String,
    var color: Int
) {
    @PrimaryKey(autoGenerate = true)
    var collectionId: Long = 0
}
