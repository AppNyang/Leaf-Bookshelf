package com.appnyang.leafbookshelf.data.repository

import androidx.lifecycle.LiveData
import com.appnyang.leafbookshelf.data.model.history.History
import com.appnyang.leafbookshelf.data.model.history.HistoryDao
import com.appnyang.leafbookshelf.viewmodel.RecentFile

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
}
