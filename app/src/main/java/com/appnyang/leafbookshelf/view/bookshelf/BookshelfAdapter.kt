package com.appnyang.leafbookshelf.view.bookshelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.history.History
import com.appnyang.leafbookshelf.databinding.LayoutBookItemBinding


/**
 * Bookshelf Adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-03-19.
 */
class BookshelfAdapter(var items: List<History>, private val listener: OnHistoryItemClickListener) : RecyclerView.Adapter<BookshelfAdapter.BookshelfViewHolder>() {

    inner class BookshelfViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutBookItemBinding = DataBindingUtil.bind<LayoutBookItemBinding>(view)!!.apply {
            listener = this@BookshelfAdapter.listener
        }
    }

    /**
     * Create a new view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookshelfViewHolder =
        BookshelfViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_book_item, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BookshelfViewHolder, position: Int) {
        holder.binding.item = items[position]
    }
}

class OnHistoryItemClickListener(private val listener: (history: History) -> Unit) {
    fun onItemClicked(history: History) { listener(history) }
}
