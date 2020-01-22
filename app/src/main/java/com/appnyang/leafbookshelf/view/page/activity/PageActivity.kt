package com.appnyang.leafbookshelf.view.page.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.ActivityPageBinding
import com.appnyang.leafbookshelf.viewmodel.PageViewModel
import kotlinx.android.synthetic.main.activity_page.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PageActivity : AppCompatActivity() {

    private val viewModel by viewModel<PageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityPageBinding>(this, R.layout.activity_page).apply {
            viewModel = this@PageActivity.viewModel
            lifecycleOwner = this@PageActivity
        }

        viewModel.rawText.observe(this, Observer {
            lifecycleScope.launch {
                pagerView.buildPagedText(it)
            }
        })

        openBook()
    }

    private fun openBook() {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"

            startActivityForResult(this, PICK_TEXT_FILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_TEXT_FILE && resultCode == Activity.RESULT_OK) {
            // TODO: Grant persist permission(https://developer.android.com/training/data-storage/shared/documents-files#persist-permissions)
            data?.data?.also { viewModel.readBookFromUri(it, applicationContext.contentResolver) }
        }
    }

    companion object {
        const val PICK_TEXT_FILE = 1000
    }
}
