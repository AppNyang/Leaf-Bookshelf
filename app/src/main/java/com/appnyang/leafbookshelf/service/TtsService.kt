package com.appnyang.leafbookshelf.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.view.page.activity.PageActivity

/**
 * Handle TextToSpeech engine.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-05.
 */
class TtsService : Service() {

    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()

        val pendingIntent = Intent(this, PageActivity::class.java).let { notificationIntent ->
            TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(notificationIntent)
                getPendingIntent(PageActivity.REQUEST_NOTIFICATION_CLICK, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        if (pendingIntent == null) {
            stopSelf()
        }

        // TODO: Change small icon.
        val notification = NotificationCompat.Builder(this, getString(R.string.channel_tts))
            .setSmallIcon(R.drawable.ic_leaf)
            .setContentTitle("Book Title")
            .setContentText("Content")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): TtsService = this@TtsService
    }
}
