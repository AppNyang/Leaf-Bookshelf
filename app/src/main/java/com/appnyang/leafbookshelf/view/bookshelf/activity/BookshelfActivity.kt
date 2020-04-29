package com.appnyang.leafbookshelf.view.bookshelf.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.databinding.ActivityBookshelfBinding
import com.appnyang.leafbookshelf.view.collection.activity.CollectionActivity
import com.appnyang.leafbookshelf.view.page.activity.PageActivity
import com.appnyang.leafbookshelf.viewmodel.BookshelfViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_bookshelf.*
import kotlinx.android.synthetic.main.dialog_new_collection.view.*
import kotlinx.android.synthetic.main.layout_book_item.view.*
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

        BottomSheetBehavior.from(bottomSheetMenu).addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    viewModel.setState(BookshelfViewModel.State.Default)
                }
            }
        })

        setSupportActionBar(toolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

        // When changed stat of the activity.
        viewModel.state.observe(this, Observer {
            when (it!!) {
                BookshelfViewModel.State.Default -> {
                    BottomSheetBehavior.from(bottomSheetMenu).state = BottomSheetBehavior.STATE_HIDDEN

                    recyclerHistories.children.forEach { child ->
                        child.cardBookItem.isChecked = false
                    }
                }
                BookshelfViewModel.State.Checked -> { BottomSheetBehavior.from(bottomSheetMenu).state = BottomSheetBehavior.STATE_EXPANDED }
            }
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
     * When the Back button is pressed when State.Checked,
     * do not finish the activity and set state to State.Default.
     */
    override fun onBackPressed() {
        if (viewModel.state.value == BookshelfViewModel.State.Checked) {
            viewModel.setState(BookshelfViewModel.State.Default)
        }
        else {
            super.onBackPressed()
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
            R.id.buttonEdit -> {
                startActivity(Intent(this, CollectionActivity::class.java).apply {
                    putExtra(CollectionActivity.KEY_COLLECTION_ID, tabLayout.getTabAt(tabLayout.selectedTabPosition)?.tag as Long)
                })
            }
            R.id.buttonAddNewBook -> {}
            R.id.buttonAddNewCollection -> {
                bottomAddDialog.dismiss()

                val layout = layoutInflater.inflate(R.layout.dialog_new_collection, null)
                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.bookshelf_title_new_collection))
                    .setView(layout)
                    .setNeutralButton(resources.getString(R.string.button_close)) { _, _ -> }
                    .setPositiveButton(resources.getString(R.string.button_add)) { _, _ ->
                        val title = layout.textCollectionName.editText?.text.toString()
                        if (title.isNotBlank()) {
                            viewModel.createCollection(
                                Collection(title, Color.parseColor("#64D992"), mutableListOf())
                            )
                        }
                    }
                    .show()
            }
        }
    }
}
