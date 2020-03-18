package com.appnyang.leafbookshelf.view.preference.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.appnyang.leafbookshelf.R

/**
 * Preference fragment.
 */
class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Initialize contact us preference.
        findPreference<Preference>(getString(R.string.pref_key_contact_us))?.setOnPreferenceClickListener {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "devnyang@appnyang.com", null)), getString(R.string.send_email)))
            true
        }
    }
}
