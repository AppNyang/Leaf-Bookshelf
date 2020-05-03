package com.appnyang.leafbookshelf

import com.appnyang.leafbookshelf.util.RoomTypeConverter
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.kotest.matchers.string.shouldHaveLineCount
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

        // ListConverter
        "Should return [\"make\",\"your\",\"world\",\"happy\"]" {
            val json = RoomTypeConverter().stringListToJson(listOf("make", "your", "world", "happy"))

            json shouldBeEqualIgnoringCase "[\"make\",\"your\",\"world\",\"happy\"]"
        }

        "Should return list of make, your, world, happy" {
            val list = RoomTypeConverter().jsonToStringList("[\"make\",\"your\",\"world\",\"happy\"]")

            list shouldBe listOf("make", "your", "world", "happy")
        }

        "Should return empty list" {
            val list = RoomTypeConverter().jsonToStringList("[]")

            list shouldHaveSize 0
        }
    }
}
