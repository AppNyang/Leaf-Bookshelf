package com.appnyang.leafbookshelf

import io.kotlintest.matchers.string.shouldBeEqualIgnoringCase
import io.kotlintest.matchers.string.shouldHaveLineCount
import io.kotlintest.specs.StringSpec
import org.koin.test.KoinTest

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class BaseUnitTest : KoinTest, StringSpec() {
    //override fun listeners() = listOf(KoinListener(viewModelModule))

    init {
        "Should return two lines" {
            val quote = "First line.\nSecond line.\nThird line."
                .trim()
                .splitToSequence("\n", limit = 3)
                .filterIndexed { index, _ -> index < 2 }
                .joinToString("\n")

            quote shouldHaveLineCount 2
            quote shouldBeEqualIgnoringCase "First line.\nSecond line."
        }
    }
}
