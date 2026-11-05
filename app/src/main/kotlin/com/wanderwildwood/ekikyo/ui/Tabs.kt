package com.wanderwildwood.ekikyo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.text_field.TextFieldMMD
import com.wanderwildwood.ekikyo.R
import com.wanderwildwood.ekikyo.iching.KingWen
import com.wanderwildwood.ekikyo.iching.Reading
import java.time.LocalDate

/** The hexagram for today: cast once with a press, then the same all day. */
@Composable
fun TodayTab(state: AppState, go: (Screen) -> Unit) {
    val today = LocalDate.now().toEpochDay()
    val daily = state.daily(today)
    if (daily == null) {
        Column(Modifier.padding(horizontal = SIDE, vertical = 16.dp).testTag("today_empty")) {
            TextMMD(text = longDate(today), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(10.dp))
            TextMMD(text = stringResource(R.string.today_intro), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            HouseButton(stringResource(R.string.today_cast), on = true, modifier = WideRow, onClick = { state.castDaily(today) })
        }
        return
    }
    val (lines, method) = daily
    val words = rememberReadingWords()
    val kept = state.entries.firstOrNull { it.daily && it.epochDay == today && it.lines == lines }
    val methodLabel = stringResource(methodName(method))
    PagedColumn(Modifier.fillMaxSize().testTag("today")) {
        item(key = "date") {
            TextMMD(
                text = "${longDate(today)} · $methodLabel",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = SIDE).padding(top = 10.dp),
            )
        }
        readingItems(Reading(lines), state.book, words, go)
        item(key = "keep") {
            Column(Modifier.padding(horizontal = SIDE, vertical = 12.dp)) {
                if (kept == null) {
                    HouseButton(stringResource(R.string.today_keep), modifier = WideRow, onClick = { state.keepDaily(today, lines, method) })
                } else {
                    HouseButton(stringResource(R.string.today_kept), onClick = { go(Screen.Reading(kept.id)) }, modifier = WideRow)
                }
            }
        }
    }
}

/**
 * A cast of one's own: a question if wanted, then the lines one by one from the bottom up, each
 * a press, or the rest all at once. When the sixth is down the reading opens and is kept in the
 * journal.
 */
@Composable
fun CastTab(state: AppState, go: (Screen) -> Unit) {
    val n = state.building.size
    PagedColumn(Modifier.fillMaxSize().testTag("cast")) {
        item(key = "question") {
            TextFieldMMD(
                value = state.question,
                onValueChange = { state.question = it },
                placeholder = { TextMMD(text = stringResource(R.string.cast_question_hint), style = MaterialTheme.typography.bodyMedium) },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().padding(horizontal = SIDE).padding(top = 6.dp).testTag("question_field"),
            )
        }
        // The figure beside its buttons, so a cast in progress and the next press share one page
        // on a small screen.
        item(key = "cast") {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = SIDE, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                    Figure(state.building.toList(), 76.dp, modifier = Modifier.testTag("building"))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    if (n < 6) {
                        HouseButton(
                            stringResource(R.string.cast_line, (n + 1).toString()),
                            on = true, modifier = WideRow.testTag("cast_line"),
                            onClick = { state.castLine() },
                        )
                        HouseButton(
                            stringResource(if (n == 0) R.string.cast_all else R.string.cast_rest),
                            modifier = WideRow.testTag("cast_all"),
                            onClick = { state.castRest() },
                        )
                    } else {
                        HouseButton(
                            stringResource(R.string.cast_read),
                            on = true, modifier = WideRow.testTag("cast_read"),
                            onClick = { go(Screen.Reading(state.finish().id)) },
                        )
                    }
                    if (n > 0) HouseButton(stringResource(R.string.cast_over), modifier = WideRow, onClick = { state.building.clear() })
                }
            }
        }
        item(key = "note") {
            TextMMD(
                text = stringResource(R.string.cast_method_note, stringResource(methodName(state.method))),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = SIDE),
            )
        }
        item(key = "intro") {
            TextMMD(
                text = stringResource(R.string.cast_intro),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = SIDE, vertical = 12.dp),
            )
        }
    }
}

/** Every reading, newest first. */
@Composable
fun JournalTab(state: AppState, go: (Screen) -> Unit, onTab: (Int) -> Unit) {
    val names = rememberNames()
    val entries = state.entries
    if (entries.isEmpty()) {
        Column(Modifier.padding(horizontal = SIDE, vertical = 16.dp).testTag("journal_empty")) {
            TextMMD(text = stringResource(R.string.journal_empty), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            HouseButton(stringResource(R.string.journal_empty_cast), onClick = { onTab(1) }, modifier = WideRow)
        }
        return
    }
    val daily = stringResource(R.string.reading_daily)
    PagedColumn(state = rememberLazyListState(), modifier = Modifier.fillMaxSize().testTag("journal")) {
        for (e in entries) {
            item(key = e.id) {
                val r = e.reading
                val hexes = buildString {
                    append("${r.primary.number} ${names.of(r.primary)}")
                    r.relating?.let { append(" → ${it.number} ${names.of(it)}") }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { go(Screen.Reading(e.id)) }
                        .padding(horizontal = SIDE, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Figure(e.lines, SmallFigure, marks = false)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        TextMMD(
                            text = if (e.daily) "${shortDate(e.epochDay)} · $daily" else shortDate(e.epochDay),
                            style = MaterialTheme.typography.labelSmall,
                        )
                        TextMMD(text = hexes, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        val second = e.question.ifBlank { e.note }
                        if (second.isNotBlank()) {
                            TextMMD(text = second, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Chevron()
                }
            }
        }
    }
}

/** All 64 in King Wen order, each opening its page. */
@Composable
fun BookTab(go: (Screen) -> Unit) {
    val names = rememberNames()
    PagedColumn(state = rememberLazyListState(), modifier = Modifier.fillMaxSize().testTag("book")) {
        for (h in KingWen.all) {
            item(key = h.number) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { go(Screen.Hexagram(h.number)) }
                        .padding(horizontal = SIDE, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Figure(youngLines(h.bits), SmallFigure, marks = false)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        TextMMD(text = names.title(h), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        TextMMD(text = names.of(h), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Chevron()
                }
            }
        }
    }
}
