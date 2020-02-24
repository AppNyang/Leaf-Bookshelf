package com.appnyang.leafbookshelf.view.main

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.data.model.history.History
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat

/**
 * History binding adapter.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-22.
 */
@BindingAdapter("recent_files")
fun setRecentFiles(view: RecyclerView, items: List<History>) {
    view.adapter?.let {
        if (it is HistoryAdapter) {
            it.items = items
            it.notifyDataSetChanged()
        }
    } ?: run {
        view.setHasFixedSize(true)

        // Create an adapter because view.adapter is null.
        HistoryAdapter(items).let {
            view.adapter = it
        }
    }
}

@BindingAdapter("readable_read_time", "readable_last_open")
fun setReadableReadTime(view: TextView, readTime: Int, lastOpen: String) {
    val hours = readTime / 60
    val mins = readTime % 60

    var readableTime = ""

    if (hours != 0) {
        readableTime = view.resources.getQuantityString(R.plurals.read_hours, hours, hours)
    }
    readableTime += view.resources.getQuantityString(R.plurals.read_mins, mins, mins)

    val lastOpenTime = ISODateTimeFormat.dateTime().parseDateTime(lastOpen)
    val duration = Interval(lastOpenTime, DateTime.now()).toDuration()

    val readableDuration: String
    readableDuration = when {
        duration.standardDays > 0 -> {
            view.resources.getQuantityString(R.plurals.readable_days_ago, duration.standardDays.toInt(), duration.standardDays.toInt())
        }
        duration.standardHours > 0 -> {
            view.resources.getQuantityString(R.plurals.readable_hours_ago, duration.standardHours.toInt(), duration.standardHours.toInt())
        }
        else -> {
            view.resources.getQuantityString(R.plurals.readable_mins_ago, duration.standardMinutes.toInt(), duration.standardMinutes.toInt())
        }
    }

    readableTime += " · $readableDuration"

    view.text = readableTime
}