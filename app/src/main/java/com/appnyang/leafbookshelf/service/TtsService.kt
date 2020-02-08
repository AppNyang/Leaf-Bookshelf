package com.appnyang.leafbookshelf.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spanned
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.MutableLiveData
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.view.page.activity.PageActivity
import java.util.*

/**
 * Handle TextToSpeech engine.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-05.
 */
class TtsService : Service() {

    private val binder = LocalBinder()

    private val notificationId = 1
    private lateinit var notification: NotificationCompat.Builder

    private val idUtterance = "LEAF_TTS"
    private lateinit var textToSpeech: TextToSpeech

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
        notification = NotificationCompat.Builder(this, getString(R.string.channel_tts))
            .setSmallIcon(R.drawable.ic_leaf)
            .setContentTitle("Book Title")
            .setContentText("Content")
            .setContentIntent(pendingIntent)

        startForeground(notificationId, notification.build())
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRead()
    }

    fun read(title: CharSequence, pagedBook: LinkedList<Spanned>, currentPage: MutableLiveData<Int>) {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    stopSelf()
                } else {
                    // Update notification.
                    notification
                        .setContentTitle(title)
                        .setContentText(pagedBook[currentPage.value ?: 0])
                    val notificationManager: NotificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(notificationId, notification.build())

                    textToSpeech.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
                        override fun onDone(id: String?) {
                            if (currentPage.value == null) { stopSelf() }

                            currentPage.value?.let { page ->
                                // Go to next page.
                                if (page + 1 in pagedBook.indices) {
                                    currentPage.postValue(currentPage.value?.plus(1))
                                    textToSpeech.speak(pagedBook[page + 1], TextToSpeech.QUEUE_FLUSH, null, idUtterance)
                                }
                                else {
                                    stopSelf()
                                }
                            }
                        }

                        override fun onError(id: String?) {
                            stopSelf()
                        }

                        override fun onStart(id: String?) {}
                    })

                    textToSpeech.speak(pagedBook[currentPage.value ?: 0], TextToSpeech.QUEUE_FLUSH, null, idUtterance)
                }

            } else {
                // status is not TextToSpeech.SUCCESS.
                stopSelf()
            }
        }
    }

    private fun stopRead() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    inner class LocalBinder : Binder() {
        fun getService(): TtsService = this@TtsService
    }
}
