package com.appnyang.leafbookshelf.view.preference.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.appnyang.leafbookshelf.R
import kotlinx.android.synthetic.main.fragment_open_source_license.*
import kotlinx.android.synthetic.main.fragment_open_source_license.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.properties.Delegates

/**
 * Display Open Source License.
 */
class OpenSourceLicenseFragment : Fragment() {

    private val licenseText = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        licenseText.observe(this, Observer {
            view?.textOpenSourceLicenses?.text = it
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_open_source_license, container, false).apply {
            lifecycleScope.launch(Dispatchers.Default) {
                val apache = async { readApacheLicense() }
                val icon = async { readIconLicense() }

                licenseText.postValue(apache.await() + icon.await())
            }
        }
    }

    private suspend fun readApacheLicense(): String = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        resources.openRawResource(R.raw.apache_license).use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    builder.append(line + "\n")
                    line = reader.readLine()
                }
            }
        }

        builder.toString()
    }

    private suspend fun readIconLicense(): String = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        resources.openRawResource(R.raw.icon_license).use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    builder.append(line + "\n")
                    line = reader.readLine()
                }
            }
        }

        builder.toString()
    }
}
