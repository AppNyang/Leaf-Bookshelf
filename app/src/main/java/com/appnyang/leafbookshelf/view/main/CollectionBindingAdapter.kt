package com.appnyang.leafbookshelf.view.main

import android.util.TypedValue
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.util.MarginDecoration

/**
 * Functions to link book collections data with RecyclerView.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-04-25.
 */
@BindingAdapter("collections")
fun setCollections(view: RecyclerView, items: List<Collection>?) {
    if (items != null) {
        view.adapter?.let {
            if (it is CollectionAdapter) {
                it.items = items
                it.notifyDataSetChanged()
            }
        } ?: run {
            view.setHasFixedSize(true)

            LinearSnapHelper().attachToRecyclerView(view)

            // Set margin decoration.
            val marginHorizontal = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8f,
                view.resources.displayMetrics
            ).toInt()
            view.addItemDecoration(MarginDecoration(0, 0, marginHorizontal, marginHorizontal))

            // Create an adapter because view.adapter is null.
            view.adapter = CollectionAdapter(items)
        }
    }
}
