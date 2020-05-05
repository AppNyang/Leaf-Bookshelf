package com.appnyang.leafbookshelf.view.bookshelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.book.Book
import com.appnyang.leafbookshelf.databinding.LayoutBookItemBinding
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.layout_book_item.view.*


/**
 * Bookshelf Adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-03-19.
 */
class BookshelfAdapter(
    var items: List<Book>,
    private val listener: OnBookshelfItemClickListener,
    private val longClickListener: OnBookshelfItemLongClickListener
) : RecyclerView.Adapter<BookshelfAdapter.BookshelfViewHolder>() {

    inner class BookshelfViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutBookItemBinding = DataBindingUtil.bind<LayoutBookItemBinding>(view)!!.apply {
            listener = this@BookshelfAdapter.listener
        }

        init {
            view.cardBookItem.setOnLongClickListener { card ->
                card as MaterialCardView
                card.isChecked = !card.isChecked

                longClickListener.onItemLongClicked(card)

                true
            }
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

class OnBookshelfItemClickListener(private val listener: (card: MaterialCardView, book: Book) -> Unit) {
    fun onItemClicked(view: View, book: Book) { listener(view as MaterialCardView, book) }
}

class OnBookshelfItemLongClickListener(private val listener: (card: MaterialCardView) -> Unit) {
    fun onItemLongClicked(card: MaterialCardView) { listener(card) }
}
