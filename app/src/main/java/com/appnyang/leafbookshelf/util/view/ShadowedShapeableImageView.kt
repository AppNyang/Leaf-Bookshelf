package com.appnyang.leafbookshelf.util.view

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.AbsoluteCornerSize

/**
 * Shapeable ImageView with shadow.
 */
class ShadowedShapeableImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : ShapeableImageView(context, attrs, defStyle) {

    init {
        // Provide an ViewOutlineProvider so that shadows can be displayed properly in the ShapeableImageView.
        outlineProvider = object: ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, width, height, (shapeAppearanceModel.topRightCornerSize as AbsoluteCornerSize).cornerSize)
            }
        }
    }
}
