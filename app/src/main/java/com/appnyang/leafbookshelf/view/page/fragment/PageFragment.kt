package com.appnyang.leafbookshelf.view.page.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.FragmentPageBinding
import com.appnyang.leafbookshelf.viewmodel.PageViewModel
import kotlinx.android.synthetic.main.fragment_page.view.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

/**
 * A fragment represents single page of ViewPager.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-29.
 */
class PageFragment(val page: Int) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = DataBindingUtil.inflate<FragmentPageBinding>(inflater, R.layout.fragment_page, container, false).apply {
            // Bind the layout with ViewModel.
            viewModel = getSharedViewModel()

            // Lifecycle owner of a fragment should be viewLifecycleOwner
            lifecycleOwner = viewLifecycleOwner
        }.root

        getSharedViewModel<PageViewModel>().let { viewModel ->
            view.textPage.text = viewModel.pagedBook.value!![page]
            view.textPage.typeface = viewModel.fontFamily
            view.textPage.setTextColor(viewModel.fontColor)
            view.textPage.setTextSize(TypedValue.COMPLEX_UNIT_SP, viewModel.fontSize)
            view.textPage.setLineSpacing(0f, viewModel.lineSpacing)
        }

        view.framePage.setOnTouchListener { frameLayout, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val viewModel = getSharedViewModel<PageViewModel>()
                when (event.x.toInt()) {
                    in 0 until (frameLayout.width / 3) -> {
                        if (viewModel.isAnyMenuOpened()) {
                            viewModel.displayMenu()
                        }
                        else {
                            viewModel.goToPage(page - 1)
                        }
                    }
                    in (frameLayout.width / 3) until (2 * frameLayout.width / 3) -> viewModel.onShowMenuClicked()
                    in (2 * frameLayout.width / 3)..frameLayout.width -> {
                        if (viewModel.isAnyMenuOpened()) {
                            viewModel.displayMenu()
                        }
                        else {
                            viewModel.goToPage(page + 1)
                        }
                    }
                }
            }

            true
        }

        return view
    }

}
