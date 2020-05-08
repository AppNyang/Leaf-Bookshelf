package com.appnyang.leafbookshelf.view.page

import android.util.TypedValue
import android.widget.SeekBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.viewmodel.PageViewModel

/**
 * Binding adapter for Page.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-08.
 */
@BindingAdapter("paged_book", "page_text_appearance", "touch_up_listener")
fun setPagedBook(
    view: ViewPager2,
    pagedBook: List<CharSequence>?,
    pageTextAppearance: PageViewModel.PageTextAppearance?,
    touchUpListener: (touchUpPosition: PageAdapter.TouchUpPosition) -> Unit)
{
    if (pagedBook != null && pageTextAppearance != null) {
        view.adapter?.let {
            if (it is PageAdapter) {
                it.pagedBook = pagedBook
                it.pageTextAppearance = pageTextAppearance
                it.notifyDataSetChanged()
            }
        } ?: run {
            // Create an adapter because view.adapter is null.
            view.adapter = PageAdapter(pagedBook, pageTextAppearance, touchUpListener)
        }
    }
}

@BindingAdapter("page_text_appearance")
fun setPageTextAppearance(view: TextView, pageTextAppearance: PageViewModel.PageTextAppearance?) {
    if (pageTextAppearance != null) {
        view.typeface = pageTextAppearance.fontFamily
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, pageTextAppearance.fontSize)
        view.setTextColor(pageTextAppearance.fontColor)
        view.setLineSpacing(0f, pageTextAppearance.lineSpacing)
    }
}

@BindingAdapter("pages")
fun setPages(view: SeekBar, pagedBook: List<CharSequence>?) {
    if (pagedBook != null) {
        view.max = pagedBook.size - 1
    }
}

@BindingAdapter("paged_book", "current_page")
fun setPageCountString(view: TextView, pagedBook: List<CharSequence>?, currentPage: Int?) {
    if (pagedBook != null && currentPage != null) {
        view.text = view.resources.getString(R.string.page_counter, currentPage + 1, pagedBook.size)
    }
}
