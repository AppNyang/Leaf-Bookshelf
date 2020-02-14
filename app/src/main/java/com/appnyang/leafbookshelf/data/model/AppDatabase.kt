package com.appnyang.leafbookshelf.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.appnyang.leafbookshelf.data.model.bookmark.Bookmark
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkDao

/**
 * Database class.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-14.
 */
@Database(entities = [Bookmark::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getBookmarkDao(): BookmarkDao

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
