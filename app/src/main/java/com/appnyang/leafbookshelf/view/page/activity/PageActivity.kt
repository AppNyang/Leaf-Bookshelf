package com.appnyang.leafbookshelf.view.page.activity

import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.ActivityPageBinding
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

        DataBindingUtil.setContentView<ActivityPageBinding>(this, R.layout.activity_page).apply {
            viewModel = this@PageActivity.viewModel
            lifecycleOwner = this@PageActivity
        }

        // Set page transformer.
        pager.setPageTransformer(DepthPageTransformer())

        // Open files depends on file type.
        if (savedInstanceState == null) {
            openBook()
        }

        viewModel.rawText.observe(this, Observer {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            lifecycleScope.launch {
                viewModel.paginateBook(
                    textPainter.width,
                    textPainter.height,
                    textPainter.paint,
                    textPainter.lineSpacingMultiplier,
                    textPainter.lineSpacingExtra,
                    textPainter.includeFontPadding
                )
            }
        })

        viewModel.pagedBook.observe(this, Observer {
            // Setup ViewPager.
            pager.adapter = TextPagerAdapter(it)
        })
    }

    /**
     * Open files depends on file type.
     */
    private fun openBook() {
        intent.extras?.getParcelable<Uri>(KEY_FILE_URI)?.let {
            viewModel.readBookFromUri(it, applicationContext.contentResolver)
        }
    }

    companion object {
        const val KEY_FILE_URI = "KEY_FILE_URI"
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
