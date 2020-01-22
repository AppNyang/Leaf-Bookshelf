package com.appnyang.leafbookshelf.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.appnyang.leafbookshelf.util.SingleLiveEvent

/**
 * Main View Model.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class MainViewModel : ViewModel() {
    private val _readText = SingleLiveEvent<Any>()

    val readText: LiveData<Any> = _readText

    fun readText() {
        _readText.call()
    }
}