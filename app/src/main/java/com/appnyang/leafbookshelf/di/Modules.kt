package com.appnyang.leafbookshelf.di

import androidx.preference.PreferenceManager
import com.appnyang.leafbookshelf.data.model.AppDatabase
import com.appnyang.leafbookshelf.data.repository.BookRepository
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
import com.appnyang.leafbookshelf.data.repository.CollectionRepository
import com.appnyang.leafbookshelf.util.SharedPreferenceLiveData
import com.appnyang.leafbookshelf.viewmodel.*
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
    viewModel { MainViewModel(get(), get()) }
    viewModel { PageViewModel(get(), get(), get(), get()) }
    viewModel { BookshelfViewModel(get(), get()) }
    viewModel { CollectionViewModel(get()) }
    viewModel { BookViewModel(get()) }

    single { BookmarkRepository(get()) }
    single { CollectionRepository(get(), get()) }
    single { BookRepository(get()) }
}

/**
 * Room Module.
 */
val roomModule: Module = module {
    single { AppDatabase.getInstance(androidApplication()) }
    single { get<AppDatabase>().getBookmarkDao() }
    single { get<AppDatabase>().getCollectionDao() }
    single { get<AppDatabase>().getBookDao() }
    single { get<AppDatabase>().getCollectionWithBooksDao() }
}

/**
 * Utility Module.
 */
val utilModule: Module = module {
    factory { SharedPreferenceLiveData(PreferenceManager.getDefaultSharedPreferences(androidApplication())) }
}
