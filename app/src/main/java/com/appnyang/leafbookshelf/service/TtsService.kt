package com.appnyang.leafbookshelf.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spanned
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.view.page.activity.PageActivity
import java.util.*

/**
 * Handle TextToSpeech engine.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-05.
 */
class TtsService : LifecycleService() {

    private val binder = LocalBinder()

    private val notificationId = 1
    private lateinit var notification: NotificationCompat.Builder
    private val actionStop = "TTS_SERVICE_ACTION_STOP_SELF"

    private val idUtterance = "LEAF_TTS"
    private lateinit var textToSpeech: TextToSpeech

    var userCancelReadListener: () -> Unit = {}

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

        val stopIntent = Intent(this, TtsService::class.java).let {
            it.action = actionStop
            if (Build.VERSION.SDK_INT >= 26) {
                PendingIntent.getForegroundService(this, 0, it, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            else {
                PendingIntent.getService(this, 0, it, PendingIntent.FLAG_CANCEL_CURRENT)
            }
        }

        // TODO: Change small icon.
        notification = NotificationCompat.Builder(this, getString(R.string.channel_tts)).apply {
            setSmallIcon(R.drawable.ic_leaf)
            setContentTitle("Book Title")
            setContentText("Content")
            setContentIntent(pendingIntent)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            addAction(NotificationCompat.Action(R.drawable.ic_stop, "Stop", stopIntent))
            setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0)
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopIntent)
            )

        }

        startForeground(notificationId, notification.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Stop the service.
        if (intent?.action == actionStop) {
            userCancelReadListener()
            stopRead()
            stopForeground(true)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRead()
    }

    /**
     * Read the book using TTS engine.
     *
     * @param title
     * @param pagedBook
     * @param currentPage
     */
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

    /**
     * Shutdown the TTS engine.
     */
    private fun stopRead() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    /**
     * Local binder to bind service with ViewModel.
     */
    inner class LocalBinder : Binder() {
        fun getService(): TtsService = this@TtsService
    }
}
