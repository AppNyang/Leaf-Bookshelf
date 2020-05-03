package com.appnyang.leafbookshelf.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appnyang.leafbookshelf.data.model.book.Book
import com.appnyang.leafbookshelf.data.model.book.BookDao
import com.appnyang.leafbookshelf.data.model.bookmark.Bookmark
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkDao
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.data.model.collection.CollectionDao
import com.appnyang.leafbookshelf.data.model.history.History
import com.appnyang.leafbookshelf.data.model.history.HistoryDao
import com.appnyang.leafbookshelf.data.model.user.User
import com.appnyang.leafbookshelf.data.model.user.UserDao
import com.appnyang.leafbookshelf.util.RoomTypeConverter

/**
 * Database class.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-14.
 */
@Database(entities = [User::class, Collection::class, Book::class, Bookmark::class, CollectionBookCrossRef::class, History::class], version = 1, exportSchema = false)
@TypeConverters(RoomTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao
    abstract fun getCollectionDao(): CollectionDao
    abstract fun getBookDao(): BookDao
    abstract fun getBookmarkDao(): BookmarkDao
    abstract fun getCollectionWithBooksDao(): CollectionWithBooksDao
    abstract fun getHistoryDao(): HistoryDao

    companion object {
        private const val DB_NAME = "db_bookshelf"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME).build()
    }
}
