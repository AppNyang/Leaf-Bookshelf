package com.appnyang.leafbookshelf.view.page.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.appnyang.leafbookshelf.R

/**
 * Text appearance preference fragment.
 */
class TextAppearancePreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.text_appearance_preferences, rootKey)
    }
}
