package com.appnyang.leafbookshelf.util.transformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * Depth page transformer for ViewPager2.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-29.
 */
class DepthPageTransformer : ViewPager2.PageTransformer {

    private val minScale = 0.75f

    override fun transformPage(page: View, position: Float) {
        page.apply {
            val pageWidth = width
            when {
                position < -1 -> alpha = 0f
                position <= 0 -> {
                    alpha = 1f
                    translationX = 0f
                    translationZ = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> {
                    alpha = 1 - position

                    translationX = pageWidth * -position
                    translationZ = -1f

                    val scaleFactor = (minScale + (1 - minScale) * (1 - abs(position)))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> alpha = 0f
            }
        }
    }
}