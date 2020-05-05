package com.appnyang.leafbookshelf.view.book.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.ActivityBookBinding
import com.appnyang.leafbookshelf.viewmodel.BookViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookActivity : AppCompatActivity() {

    private val viewModel by viewModel<BookViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityBookBinding>(this, R.layout.activity_book).apply {
            viewModel = this@BookActivity.viewModel
            lifecycleOwner = this@BookActivity
        }
    }
}
