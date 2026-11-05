package com.wanderwildwood.ekikyo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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

fun AnnotatedString.Builder.hanzi(text: String) = withStyle(SpanStyle(fontFamily = HanziFont)) { append(text) }

/** "坎 Kǎn, Water": a trigram by its character, pinyin and image. */
fun trigramLine(label: String, t: Trigram, names: Names): AnnotatedString = buildAnnotatedString {
    append("$label: ")
    hanzi(t.hanzi)
    append(" ${t.pinyin}, ${names.of(t)}")
}

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
