package com.appnyang.leafbookshelf.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.LayoutRecentFileBinding
import com.appnyang.leafbookshelf.viewmodel.RecentFile
import com.appnyang.leafbookshelf.viewmodel.RecentHistory
import com.appnyang.leafbookshelf.viewmodel.RecentPromo
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.layout_recent_file.view.*
import kotlinx.android.synthetic.main.layout_recent_file_promo.view.*


/**
 * Recent Files Adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-22.
 */
class RecentFileAdapter(var items: List<RecentFile>, private val listener: OnHistoryItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutRecentFileBinding = DataBindingUtil.bind<LayoutRecentFileBinding>(view)!!.apply {
            listener = this@RecentFileAdapter.listener
        }

        init {
            val radius = view.resources.getDimension(R.dimen.main_book_round)
            view.imageCover.shapeAppearanceModel = view.imageCover.shapeAppearanceModel
                .toBuilder()
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setBottomRightCorner(CornerFamily.ROUNDED, radius)
                .build()
        }
    }

    inner class PromoViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun displayUnifiedAd(recentPromo: RecentPromo) {
            val adView = view.adView as UnifiedNativeAdView

            if (recentPromo.unifiedNativeAd.icon != null) {
                view.imagePromoIcon.setImageDrawable(recentPromo.unifiedNativeAd.icon.drawable)
                adView.iconView = view.imagePromoIcon
            }

            view.textPromoHeadline.text = recentPromo.unifiedNativeAd.headline
            adView.headlineView = view.textPromoHeadline

            view.textPromoBody.text = recentPromo.unifiedNativeAd.body
            adView.bodyView = view.textPromoBody

            adView.setNativeAd(recentPromo.unifiedNativeAd)
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is RecentHistory -> 0
            is RecentPromo -> 1
        }

    /**
     * Create a new view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_recent_file, parent, false))
        }
        else {
            PromoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_recent_file_promo, parent, false))
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val recentFile = items[position]) {
            is RecentHistory -> (holder as HistoryViewHolder).binding.item = recentFile
            is RecentPromo -> (holder as PromoViewHolder).displayUnifiedAd(recentFile)
        }
    }
}

class OnHistoryItemClickListener(private val listener: (recentFile: RecentFile) -> Unit) {
    fun onItemClicked(recentFile: RecentFile) { listener(recentFile) }
}
