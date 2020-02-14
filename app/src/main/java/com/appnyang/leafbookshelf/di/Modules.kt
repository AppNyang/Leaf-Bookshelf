package com.appnyang.leafbookshelf.di

import com.appnyang.leafbookshelf.data.model.AppDatabase
import com.appnyang.leafbookshelf.data.repository.BookmarkRepository
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
    viewModel { MainViewModel() }
    viewModel { PageViewModel(get(), get()) }

    single { BookmarkRepository(get()) }
}

val roomModule: Module = module {
    single { AppDatabase.getInstance(androidApplication()) }
    single { get<AppDatabase>().getBookmarkDao() }
}
