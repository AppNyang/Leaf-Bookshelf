package com.appnyang.leafbookshelf.view.main.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.ActivityMainBinding
import com.appnyang.leafbookshelf.view.page.activity.PageActivity
import com.appnyang.leafbookshelf.viewmodel.MainViewModel
import com.google.android.material.appbar.AppBarLayout
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            viewModel = this@MainActivity.viewModel
            lifecycleOwner = this@MainActivity
        }

        toolBar.title = resources.getString(R.string.app_name)

        initStatusBar()
        // Set top margin of AppBar to avoid overlapping status bar.
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { _, insets ->
            toolBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            insets.consumeSystemWindowInsets()
        }

        viewModel.readText.observe(this, Observer {
            startActivity(Intent(this, PageActivity::class.java))
        })
        initFab()
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

        // Change the color of status icons to dark when the app bar is collapsed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                window.decorView.systemUiVisibility = if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                else {
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            })
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
                    startActivity(Intent(this, PageActivity::class.java))
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
}
