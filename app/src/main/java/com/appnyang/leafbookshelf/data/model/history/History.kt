package com.appnyang.leafbookshelf.data.model.history

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * History Entity.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-19.
 */
@Entity(tableName = "history", indices = [Index(value= ["uri"], unique = true)])
data class History(
    val uri: String,
    val title: String,
    val readTime: Int,  // Read time in minute.
    val lastOpen: String,
    var quote: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var cover: String = ""
}
