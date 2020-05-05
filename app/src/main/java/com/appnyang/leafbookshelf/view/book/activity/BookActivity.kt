package com.appnyang.leafbookshelf.view.book.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.ActivityBookBinding
import com.appnyang.leafbookshelf.viewmodel.BookViewModel
import kotlinx.android.synthetic.main.activity_book.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookActivity : AppCompatActivity() {

    private val viewModel by viewModel<BookViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityBookBinding>(this, R.layout.activity_book).apply {
            viewModel = this@BookActivity.viewModel
            lifecycleOwner = this@BookActivity
        }

        setSupportActionBar(toolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        readBook()
    }

    /**
     * Read the book from the database.
     */
    private fun readBook() {
        val bookId = intent.getLongExtra(KEY_BOOK_ID, -1L)

        // bookId should greater than zero.
        if (bookId < 0) {
            finish()
        }
        else {
            viewModel.loadBook(bookId)
        }
    }

    companion object {
        const val KEY_BOOK_ID = "KEY_BOOK_ID"
    }
}
