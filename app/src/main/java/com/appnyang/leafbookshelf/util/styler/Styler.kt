package com.appnyang.leafbookshelf.util.styler

import android.text.Spannable
import android.text.style.RelativeSizeSpan

/**
 * Interface of Styler.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-02-05.
 */
interface Styler {
    val listRegex: List<Regex>
    val listSpans: List<MatchResult.(Spannable) -> Unit>
}

class DefaultStyler : Styler {
    override val listRegex = mutableListOf<Regex>()
    override val listSpans = mutableListOf<MatchResult.(Spannable) -> Unit>()

    init {
        listRegex.add(Regex("(?m)(^\\d+\\.)[^\\S\\r\\n]*(.+)"))

        listSpans.add { spannableText ->
            // Chapter number.
            groups[1]?.apply {
                spannableText.setSpan(
                    RelativeSizeSpan(2f),
                    range.first,
                    range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            // Chapter title.
            groups[2]?.apply {
                spannableText.setSpan(
                    RelativeSizeSpan(1.3f),
                    range.first,
                    range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}
