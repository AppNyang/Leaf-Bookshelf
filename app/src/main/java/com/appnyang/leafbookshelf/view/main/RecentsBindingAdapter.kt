package com.appnyang.leafbookshelf.view.main

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.viewmodel.Recents
import org.joda.time.DateTime
import org.joda.time.Interval

/**
 * Recent files binding adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-22.
 */
@BindingAdapter("recents", "item_click_listener")
fun setRecents(view: RecyclerView, items: List<Recents>?, listener: OnRecentsItemClickListener) {
    if (items != null) {
        view.adapter?.let {
            if (it is RecentFileAdapter) {
                it.items = items
                it.notifyDataSetChanged()
            }
        } ?: run {
            view.setHasFixedSize(false)

            // Create an adapter because view.adapter is null.
            RecentFileAdapter(items, listener).let {
                view.adapter = it
            }
        }
    }
}

@BindingAdapter("readable_read_time", "readable_last_open")
fun setReadableReadTime(view: TextView, readTime: Int, lastOpenedAt: DateTime) {
    val hours = readTime / 60
    val mins = readTime % 60

    var readableTime = ""

    if (hours != 0) {
        readableTime = view.resources.getQuantityString(R.plurals.read_hours, hours, hours)
    }
    if (mins != 0) {
        readableTime += view.resources.getQuantityString(R.plurals.read_mins, mins, mins)
    }
    if (hours == 0 && mins == 0) {
        readableTime = view.resources.getString(R.string.few_seconds_read)
    }

    val duration = Interval(lastOpenedAt, DateTime.now()).toDuration()

    val readableDuration: String
    readableDuration = when {
        duration.standardDays > 0 -> {
            view.resources.getQuantityString(R.plurals.readable_days_ago, duration.standardDays.toInt(), duration.standardDays.toInt())
        }
        duration.standardHours > 0 -> {
            view.resources.getQuantityString(R.plurals.readable_hours_ago, duration.standardHours.toInt(), duration.standardHours.toInt())
        }
        duration.standardDays == 0L && duration.standardHours == 0L && duration.standardMinutes == 0L -> {
            view.resources.getString(R.string.few_seconds_ago)
        }
        else -> {
            view.resources.getQuantityString(R.plurals.readable_mins_ago, duration.standardMinutes.toInt(), duration.standardMinutes.toInt())
        }
    }

    readableTime += " · $readableDuration"

    view.text = readableTime
}

@BindingAdapter("reading_progress")
fun setReadingProgress(view: View, readingProgress: Float) {
    val progressHeight = readingProgress * view.resources.getDimension(R.dimen.recent_book_height)

    view.layoutParams.height = progressHeight.toInt()
}
