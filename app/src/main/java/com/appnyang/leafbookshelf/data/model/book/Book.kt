package com.appnyang.leafbookshelf.data.model.book

import android.net.Uri
import androidx.room.*
import com.appnyang.leafbookshelf.data.model.bookmark.Bookmark
import org.joda.time.DateTime

/**
 * Book entity.
 * Book has many-to-many relationship with Collection.
 * Book has one-to-many relationship with Bookmarks
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-04.
 */
@Entity(indices = [Index(value = ["uri"], unique = true)])
data class Book(
    var uri: Uri,
    var displayName: String,
    var coverUri: Uri,
    var quote: String,
    var readTime: Int,  // Read time in minute.
    var lastOpenedAt: DateTime
) {
    @PrimaryKey(autoGenerate = true)
    var bookId: Long = 0
    var readingProgress: Float = 0f
}

data class BookWithBookmarks(
    @Embedded val book: Book,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "ownerBookId"
    )
    val bookmarks: List<Bookmark>
)
