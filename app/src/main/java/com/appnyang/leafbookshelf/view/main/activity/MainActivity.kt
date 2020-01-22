package com.appnyang.leafbookshelf.view.main.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.ActivityMainBinding
import com.appnyang.leafbookshelf.view.page.activity.PageActivity
import com.appnyang.leafbookshelf.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            viewModel = this@MainActivity.viewModel
            lifecycleOwner = this@MainActivity
        }

        viewModel.readText.observe(this, Observer {
            startActivity(Intent(this, PageActivity::class.java))
        })
    }
}
