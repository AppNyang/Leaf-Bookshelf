package com.appnyang.leafbookshelf.view.page

import android.util.TypedValue
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.appnyang.leafbookshelf.viewmodel.PageViewModel

/**
 * Binding adapter for Page.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-08.
 */
@BindingAdapter("pageTextAppearance")
fun setPageTextAppearance(view: TextView, pageTextAppearance: PageViewModel.PageTextAppearance?) {
    if (pageTextAppearance != null) {
        view.typeface = pageTextAppearance.fontFamily
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, pageTextAppearance.fontSize)
        view.setTextColor(pageTextAppearance.fontColor)
        view.setLineSpacing(0f, pageTextAppearance.lineSpacing)
    }
}
