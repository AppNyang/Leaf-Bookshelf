package com.appnyang.leafbookshelf.di

import androidx.preference.PreferenceManager
import com.appnyang.leafbookshelf.data.model.AppDatabase
import com.appnyang.leafbookshelf.data.repository.BookRepository
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.data.repository.CollectionRepository
import com.appnyang.leafbookshelf.data.repository.HistoryRepository
import com.appnyang.leafbookshelf.util.SharedPreferenceLiveData
import com.appnyang.leafbookshelf.viewmodel.BookshelfViewModel
import com.appnyang.leafbookshelf.viewmodel.CollectionViewModel
import com.appnyang.leafbookshelf.viewmodel.MainViewModel
import com.appnyang.leafbookshelf.viewmodel.PageViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin modules.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-01-20.
 */

/**
 * ViewModel Module.
 */
val viewModelModule: Module = module {
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { PageViewModel(get(), get(), get(), get()) }
    viewModel { BookshelfViewModel(get(), get(), get()) }
    viewModel { CollectionViewModel(get()) }

    single { BookmarkRepository(get()) }
    single { HistoryRepository(get()) }
    single { CollectionRepository(get()) }
    single { BookRepository(get()) }
}

/**
 * Room Module.
 */
val roomModule: Module = module {
    single { AppDatabase.getInstance(androidApplication()) }
    single { get<AppDatabase>().getBookmarkDao() }
    single { get<AppDatabase>().getHistoryDao() }
    single { get<AppDatabase>().getCollectionDao() }
    single { get<AppDatabase>().getBookDao() }
}

/**
 * Utility Module.
 */
val utilModule: Module = module {
    factory { SharedPreferenceLiveData(PreferenceManager.getDefaultSharedPreferences(androidApplication())) }
}
