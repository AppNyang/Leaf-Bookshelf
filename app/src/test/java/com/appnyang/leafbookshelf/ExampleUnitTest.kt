package com.appnyang.leafbookshelf

import com.appnyang.leafbookshelf.di.roomModule
import io.kotlintest.koin.KoinListener
import io.kotlintest.matchers.haveLength
import io.kotlintest.should
import io.kotlintest.specs.StringSpec
import org.koin.test.KoinTest

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class Db : KoinTest, StringSpec() {
    override fun listeners() = listOf(KoinListener(roomModule))

    init {
        "Test" {
            "abc" should haveLength(3)
        }
    }
}
