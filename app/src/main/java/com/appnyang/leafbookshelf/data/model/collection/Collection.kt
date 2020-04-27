package com.appnyang.leafbookshelf.data.model.collection

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Collection Entity.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-04-25.
 */
@Entity(tableName = "collections", indices = [Index(value= ["title"], unique = true)])
data class Collection(
    val title: String,
    val color: Int,
    val books: List<String>
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
