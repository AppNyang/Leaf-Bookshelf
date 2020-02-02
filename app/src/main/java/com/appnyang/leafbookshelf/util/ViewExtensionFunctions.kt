package com.appnyang.leafbookshelf.util

import android.view.View

/**
 * View extension functions.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-31.
 */

inline fun View.afterMeasured(crossinline func: View.() -> Unit) {
    addOnLayoutChangeListener(object: View.OnLayoutChangeListener {
        override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int,
            oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            removeOnLayoutChangeListener(this)
            func()
        }
    })
}
