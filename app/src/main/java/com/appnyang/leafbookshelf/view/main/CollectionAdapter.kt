package com.appnyang.leafbookshelf.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.databinding.LayoutCollectionBinding
import java.util.*

/**
 * Adapter to show book collections with RecyclerView.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-04-25.
 */
class CollectionAdapter(var items: List<Collection>) : RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutCollectionBinding = DataBindingUtil.bind(view)!!
        val bookCountTemplate = view.context.getString(R.string.collection_readable_books)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_collection, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.item = items[position]
        holder.binding.counter = holder.bookCountTemplate.format(items[position].books.size)
    }
}
