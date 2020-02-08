package com.appnyang.leafbookshelf.view.page.activity

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.ActivityPageBinding
import com.appnyang.leafbookshelf.util.afterMeasured
import com.appnyang.leafbookshelf.util.transformer.DepthPageTransformer
import com.appnyang.leafbookshelf.view.page.fragment.PageFragment
import com.appnyang.leafbookshelf.viewmodel.PageViewModel
import kotlinx.android.synthetic.main.activity_page.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PageActivity : AppCompatActivity() {

    private val viewModel by viewModel<PageViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideStatusBar()

        DataBindingUtil.setContentView<ActivityPageBinding>(this, R.layout.activity_page).apply {
            viewModel = this@PageActivity.viewModel
            lifecycleOwner = this@PageActivity
        }

        registerSystemUiChangeListener()

        // Initialize ViewPager2.
        pager.setPageTransformer(DepthPageTransformer())
        pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.currentPage.value = position
            }
        })

        // TODO: Handle the screen orientation changes.
        // Open files depends on file type.
        if (savedInstanceState == null) {
            textPainter.afterMeasured {
                openBook()
            }
        }

        subscribeObservers()
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

    /**
     * Subscribe live data from ViewModel.
     */
    private fun subscribeObservers() {
        // Called after read the raw file in chunk.
        viewModel.chunkedText.observe(this, Observer {
            lifecycleScope.launch {
                viewModel.paginateBook(
                    PageViewModel.StaticLayoutParam(textPainter.width,
                        textPainter.height,
                        textPainter.paint,
                        textPainter.lineSpacingMultiplier,
                        textPainter.lineSpacingExtra,
                        textPainter.includeFontPadding)
                )
            }
        })

        // Called when the first chunk is paginated.
        viewModel.pagedBook.observe(this, Observer {
            // Setup ViewPager.
            pager.adapter = TextPagerAdapter(it)

            textPages.text = getPageCountString()
            seekPages.max = it.size - 1
        })

        // Called rest of chunks is paginated.
        viewModel.chunkPaged.observe(this, Observer {
            pager.adapter?.notifyDataSetChanged()

            textPages.text = getPageCountString()
            seekPages.max = viewModel.pagedBook.value!!.size - 1
        })

        // Called the user has navigated to the page.
        viewModel.currentPage.observe(this, Observer {
            // Avoid infinite loop.
            if (pager.currentItem != it) {
                pager.setCurrentItem(it, viewModel.bScrollAnim.get())
                viewModel.bScrollAnim.set(true)
            }

            textPages.text = getPageCountString()
        })

        viewModel.showMenu.observe(this, Observer {
            showMenu(it)
        })

        // Called when TTS chip is clicked.
        viewModel.bTts.observe(this, Observer {
            viewModel.startTtsService(it)
        })

        // Called when Auto chip is clicked.
        viewModel.bAuto.observe(this, Observer {

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
     * Open files depends on file type.
     */
    private fun openBook() {
        intent.extras?.getParcelable<Uri>(KEY_FILE_URI)?.let {
            viewModel.readBookFromUri(it, applicationContext.contentResolver)
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

    private fun getPageCountString() =
        getString(R.string.page_counter, pager.currentItem + 1, pager.adapter?.itemCount ?: 0)

    companion object {
        const val KEY_FILE_URI = "KEY_FILE_URI"

        const val REQUEST_NOTIFICATION_CLICK = 5000
    }

    /**
     * A Text binder for ViewPager2.
     */
    private inner class TextPagerAdapter(val texts: List<CharSequence>) : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = texts.size

        override fun createFragment(position: Int): Fragment {
            return PageFragment(position)
        }
    }
}
