package com.appnyang.leafbookshelf.view.page.activity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.properties.Delegates

/**
 * A custom view that shows texts to look similar to a page of book.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class PagerView : View {

    private val pagedText = mutableListOf<MutableList<String>>()
    private var completePaged: Int by Delegates.observable(-1) { _, _, new ->
        if (currentPage == new) {
            invalidate()
        }
    }

    private val paint = Paint()
    private var textSize = 55f
    private var lineSpace = 40f

    private var currentPage = 0

    private val gestureDetector = GestureDetector(context, GestureListener())

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        paint.apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = this@PagerView.textSize
        }
    }

    /**
     * Should be called after the textSize or text itself is changed.
     */
    suspend fun buildPagedText(rawText: String) {
        withContext(Dispatchers.Default) {
            var lineBegin = 0
            val rect = Rect()

            val aPage = mutableListOf<String>()

            for (end in 1 until rawText.length) {
                if (end == rawText.length) {
                    paint.getTextBounds(rawText, lineBegin, end, rect)
                } else {
                    paint.getTextBounds(rawText, lineBegin, end + 1, rect)
                }

                if (rect.width() + textSize > width || rawText[end] == '\n') {
                    // If next line over the one page height,
                    if ((aPage.size) * (textSize + lineSpace) + textSize > height) {
                        pagedText.add(aPage.toMutableList())

                        aPage.clear()
                        aPage.add(rawText.substring(lineBegin, end))

                        completePaged = pagedText.size - 1
                    } else {
                        aPage.add(rawText.substring(lineBegin, end))
                    }

                    lineBegin = end
                }
            }

            if (aPage.isNotEmpty()) {
                pagedText.add(aPage.toMutableList())
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (currentPage in 0 until pagedText.size) {
            pagedText[currentPage].forEachIndexed { index, line ->
                canvas?.drawText(line, 0f, index * (textSize + lineSpace) + textSize, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private inner class GestureListener : GestureDetector.OnGestureListener {
        override fun onFling(ev0: MotionEvent?, ev1: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (abs(velocityX) > 800) {
                // Fling to right.
                if (velocityX > 0) {
                    if (currentPage > 0) {
                        currentPage--
                        invalidate()
                    }
                }
                else {
                    if (currentPage < pagedText.size - 1) {
                        currentPage++
                        invalidate()
                    }
                }
            }
            return true
        }

        override fun onShowPress(p0: MotionEvent?) {}
        override fun onSingleTapUp(p0: MotionEvent?): Boolean = true
        override fun onDown(p0: MotionEvent?): Boolean = true
        override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = true
        override fun onLongPress(p0: MotionEvent?) {}
    }
}
