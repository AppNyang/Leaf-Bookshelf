package com.appnyang.leafbookshelf.view.page

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.viewpager2.widget.ViewPager2
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.viewmodel.PageViewModel
import com.google.android.material.slider.Slider

/**
 * Binding adapter for Page.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-08.
 */
@BindingAdapter("paged_book", "page_text_appearance", "touch_up_listener", "current_page")
fun setPagedBook(
    view: ViewPager2,
    pagedBook: List<CharSequence>?,
    pageTextAppearance: PageViewModel.PageTextAppearance?,
    touchUpListener: (touchUpPosition: PageAdapter.TouchUpPosition) -> Unit,
    currentPage: PageViewModel.CurrentPage
) {
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

        //view.currentItem = currentPage
        view.setCurrentItem(currentPage.page, currentPage.bSmoothScroll)
    }
}

@InverseBindingAdapter(attribute = "current_page", event = "pageAttrChanged")
fun getCurrentPage(view: ViewPager2): PageViewModel.CurrentPage {
    return PageViewModel.CurrentPage(view.currentItem)
}

@BindingAdapter("pageAttrChanged")
fun setOnPageChangeListener(view: ViewPager2, pageAttrChanged: InverseBindingListener) {
    view.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            pageAttrChanged.onChange()
        }
    })
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
fun setPages(view: Slider, pagedBook: List<CharSequence>?) {
    if (pagedBook != null) {
        view.valueTo = pagedBook.size.toFloat()
    }
}

@BindingAdapter("current_page")
fun setCurrentPage(view: Slider, currentPage: PageViewModel.CurrentPage) {
    if (view.value.toInt() != currentPage.page + 1) {
        view.value = currentPage.page + 1f
    }
}

@InverseBindingAdapter(attribute = "current_page", event = "pageAttrChanged")
fun getCurrentPage(view: Slider): PageViewModel.CurrentPage {
    return PageViewModel.CurrentPage(view.value.toInt() - 1)
}

@BindingAdapter("pageAttrChanged")
fun setOnPageChangeListener(view: Slider, pageAttrChanged: InverseBindingListener) {
    view.addOnChangeListener { _, _, fromUser ->
        if (fromUser) {
            pageAttrChanged.onChange()
        }
    }
}

@BindingAdapter("paged_book", "current_page")
fun setPageCountString(view: TextView, pagedBook: List<CharSequence>?, currentPage: PageViewModel.CurrentPage) {
    if (pagedBook != null) {
        view.text = view.resources.getString(R.string.page_counter, currentPage.page + 1, pagedBook.size)
    }
}

@BindingAdapter("auto_reading_progress")
fun setAutoReadingProgress(view: View, progress: Float) {
    (view.layoutParams as ConstraintLayout.LayoutParams)
        .matchConstraintPercentWidth = progress

    view.requestLayout()
}
