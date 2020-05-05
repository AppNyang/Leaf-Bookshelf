package com.appnyang.leafbookshelf.view.collection.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.ActivityCollectionBinding
import com.appnyang.leafbookshelf.viewmodel.CollectionViewModel
import kotlinx.android.synthetic.main.activity_collection.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CollectionActivity : AppCompatActivity() {

    private val viewModel by viewModel<CollectionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityCollectionBinding>(this, R.layout.activity_collection).apply {
            viewModel = this@CollectionActivity.viewModel
            lifecycleOwner = this@CollectionActivity
        }

        viewModel.collection.observe(this, Observer {
            // If it is null, it means the collection has been deleted.
            if (it == null) {
                finish()
            }
        })


        setSupportActionBar(toolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        readCollection()
    }

    /**
     * Read collection from DB.
     */
    private fun readCollection() {
        val collectionId = intent.getLongExtra(KEY_COLLECTION_ID, -1L)

        // CollectionId should greater than zero.
        if (collectionId < 0) {
            finish()
        }
        else {
            viewModel.loadCollection(collectionId)
        }
    }

    override fun onPause() {
        super.onPause()

        viewModel.updateCollectionTitle(textCollectionName.editText?.text.toString())
    }

    companion object {
        const val KEY_COLLECTION_ID = "KEY_COLLECTION_ID"
    }
}
