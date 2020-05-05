package com.appnyang.leafbookshelf.data.model.history

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.appnyang.leafbookshelf.viewmodel.RecentFile

/**
 * History Data Access Object.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-19.
 */
@Dao
interface HistoryDao {
    @Insert(onConflict = REPLACE)
    fun insert(history: History)

    @Query("SELECT * FROM history WHERE uri = :uri LIMIT 1")
    fun getHistory(uri: String): History?

    @Query("SELECT * FROM history ORDER BY lastOpen DESC")
    fun getHistory(): LiveData<List<History>>

    @Query("SELECT * FROM history ORDER BY lastOpen DESC LIMIT 6")
    fun getRecentHistory(): LiveData<List<RecentFile>>

    @Transaction
    fun upsert(history: History) {
        val old = getHistory(history.uri)
        if (old != null) {
            history.id = old.id
        }
        insert(history)
    }

    @Query("DELETE FROM history WHERE uri = :uri")
    fun delete(uri: String)

    @Query("DELETE FROM history")
    fun deleteAll()
}
