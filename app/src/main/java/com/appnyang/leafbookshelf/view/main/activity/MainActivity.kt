package com.appnyang.leafbookshelf.view.main.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.MainThread
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.appnyang.leafbookshelf.BuildConfig
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkType
import com.appnyang.leafbookshelf.databinding.ActivityMainBinding
import com.appnyang.leafbookshelf.view.bookshelf.activity.BookshelfActivity
import com.appnyang.leafbookshelf.view.page.activity.PageActivity
import com.appnyang.leafbookshelf.view.preference.activity.PreferenceActivity
import com.appnyang.leafbookshelf.viewmodel.MainViewModel
import com.appnyang.leafbookshelf.viewmodel.RecentPromo
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()

    private lateinit var adLoader: AdLoader
    private val ads = mutableListOf<UnifiedNativeAd>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            viewModel = this@MainActivity.viewModel
            lifecycleOwner = this@MainActivity
        }

        initStatusBar()

        subscribeObservers()

        // Set top margin of AppBar to avoid overlapping status bar.
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { _, insets ->
            toolBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            insets.consumeSystemWindowInsets()
        }

        initToolBar()

        initFab()

        // To reduce stuttering, call requestAds in a new coroutine job.
        // Keep in mind that it should run on the main thread.
        lifecycleScope.launch { requestAds() }
    }

    /**
     * Make status bar transparent and set color of icons based on scroll position.
     */
    private fun initStatusBar() {
        // Make status bar transparent and overlap contents below.
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        // Change the color of status icons and navigation icon to dark when the app bar is collapsed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                window.decorView.systemUiVisibility = if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                    toolBar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.lightGray))

                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                else {
                    toolBar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))

                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            })
        }
    }

    private fun subscribeObservers() {
        viewModel.recentFiles.observe(this, Observer { recentFiles ->
            showEmptyBookshelf(recentFiles.isEmpty())
            if (!adLoader.isLoading && recentFiles.none { it is RecentPromo }) {
                requestAds()
            }
        })

        viewModel.bookmarks.observe(this, Observer {
            chipGroupBookmarks.removeAllViews()
            synchronized(this) {
                it.asSequence()
                    .filter { bookmark -> bookmark.type == BookmarkType.CUSTOM.name }
                    .forEach { bookmark ->
                        val chip = layoutInflater.inflate(R.layout.layout_bookmark_chip, chipGroupBookmarks, false) as Chip
                        chip.text = bookmark.title
                        chip.setTag(R.string.tag_uri, bookmark.uri)
                        chip.setTag(R.string.tag_title, bookmark.title)
                        chip.setTag(R.string.tag_index, bookmark.index)
                        chip.setOnCloseIconClickListener { view ->
                            viewModel.deleteBookmark(view.getTag(R.string.tag_uri).toString(), view.getTag(R.string.tag_title).toString(), view.getTag(R.string.tag_index).toString().toLong())
                        }
                        chip.setOnClickListener { view ->
                            openPageActivity(Uri.parse(view.getTag(R.string.tag_uri).toString()), view.getTag(R.string.tag_index).toString().toLong())
                        }
                        chipGroupBookmarks.addView(chip)
                    }
            }
        })

        viewModel.historyClicked.observe(this, Observer {
            openPageActivity(Uri.parse(it.first), it.second)
        })
    }

    /**
     * Initialize ToolBar and Navigation Drawer.
     */
    private fun initToolBar() {
        setSupportActionBar(toolBar)
        toolBar.title = resources.getString(R.string.app_name)

        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_hamburger)
            setDisplayHomeAsUpEnabled(true)
        }

        ActionBarDrawerToggle(this, drawer, toolBar, R.string.drawer_open, R.string.drawer_close).let {
            drawer.addDrawerListener(it)
        }

        navigationView.setNavigationItemSelectedListener {
            lifecycleScope.launch(Dispatchers.Default) {
                delay(300)
                when (it.itemId) {
                    R.id.menu_my_bookshelf -> {
                        startActivity(Intent(this@MainActivity, BookshelfActivity::class.java))
                    }
                    //R.id.menu_bookmarks -> {}
                    //R.id.menu_stats -> {}
                    R.id.menu_settings -> {
                        startActivity(Intent(this@MainActivity, PreferenceActivity::class.java))
                    }
                }
            }

            drawer.closeDrawer(navigationView)
            true
        }
    }

    /**
     * Initialize the Floating Action Button.
     */
    private fun initFab() {
        dialFab.addAllActionItems(buildFabItems())

        // Add the fab click listener.
        dialFab.setOnActionSelectedListener { item ->
            when (item.id) {
                R.id.fab_folder -> {
                    openFromStorage()
                }
                R.id.fab_google_drive -> {}
                R.id.fab_dropbox -> {}
            }

            dialFab.close()

            true
        }
    }

    /**
     * Return the list of sub-items of fab.
     *
     * @return List of sub-items of fab.
     */
    private fun buildFabItems() = listOf<SpeedDialActionItem>(
        SpeedDialActionItem.Builder(R.id.fab_folder, R.drawable.ic_folder)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.lightLeaf, theme))
            .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
            .create(),
        SpeedDialActionItem.Builder(R.id.fab_google_drive, R.drawable.ic_google_drive)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.lightLeaf, theme))
            .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
            .create(),
        SpeedDialActionItem.Builder(R.id.fab_dropbox, R.drawable.ic_dropbox)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.lightLeaf, theme))
            .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
            .create()
    )

    /**
     * Request native ads.
     */
    @MainThread
    private fun requestAds() {
        ads.clear()

        adLoader = AdLoader.Builder(this, BuildConfig.RECENT_FILE_PROMO_ID)
            .forUnifiedNativeAd {
                ads.add(it)

                if (!adLoader.isLoading) {
                    ads
                        .map { ad -> RecentPromo(ad) }
                        .let { promos -> viewModel.recentFilePromos.value = promos }
                }
            }
            .build()

        adLoader.loadAds(AdRequest.Builder().build(), 2)
    }

    /**
     * Show file picker.
     */
    private fun openFromStorage() {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"

            startActivityForResult(this, PICK_FILE_STORAGE)
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(navigationView)) {
            drawer.closeDrawers()
        }
        else {
            super.onBackPressed()
        }
    }

    /**
     * Get the file and pass it to PageActivity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_STORAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.also {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                // Take persist permissions to access the file across device restarts.
                applicationContext.contentResolver.takePersistableUriPermission(it, takeFlags)

                openPageActivity(it)
            }
        }
    }

    /**
     * Show empty bookshelf message.
     *
     * @param bShow true to show empty bookshelf message.
     */
    private fun showEmptyBookshelf(bShow: Boolean) {
        if (bShow) {
            textEmpty.visibility = View.VISIBLE
            layoutContainer.visibility = View.GONE
        }
        else {
            textEmpty.visibility = View.GONE
            layoutContainer.visibility = View.VISIBLE
        }
    }

    /**
     * Open a PageActivity with uri.
     *
     * @param uri File uri.
     * @param charIndex Index.
     */
    private fun openPageActivity(uri: Uri, charIndex: Long = 0) {
        startActivity(Intent(this, PageActivity::class.java).apply {
            putExtra(PageActivity.KEY_FILE_URI, uri)
            putExtra(PageActivity.KEY_CHAR_INDEX, charIndex)
        })
    }

    companion object {
        const val PICK_FILE_STORAGE = 1000
    }
}
