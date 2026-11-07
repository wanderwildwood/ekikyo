package com.wanderwildwood.ekikyo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import com.wanderwildwood.ekikyo.R
import com.wanderwildwood.ekikyo.iching.Hexagram
import com.wanderwildwood.ekikyo.iching.Trigram
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** The English names: the 64 in King Wen order and the eight trigrams' images in [Trigram] order. */
class Names(private val hexagrams: Array<String>, private val trigrams: Array<String>) {
    fun of(number: Int): String = hexagrams[number - 1]
    fun of(h: Hexagram): String = of(h.number)
    fun of(t: Trigram): String = trigrams[t.ordinal]

    /** "3 屯 Zhūn", the line over a hexagram's English name, the characters in the app's own font. */
    fun title(h: Hexagram): AnnotatedString = buildAnnotatedString {
        append("${h.number}  ")
        hanzi(h.hanzi)
        append("  ${h.pinyin}")
    }
}

/**
 * The 71 characters of the hexagram and trigram names, cut from Noto Serif CJK TC
 * (tools/make-font.py): the phone may have no Chinese font of its own.
 */
val HanziFont = FontFamily(Font(R.font.noto_serif_tc_hexagrams))

/**
 * Legge's words are set in a book face, Noto Serif cut to the characters the app uses
 * (tools/make-book-font.py), so the classic reads as a page of a book and the app's own words,
 * in the house Lato, read as the frame around it. Its line is a little looser than the house's.
 */
val BookFont = FontFamily(
    Font(R.font.noto_serif_book),
    Font(R.font.noto_serif_book_italic, style = FontStyle.Italic),
)

/** Legge's text: the house body size in the book face, with room between the lines. */
val bookStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyMedium.copy(fontFamily = BookFont, lineHeight = 27.sp)

/**
 * A hexagram's short name under its figure or in the list. Hyphenated in the phone's language, so a
 * long German word (Unerfahrenheit, Zurückhaltung) breaks where a reader expects in a narrow column
 * instead of at whatever letter the width ran out on.
 */
val nameStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyMedium.copy(hyphens = Hyphens.Auto)

/** A heading over the text: the book face in italic, a step up. */
val rubricStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyLarge.copy(fontFamily = BookFont, fontStyle = FontStyle.Italic)

fun AnnotatedString.Builder.hanzi(text: String) = withStyle(SpanStyle(fontFamily = HanziFont)) { append(text) }

@Composable
fun rememberNames(): Names {
    val res = LocalContext.current.resources
    return remember(res) {
        Names(res.getStringArray(R.array.hexagram_names), res.getStringArray(R.array.trigram_names))
    }
}

fun longDate(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

fun shortDate(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
