package com.appnyang.leafbookshelf.view.page.activity

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.appnyang.leafbookshelf.BuildConfig
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkType
import com.appnyang.leafbookshelf.databinding.ActivityPageBinding
import com.appnyang.leafbookshelf.util.afterMeasured
import com.appnyang.leafbookshelf.util.transformer.DepthPageTransformer
import com.appnyang.leafbookshelf.view.book.activity.BookActivity
import com.appnyang.leafbookshelf.view.page.fragment.TextAppearancePreferenceFragment
import com.appnyang.leafbookshelf.viewmodel.PageViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kotlinx.android.synthetic.main.activity_page.*
import kotlinx.android.synthetic.main.dialog_add_bookmark.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PageActivity : AppCompatActivity() {

    private val viewModel by viewModel<PageViewModel>()

    private var autoReadJob: Job = Job()

    private lateinit var interstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideStatusBar()

        DataBindingUtil.setContentView<ActivityPageBinding>(this, R.layout.activity_page).apply {
            viewModel = this@PageActivity.viewModel
            lifecycleOwner = this@PageActivity
        }

        // Read preference values.
        readPreferences()

        // Initialize settings fragment.
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferenceContainer, TextAppearancePreferenceFragment())
            .commit()

        registerSystemUiChangeListener()

        // Initialize ViewPager2.
        pager.setPageTransformer(DepthPageTransformer())

        // Open files depends on file type.
        pager.afterMeasured {
            openBook()
        }

        subscribeObservers()

        // Init interstitial ads.
        interstitialAd = InterstitialAd(this).apply {
            adUnitId = BuildConfig.AFTER_READING_PROMO_ID
            loadAd(AdRequest.Builder().build())
        }
    }

    /**
     * On focused to the window, hide status bar.
     *
     * @param hasFocus true when this window is focused.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideStatusBar()
        }
    }

    /**
     * When the system ui is appear, hide it after 3 seconds.
     */
    private fun registerSystemUiChangeListener() {
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if (it and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                // System bars are visible.
                Handler().postDelayed({
                    hideStatusBar()
                }, 3000)
            }
        }
    }

    override fun onBackPressed() {
        if (viewModel.isAnyMenuOpened()) {
            viewModel.displayMenu()
        }
        else {
            // Before exit this activity.
            if (interstitialAd.isLoaded) {
                interstitialAd.show()
            }

            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()

        if (viewModel.bookWithBookmarks.value != null) {
            // Save a bookmark.
            viewModel.bookmarkCurrentPage(getString(R.string.last_read), BookmarkType.LAST_READ)

            viewModel.updateBookBeforeClose()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.setLastOpenedToNow()
    }

    /**
     * Read preferences values and set it to ViewModel.
     */
    private fun readPreferences() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val fontFamily = sharedPreferences.getString(getString(R.string.pref_key_font), "").let {
            when (it) {
                "bon_gothic" -> ResourcesCompat.getFont(this@PageActivity, R.font.noto_sans_cjk_r)!!
                "nanum_square" -> ResourcesCompat.getFont(this@PageActivity, R.font.nanum_square)!!
                "nanum_barun_gothic" -> ResourcesCompat.getFont(this@PageActivity, R.font.nanum_barun_gothic)!!
                else -> ResourcesCompat.getFont(this@PageActivity, R.font.noto_sans_cjk_r)!!
            }
        }

        val fontSize = sharedPreferences.getString(getString(R.string.pref_key_font_size), "18").let {
            it?.toFloat() ?: 18f
        }
        val fontColor = sharedPreferences.getString(getString(R.string.pref_key_font_color), "#2A2A2A").let {
            Color.parseColor(it)
        }
        val lineSpacing = sharedPreferences.getString(getString(R.string.pref_key_line_spacing), "1.8").let {
            it?.toFloat() ?: 1.8f
        }

        viewModel.updatePageTextAppearance(PageViewModel.PageTextAppearance(fontFamily, fontSize, fontColor, lineSpacing))
    }

    /**
     * Open files depends on file type.
     */
    private fun openBook() {
        intent.extras?.getParcelable<Uri>(KEY_FILE_URI)?.let {
            val layoutParam = buildLayoutParam()
            val charIndex = intent.extras?.getLong(KEY_CHAR_INDEX, -1) ?: -1

            viewModel.readBookFromUri(it, applicationContext.contentResolver, layoutParam, charIndex)
        }
    }

    /**
     * Build a layout param.
     *
     * @return A StaticLayoutParam.
     */
    private fun buildLayoutParam() : PageViewModel.StaticLayoutParam {
        val textPaint = TextPaint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            typeface = viewModel.pageTextAppearance.value!!.fontFamily
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, viewModel.pageTextAppearance.value!!.fontSize, resources.displayMetrics)
        }

        return PageViewModel.StaticLayoutParam(pager.width - (2f * resources.getDimension(R.dimen.page_margin)).toInt(),
            pager.height - (2f * resources.getDimension(R.dimen.page_margin)).toInt(),
            textPaint,
            viewModel.pageTextAppearance.value!!.lineSpacing,
            0f,
            false)
    }

    /**
     * Subscribe live data from ViewModel.
     */
    private fun subscribeObservers() {
        viewModel.bookWithBookmarks.observe(this, Observer {
            // If bookWithBookmarks is null, it means the book has been deleted.
            if (it == null) {
                finish()
            }
        })

        // Called when the middle of page is clicked.
        viewModel.showMenu.observe(this, Observer {
            showMenu(it)
        })

        // Called when settings button of top-menu clicked.
        viewModel.showSettings.observe(this, Observer {
            showSettingsMenu(it)
        })

        // Called when the bookmark button of top-menu clicked.
        viewModel.showBookmark.observe(this, Observer {
            showBookmarksMenu(it)
        })

        // Called when TTS chip is clicked.
        viewModel.bTts.observe(this, Observer {
            viewModel.startTtsService(it)
        })

        // Called when Auto chip is clicked.
        viewModel.bAuto.observe(this, Observer {
            runAutoRead(it)
        })

        // Called when bookmarks from database is updated.
        viewModel.bookmarks.observe(this, Observer {
            // Add bookmark chips.
            chipGroupBookmarks.removeAllViews()
            chipGroupAutoGeneratedBookmarks.removeAllViews()

            it.forEach { bookmark ->
                // TODO: Make it work!!
                /*val group: ChipGroup = if (bookmark.type == BookmarkType.CUSTOM.name) { chipGroupBookmarks } else { chipGroupAutoGeneratedBookmarks }
                val chip = layoutInflater.inflate(R.layout.layout_bookmark_chip, group, false) as Chip
                chip.text = bookmark.title
                chip.setTag(R.string.tag_title, bookmark.title)
                chip.setTag(R.string.tag_index, bookmark.index)
                chip.setOnCloseIconClickListener { view ->
                    viewModel.deleteBookmark(view.getTag(R.string.tag_title).toString(), view.tag.toString().toLong())
                }
                chip.setOnClickListener { view ->
                    if (!viewModel.isPaginating.get()) {
                        viewModel.setCurrentPageToTextIndex(view.tag.toString().toLong())
                    }
                }
                group.addView(chip)*/
            }
        })

        // Called when the shared preference has been changed.
        viewModel.sharedPreferenceLiveData.observe(this, Observer {
            readPreferences()

            // TODO: Fix null exception of value!!
            viewModel.readBookFromUri(
                viewModel.bookWithBookmarks.value!!.book.uri,
                applicationContext.contentResolver,
                buildLayoutParam(),
                viewModel.getCurrentTextIndex()
            )
        })
    }

    /**
     * Hide system status bar when focused.
     */
    private fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    /**
     * Run auto-read feature.
     *
     * @param bStart If true, start the auto-read.
     */
    private fun runAutoRead(bStart: Boolean) {
        lifecycleScope.launch(Dispatchers.Default) {
            if (bStart) {
                autoReadJob = launch {
                    val tickerChannel = ticker(delayMillis = 35000)
                    try {
                        for (event in tickerChannel) {
                            if (!viewModel.goToNextPage()) {
                                viewModel.bAuto.postValue(false)
                            }
                        }
                    } finally {
                        tickerChannel.cancel()
                    }
                }
            }
            else {
                if (autoReadJob.isActive) {
                    autoReadJob.cancelAndJoin()
                }
            }
        }
    }

    /**
     * Called when the button of top-menu clicked.
     *
     * @param view Button.
     */
    fun onTopMenuClicked(view: View) {
        when (view.id) {
            R.id.buttonBack -> {
                viewModel.displayMenu()
                onBackPressed()
            }
            R.id.buttonAddBookmark -> {
                viewModel.bookWithBookmarks.value?.let {
                    startActivity(Intent(this, BookActivity::class.java).apply {
                        putExtra(BookActivity.KEY_BOOK_ID, it.book.bookId)
                    })
                }
                /*if (!viewModel.isPaginating.get()) {
                    viewModel.displayMenu()
                    AddBookmarkDialog(viewModel.pagedBook.value?.get(viewModel.currentPage.value ?: 0)?.substring(0..10)?.trim() ?: "", viewModel)
                        .show(supportFragmentManager, "AddBookmark")
                }*/
            }
        }
    }

    /**
     * Show or hide menus based on bShow
     *
     * @param bShow true to show the menus.
     */
    private fun showMenu(bShow: Boolean) {
        if (layoutTopMenu.height != 0 || layoutBottomMenu.height != 0) {
            val animationDuration = 200L

            if (bShow) {
                ObjectAnimator.ofFloat(layoutTopMenu, "translationY", 0f).apply {
                    duration = animationDuration
                    start()
                }
                ObjectAnimator.ofFloat(layoutBottomMenu, "translationY", 0f).apply {
                    duration = animationDuration
                    start()
                }
            } else {
                ObjectAnimator.ofFloat(layoutTopMenu, "translationY", -layoutTopMenu.height.toFloat()).apply {
                    duration = animationDuration
                    start()
                }
                ObjectAnimator.ofFloat(layoutBottomMenu, "translationY", layoutBottomMenu.height.toFloat()).apply {
                    duration = animationDuration
                    start()
                }
            }
        }
    }

    /**
     * Show or hide bookmarks menu.
     *
     * @param bShow true to show the bookmarks menu.
     */
    private fun showBookmarksMenu(bShow: Boolean) {
        if (layoutBookmarkMenu.height != 0) {
            val animationDuration = 200L

            if (bShow) {
                ObjectAnimator.ofFloat(layoutBookmarkMenu, "translationY", 0f).apply {
                    duration = animationDuration
                    start()
                }
            }
            else {
                ObjectAnimator.ofFloat(layoutBookmarkMenu, "translationY", layoutBookmarkMenu.height.toFloat() * 1.5f).apply {
                    duration = animationDuration
                    start()
                }
            }
        }
    }

    /**
     * Show or hide settings menu.
     *
     * @param bShow true to show the settings menu.
     */
    private fun showSettingsMenu(bShow: Boolean) {
        if (layoutSettingsMenu.height != 0) {
            val animationDuration = 200L

            if (bShow) {
                ObjectAnimator.ofFloat(layoutSettingsMenu, "translationY", 0f).apply {
                    duration = animationDuration
                    start()
                }
            }
            else {
                ObjectAnimator.ofFloat(layoutSettingsMenu, "translationY", -layoutSettingsMenu.height.toFloat()).apply {
                    duration = animationDuration
                    start()
                }
            }
        }
    }

    companion object {
        const val KEY_FILE_URI = "KEY_FILE_URI"
        const val KEY_CHAR_INDEX = "KEY_CHAR_INDEX"

        const val REQUEST_NOTIFICATION_CLICK = 5000
    }
}

class AddBookmarkDialog(private val title: String, private val viewModel: PageViewModel) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_bookmark, null)
            view.editBookmarkTitle.setText(title)

            val builder = AlertDialog.Builder(it)
                .setView(view)
                .setTitle(R.string.title_add_bookmark)
                .setIcon(R.drawable.ic_bookmark)
                .setPositiveButton(R.string.button_add) { _, _ ->
                    viewModel.bookmarkCurrentPage(view.editBookmarkTitle.text.toString())
                }
                .setNegativeButton(R.string.button_cancel) { _, _ ->
                    dialog?.cancel()
                }

            builder.create()
        } ?: throw IllegalStateException()
    }
}
