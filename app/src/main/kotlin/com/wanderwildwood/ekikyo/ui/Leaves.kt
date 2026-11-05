package com.wanderwildwood.ekikyo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD

/*
 * Legge's text set as a book is: broken into lines here, once, at the width the page gives, and
 * each line (or a few kept together) handed to the list as a row of its own. Whole-row pages then
 * fill to the foot and a paragraph runs on over the turn, where a paragraph as one row left half
 * a page blank when it did not fit, and a paragraph taller than a page was cut through a line.
 *
 * Kept together, as a printer would: a heading with the first two lines under it (no heading
 * alone at a foot), and the last two lines of a paragraph (no line alone at a head).
 */

/**
 * The width of [PagedColumn]'s page bar: 8 dp either side of its 24 dp chevrons (PageBar in
 * PagedColumn.kt). The rows are this much narrower than the screen whenever there are pages.
 */
private val PAGE_BAR = 40.dp

/** The book style with each line exactly its line height tall, so lines set one per row stand as they would in a paragraph. */
val lineStyle: TextStyle
    @Composable get() = bookStyle.copy(lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None))

/** Breaks paragraphs into the lines they take at the page's width, remembering each. */
class Setter internal constructor(private val measurer: TextMeasurer, private val style: TextStyle, private val width: Int) {
    private val cache = HashMap<String, List<String>>()

    fun lines(text: String): List<String> = cache.getOrPut(text) {
        val r = measurer.measure(text, style, constraints = Constraints(maxWidth = width))
        List(r.lineCount) { i -> text.substring(r.getLineStart(i), r.getLineEnd(i, visibleEnd = true)) }
    }
}

/** A [Setter] for the full-width screens of this app, at the width their rows have beside the page bar. */
@Composable
fun rememberSetter(): Setter {
    val measurer = rememberTextMeasurer(cacheSize = 0)
    val style = lineStyle
    val screen = LocalConfiguration.current.screenWidthDp.dp
    // Two dp to spare, so a line measured here can never fail to fit where it is drawn.
    val width = with(LocalDensity.current) { (screen - SIDE * 2 - PAGE_BAR - 2.dp).roundToPx() }
    return remember(measurer, style, width) { Setter(measurer, style, width) }
}

/**
 * A paragraph of Legge as rows: under [heading] when given (a rule and the italic heading),
 * otherwise a paragraph's space above it. Keys start with [key].
 */
fun LazyListScope.passage(key: String, heading: AnnotatedString?, body: String, set: Setter) {
    val lines = set.lines(body)
    val chunks: List<List<String>> = if (lines.size <= 4) {
        listOf(lines)
    } else {
        listOf(lines.take(2)) + lines.subList(2, lines.size - 2).map { listOf(it) } + listOf(lines.takeLast(2))
    }
    chunks.forEachIndexed { i, chunk ->
        item(key = "$key.$i") {
            Column(Modifier.fillMaxWidth().padding(horizontal = SIDE)) {
                if (i == 0) {
                    if (heading != null) {
                        Rule(Modifier.padding(top = 20.dp, bottom = 10.dp))
                        TextMMD(text = heading, style = rubricStyle, modifier = Modifier.padding(bottom = 6.dp))
                    } else {
                        Spacer(Modifier.padding(top = 12.dp))
                    }
                }
                BookLines(chunk)
            }
        }
    }
}

fun LazyListScope.passage(key: String, heading: String?, body: String, set: Setter) =
    passage(key, heading?.let { AnnotatedString(it) }, body, set)

@Composable
private fun BookLines(lines: List<String>) {
    val style = lineStyle
    for (l in lines) TextMMD(text = l, style = style, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip)
}
