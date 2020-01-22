package com.appnyang.leafbookshelf.core

import android.app.Application
import com.appnyang.leafbookshelf.di.viewModelModule
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
            modules(listOf(
                viewModelModule
            ))
        }
    }
}