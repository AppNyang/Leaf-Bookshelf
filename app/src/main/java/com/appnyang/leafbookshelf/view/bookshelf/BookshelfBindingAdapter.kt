package com.appnyang.leafbookshelf.view.bookshelf

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.data.model.history.History

/**
 * Bookshelf binding adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-03-19.
 */
@BindingAdapter("books", "item_click_listener", "item_long_click_listener")
fun setHistories(view: RecyclerView, items: List<History>?, listener: OnHistoryItemClickListener, longClickListener: OnBookshelfItemLongClickListener) {
    if (items != null) {
        view.adapter?.let {
            if (it is BookshelfAdapter) {
                it.items = items
                it.notifyDataSetChanged()
            }
        } ?: run {
            view.setHasFixedSize(true)

            // Create an adapter because view.adapter is null.
            BookshelfAdapter(items, listener, longClickListener).let {
                view.adapter = it
            }
        }
    }
}
