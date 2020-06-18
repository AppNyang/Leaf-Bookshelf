package com.appnyang.leafbookshelf.view.book

import android.net.Uri
import androidx.databinding.BindingAdapter
import com.appnyang.leafbookshelf.R
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

/**
 * Cover binding adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-06.
 */
@BindingAdapter("cover")
fun setCover(view: ShapeableImageView, cover: Uri?) {
    if (cover != null) {
        Glide.with(view.context)
            .asBitmap()
            .run {
                // Load the image according to the cover scheme.
                if (cover.scheme == "color") {
                    load(getCoverId(cover))
                }
                else {
                    load(cover)
                }

                // Set image to the view.
                into(view)
        }
    }
}

/**
 * Return the cover resource id according to the given key.
 *
 * @param cover Uri of the cover.
 * @return Resource id.
 */
private fun getCoverId(cover: Uri): Int =
    when (cover.toString().partition { it == ':' }.second) {
        "red" -> { R.drawable.ic_book_cover_red }
        else -> { R.drawable.ic_book_cover_red }
    }
