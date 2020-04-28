package com.appnyang.leafbookshelf.view.bookshelf.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.databinding.ActivityBookshelfBinding
import com.appnyang.leafbookshelf.view.page.activity.PageActivity
import com.appnyang.leafbookshelf.viewmodel.BookshelfViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_bookshelf.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookshelfActivity : AppCompatActivity() {

    private val viewModel by viewModel<BookshelfViewModel>()

    private val bottomAddDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(this).apply {
            setContentView(R.layout.dialog_bookshelf_add)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityBookshelfBinding>(this, R.layout.activity_bookshelf).apply {
            viewModel = this@BookshelfActivity.viewModel
            lifecycleOwner = this@BookshelfActivity
        }

        subscribeObservers()

        setSupportActionBar(toolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerHistories.layoutManager = GridLayoutManager(this, 3)
    }

    /**
     * Subscribe observers of ViewModel.
     */
    private fun subscribeObservers() {
        viewModel.historyClicked.observe(this, Observer {
            startActivity(Intent(this, PageActivity::class.java).apply {
                putExtra(PageActivity.KEY_FILE_URI, Uri.parse(it.first))
                putExtra(PageActivity.KEY_CHAR_INDEX, it.second)
            })
        })

        viewModel.collections.observe(this, Observer { collections ->
            updateTabs(collections)
        })
    }

    /**
     * Build tabLayout with given book collection list.
     *
     * @param collections A list of book collections.
     */
    private fun updateTabs(collections: List<Collection>) {
        tabLayout.removeAllTabs()

        // Add "ALL" tab.
        tabLayout.newTab().let { tab ->
            tab.text = resources.getString(R.string.bookshelf_all)
            tab.tag = -1L
            tabLayout.addTab(tab)
        }

        collections.forEach { collection ->
            tabLayout.newTab().let { tab ->
                tab.text = collection.title
                tab.tag = collection.id
                tabLayout.addTab(tab)
            }
        }
    }

    /**
     * Called when buttons are clicked.
     *
     * @param view Clicked button.
     */
    fun onButtonsClicked(view: View) {
        when (view.id) {
            R.id.buttonAdd -> { bottomAddDialog.show() }
            R.id.buttonEdit -> {}
            R.id.buttonAddNewBook -> {}
            R.id.buttonAddNewCollection -> {
                bottomAddDialog.dismiss()

                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.bookshelf_title_new_collection))
                    .setView(R.layout.dialog_new_collection)
                    .setNeutralButton(resources.getString(R.string.button_close)) { dialog, which ->
                        // Respond to neutral button press
                    }
                    .setPositiveButton(resources.getString(R.string.button_add)) { dialog, which ->
                        // Respond to positive button press
                    }
                    .show()
            }
        }
    }
}
