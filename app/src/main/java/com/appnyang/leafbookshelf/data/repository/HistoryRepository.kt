package com.appnyang.leafbookshelf.data.repository

import androidx.lifecycle.LiveData
import com.appnyang.leafbookshelf.data.model.history.History
import com.appnyang.leafbookshelf.data.model.history.HistoryDao

/**
 * History Repository.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-20.
 */
class HistoryRepository(private val historyDao: HistoryDao) {

    /**
     * Fetch all history from the database.
     *
     * @return A list of all history.
     */
    fun loadHistory(): LiveData<List<History>> = historyDao.getHistory()

    /**
     * Fetch a history from the database.
     *
     * @return A History.
     */
    fun loadHistory(uri: String): History? = historyDao.getHistory(uri)

    /**
     * Upsert a history.
     *
     * @param history A History.
     */
    fun saveHistory(history: History) = historyDao.upsert(history)
}
