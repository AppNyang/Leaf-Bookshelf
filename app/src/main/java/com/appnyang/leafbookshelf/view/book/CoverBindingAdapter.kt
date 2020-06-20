package com.appnyang.leafbookshelf.view.book

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.palette.graphics.Palette
import com.appnyang.leafbookshelf.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
            .override(view.width, view.height)
            .centerCrop()
            .run {
                // Load the image according to the cover scheme.
                if (cover.scheme == "color") {
                    load(getCoverId(cover))
                }
                else {
                    load(cover)
                }

                // Pick muted color from palette and set it to shadow color.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    listener(object: RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean) = false

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            if (resource != null) {
                                Palette.from(resource).generate { palette ->
                                    val defaultColor = ContextCompat.getColor(view.context, R.color.darkEmerald)
                                    palette?.getMutedColor(defaultColor).let { mutedColor ->
                                        view.outlineAmbientShadowColor = mutedColor ?: defaultColor
                                        view.outlineSpotShadowColor = mutedColor ?: defaultColor
                                    }
                                }
                            }
                            return false
                        }
                    })
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
