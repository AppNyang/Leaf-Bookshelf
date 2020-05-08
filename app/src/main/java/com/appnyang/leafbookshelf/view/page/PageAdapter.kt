package com.appnyang.leafbookshelf.view.page

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.databinding.LayoutPageBinding
import com.appnyang.leafbookshelf.viewmodel.PageViewModel
import kotlinx.android.synthetic.main.layout_page.view.*

/**
 * Page Adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-08.
 */
class PageAdapter(
    private var pagedBook: List<CharSequence>,
    private val pageTextAppearance: LiveData<PageViewModel.PageTextAppearance>,
    private val onTouchUpListener: (touchUpPosition: TouchUpPosition) -> Unit
) : RecyclerView.Adapter<PageAdapter.PageViewHolder>() {

    inner class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: LayoutPageBinding = DataBindingUtil.bind(view)!!

        init {
            view.framePage.setOnTouchListener { frameLayout, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val touchUpPosition = when (event.x.toInt()) {
                        in 0 until (frameLayout.width / 3) -> TouchUpPosition.LEFT
                        in (frameLayout.width / 3) until (2 * frameLayout.width / 3) -> TouchUpPosition.MIDDLE
                        in (2 * frameLayout.width / 3)..frameLayout.width -> TouchUpPosition.RIGHT
                        else -> TouchUpPosition.RIGHT
                    }

                    onTouchUpListener(touchUpPosition)
                }

                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder =
        PageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_page, parent, false))

    override fun getItemCount(): Int = pagedBook.size

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.binding.content = pagedBook[position].toString()
        holder.binding.pageTextAppearance = pageTextAppearance
    }

    enum class TouchUpPosition {
        LEFT, MIDDLE, RIGHT
    }
}
