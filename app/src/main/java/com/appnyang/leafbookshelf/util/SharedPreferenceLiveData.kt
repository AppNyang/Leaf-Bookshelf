package com.appnyang.leafbookshelf.util

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

/**
 * A LiveData for notify shared preferences has changed.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-03-19.
 */
class SharedPreferenceLiveData(private val sharedPreferences: SharedPreferences)
    : LiveData<Pair<String, String>>() {

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
        value = Pair(key, preferences?.getString(key, "") ?: "")
    }

    override fun onActive() {
        super.onActive()
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
