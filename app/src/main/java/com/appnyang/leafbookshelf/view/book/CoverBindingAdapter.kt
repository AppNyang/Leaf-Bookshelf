package com.appnyang.leafbookshelf.view.book

import android.net.Uri
import androidx.databinding.BindingAdapter
import com.appnyang.leafbookshelf.R
import com.google.android.material.imageview.ShapeableImageView

/**
 * Cover binding adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-06.
 */
@BindingAdapter("cover")
fun setCover(view: ShapeableImageView, cover: Uri?) {
    if (cover != null) {
        if (cover.scheme == "color") {
            view.setImageResource(
                when (cover.toString().partition { it == ':' }.second) {
                    "red" -> { R.drawable.ic_book_cover_red }
                    else -> { R.drawable.ic_book_cover_red }
                }
            )
        }
        else {
            view.setImageURI(cover)
        }
    }
}
