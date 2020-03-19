package com.appnyang.leafbookshelf.view.bookshelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.history.History
import com.appnyang.leafbookshelf.databinding.LayoutHistoryBinding


/**
 * History Adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-03-19.
 */
class HistoryAdapter(var items: List<History>, private val listener: OnHistoryItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutHistoryBinding = DataBindingUtil.bind<LayoutHistoryBinding>(view)!!.apply {
            listener = this@HistoryAdapter.listener
        }
    }

    /**
     * Create a new view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_history, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HistoryViewHolder).binding.item = items[position]
    }
}

class OnHistoryItemClickListener(private val listener: (history: History) -> Unit) {
    fun onItemClicked(history: History) { listener(history) }
}
