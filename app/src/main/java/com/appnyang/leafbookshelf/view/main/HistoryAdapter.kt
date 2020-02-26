package com.appnyang.leafbookshelf.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.history.History
import com.appnyang.leafbookshelf.databinding.LayoutRecentFileBinding


/**
 * History Adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-22.
 */
class HistoryAdapter(var items: List<History>, private val listener: OnHistoryItemClickListener) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutRecentFileBinding = DataBindingUtil.bind<LayoutRecentFileBinding>(view)!!.apply {
            listener = this@HistoryAdapter.listener
        }
    }

    /**
     * Create a new view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder =
        HistoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_recent_file, parent, false)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.binding.item = items[position]
    }
}

class OnHistoryItemClickListener(private val listener: (history: History) -> Unit) {
    fun onItemClicked(history: History) { listener(history) }
}
