package com.appnyang.leafbookshelf.view.bookshelf

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.data.model.history.History

/**
 * History binding adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-03-19.
 */
@BindingAdapter("histories", "item_click_listener")
fun setHistories(view: RecyclerView, items: List<History>?, listener: OnHistoryItemClickListener) {
    if (items != null) {
        view.adapter?.let {
            if (it is HistoryAdapter) {
                it.items = items
                it.notifyDataSetChanged()
            }
        } ?: run {
            view.setHasFixedSize(true)

            // Create an adapter because view.adapter is null.
            HistoryAdapter(items, listener).let {
                view.adapter = it
            }
        }
    }
}
