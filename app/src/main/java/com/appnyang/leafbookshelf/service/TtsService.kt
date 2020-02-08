package com.appnyang.leafbookshelf.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.appnyang.leafbookshelf.R

/**
 * Handle TextToSpeech engine.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-05.
 */
class TtsService : Service() {

    override fun onCreate() {
        super.onCreate()

        // TODO: Change small icon.
        val notification = NotificationCompat.Builder(this, getString(R.string.channel_tts))
            .setSmallIcon(R.drawable.ic_leaf)
            .setContentTitle("Book Title")
            .setContentText("Content")
            .build()

        startForeground(1, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}
