package com.appnyang.leafbookshelf.data.model.bookmark

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.appnyang.leafbookshelf.data.model.book.Book
import org.joda.time.DateTime

/**
 * Bookmark entity.
 * Bookmark has one-to-many relationship with Book.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-14.
 */
@Entity(foreignKeys = [ForeignKey(entity = Book::class, parentColumns = ["bookId"], childColumns = ["ownerBookId"], onDelete = ForeignKey.CASCADE)])
data class Bookmark(
    val ownerBookId: Long,
    val displayName: String,
    val index: Long,
    val type: String,
    val createAt: DateTime
) {
    @PrimaryKey(autoGenerate = true)
    var bookmarkId: Long = 0
}

enum class BookmarkType {
    CUSTOM,
    AUTO_GENERATED,
    LAST_READ
}
