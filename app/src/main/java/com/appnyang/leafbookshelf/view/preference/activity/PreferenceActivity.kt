package com.appnyang.leafbookshelf.view.preference.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.view.preference.fragment.PreferenceFragment
import kotlinx.android.synthetic.main.activity_preference.*

class PreferenceActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        setSupportActionBar(toolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferenceContainer, PreferenceFragment())
            .commit()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat?, pref: Preference): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment)
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferenceContainer, fragment)
            .addToBackStack(null)
            .commit()

        return true
    }
}
