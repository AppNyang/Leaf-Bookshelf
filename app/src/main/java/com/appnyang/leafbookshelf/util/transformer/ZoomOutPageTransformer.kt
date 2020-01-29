package com.appnyang.leafbookshelf.util.transformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * A zoom out page transformer for ViewPager2.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-29.
 */
class ZoomOutPageTransformer : ViewPager2.PageTransformer {

    private val minScale = 0.9f
    private val minAlpha = 0.5f

    override fun transformPage(page: View, position: Float) {
        page.apply {
            val pageWidth = width
            val pageHeight = height

            when {
                position < -1 -> alpha = 0f
                position <= 1 -> {
                    val scaleFactor = minScale.coerceAtLeast(1 - abs(position))
                    val verticalMargin = pageHeight * (1 - scaleFactor) / 2
                    val horizontalMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horizontalMargin - verticalMargin / 2
                    } else {
                        horizontalMargin + verticalMargin / 2
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    // Fade the page relative to its size.
                    alpha = (minAlpha + (((scaleFactor - minScale) / (1 - minScale)) * (1 - minAlpha)))
                }
                else -> alpha = 0f
            }
        }
    }
}
