package com.appnyang.leafbookshelf.view.page.fragment

import android.os.Bundle
import android.view.LayoutInflater
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
            viewmodel = getSharedViewModel()

            // Lifecycle owner of a fragment should be viewLifecycleOwner
            lifecycleOwner = viewLifecycleOwner
        }.root

        view.textPage.text = getSharedViewModel<PageViewModel>().pagedBook.value!![page]

        return view
    }

}
