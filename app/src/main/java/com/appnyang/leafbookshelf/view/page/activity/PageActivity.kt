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
    }
}
