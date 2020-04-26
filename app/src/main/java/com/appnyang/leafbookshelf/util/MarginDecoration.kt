package com.appnyang.leafbookshelf.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL

/**
 * Add margin between items of recycler view.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2019-08-06.
 */
class MarginDecoration(
    private val marginTop: Int,
    private val marginBottom: Int,
    private val marginLeft: Int,
    private val marginRight: Int)
    : RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.apply {
            (parent.layoutManager as LinearLayoutManager).apply {
                when (orientation) {
                    HORIZONTAL -> {
                        left = getHorizontalLeft(view, parent)
                        right = getHorizontalRight(view, parent)
                        top = marginTop
                        bottom = marginBottom
                    }
                    VERTICAL -> {
                        top = getVerticalTop(view, parent)
                        bottom = getVerticalBottom(view, parent)
                        left = marginLeft
                        right = marginRight
                    }
                }
            }
        }
    }

    private fun getHorizontalLeft(view: View, parent: RecyclerView): Int {
        return if (isFirst(view, parent)) {
            0
        }
        else {
            marginLeft
        }
    }

    private fun getHorizontalRight(view: View, parent: RecyclerView): Int {
        return if (isLast(view, parent)) {
            0
        }
        else {
            marginRight
        }
    }

    private fun getVerticalTop(view: View, parent: RecyclerView): Int {
        return if (isFirst(view, parent)) {
            0
        }
        else {
            marginTop
        }
    }

    private fun getVerticalBottom(view: View, parent: RecyclerView): Int {
        return if (isLast(view, parent)) {
            0
        }
        else {
            marginBottom
        }
    }

    private fun isFirst(view: View, parent: RecyclerView) = parent.getChildAdapterPosition(view) == 0
    private fun isLast(view: View, parent: RecyclerView) = parent.getChildAdapterPosition(view) + 1 == parent.adapter?.itemCount
}
