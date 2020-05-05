package com.appnyang.leafbookshelf.view.book.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
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

        subscribeObservers()

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

    private fun subscribeObservers() {
        viewModel.bookWithBookmarks.observe(this, Observer {
            // If it is null, it means the book has been deleted.
            if (it == null) {
                finish()
            }
        })

        viewModel.buttonClicked.observe(this, Observer {
            when (it.id) {
                R.id.changeCover -> {
                    // TODO: Change handle media directly.
                    Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"

                        startActivityForResult(this, PICK_COVER)
                    }
                }
                R.id.manageChange -> {

                }
            }
        })
    }

    /**
     * Get the file uri.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_COVER && resultCode == Activity.RESULT_OK) {
            data?.data?.also {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                // Take persist permissions to access the file across device restarts.
                applicationContext.contentResolver.takePersistableUriPermission(it, takeFlags)

                viewModel.setBookCover(it)
            }
        }
    }

    companion object {
        const val KEY_BOOK_ID = "KEY_BOOK_ID"
        const val PICK_COVER = 1000
    }
}
