package com.appnyang.leafbookshelf.core

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.appnyang.leafbookshelf.R
import com.appnyang.leafbookshelf.di.roomModule
import com.appnyang.leafbookshelf.di.viewModelModule
import com.google.android.gms.ads.MobileAds
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * The app class.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */
class LeafApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LeafApp)
            modules(listOf(
                viewModelModule,
                roomModule
            ))
        }

        // Create the notification channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            NotificationChannel(getString(R.string.channel_tts), getString(R.string.notification_channel_service_name), NotificationManager.IMPORTANCE_DEFAULT).let {
                it.description = getString(R.string.notification_channel_service_desc)
                notificationManager.createNotificationChannel(it)
            }
        }

        // Initialize time library.
        JodaTimeAndroid.init(this)

        // Initialize google ads.
        MobileAds.initialize(this) {}
    }
}
