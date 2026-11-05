package com.wanderwildwood.ekikyo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.text_field.TextFieldMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import com.wanderwildwood.ekikyo.R
import com.wanderwildwood.ekikyo.iching.Book
import com.wanderwildwood.ekikyo.iching.Hexagram
import com.wanderwildwood.ekikyo.iching.KingWen
import com.wanderwildwood.ekikyo.iching.Reading

internal val SIDE = 20.dp

/** The words a reading shows, gathered once in composition so the list's items need no lookups. */
class ReadingWords(
    val names: Names,
    val judgment: String,
    val image: String,
    val changingHead: String,
    val becomesHead: String,
)

@Composable
fun rememberReadingWords(): ReadingWords = ReadingWords(
    names = rememberNames(),
    judgment = stringResource(R.string.head_judgment),
    image = stringResource(R.string.head_image),
    changingHead = stringResource(R.string.head_changing),
    becomesHead = stringResource(R.string.head_becomes),
)

/**
 * A cast read out: the hexagram and the one it becomes side by side, then Legge's judgment and
 * image, the changing lines only, and the judgment of the hexagram it becomes. Each paragraph
 * is its own item, so pages turn between paragraphs and never cut one.
 */
fun LazyListScope.readingItems(reading: Reading, book: Book, w: ReadingWords, go: (Screen) -> Unit) {
    val primary = reading.primary
    val relating = reading.relating
    val text = book.text(primary.number)
    item(key = "figures") {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = SIDE, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            FigureWithName(reading.lines, primary, w.names, Modifier.weight(1f), go)
            if (relating != null) {
                TextMMD(text = "→", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 22.dp))
                FigureWithName(youngLines(relating.bits), relating, w.names, Modifier.weight(1f), go)
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
    item(key = "j") { Passage(w.judgment, text.judgment) }
    item(key = "i") { Passage(w.image, text.image) }
    if (relating != null) {
        // The heading goes with the first changing line, so a page never ends on it alone.
        reading.changing.forEachIndexed { k, i ->
            item(key = "l$i") {
                if (k == 0) Passage(w.changingHead, text.lines[i]) else Body(text.lines[i])
            }
        }
        if (reading.changing.size == 6 && text.allLines != null) item(key = "all") { Body(text.allLines) }
        item(key = "b") {
            Passage(
                buildAnnotatedString {
                    append("${w.becomesHead} · ${relating.number} ")
                    hanzi(relating.hanzi)
                    append(" ${relating.pinyin}, ${w.names.of(relating)}")
                },
                book.text(relating.number).judgment,
            )
        }
    }
}

@Composable
private fun FigureWithName(lines: List<Int>, h: Hexagram, names: Names, modifier: Modifier, go: (Screen) -> Unit) {
    Column(
        modifier.clickable { go(Screen.Hexagram(h.number)) }.testTag("figure_${h.number}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Figure(lines, 78.dp, description = "${h.number} ${names.of(h)}")
        Spacer(Modifier.height(10.dp))
        TextMMD(text = names.title(h), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        TextMMD(text = names.of(h), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Passage(heading: String, body: String) = Passage(AnnotatedString(heading), body)

@Composable
fun Passage(heading: AnnotatedString, body: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = SIDE)) {
        TextMMD(
            text = heading,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 18.dp, bottom = 4.dp),
        )
        TextMMD(text = body, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
fun Body(body: String) {
    TextMMD(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = SIDE, vertical = 6.dp),
    )
}

/** A reading from the journal: its date and question, the reading, the note, and the way to let it go. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(state: AppState, id: Long, onBack: () -> Unit, go: (Screen) -> Unit) {
    val entry = state.entry(id)
    if (entry == null) {
        // Deleted from under this screen: leave it, once, after this frame.
        LaunchedEffect(id) { onBack() }
        return
    }
    val words = rememberReadingWords()
    var editing by remember { mutableStateOf(false) }
    val method = stringResource(methodName(entry.method))

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBarMMD(
                title = { TextMMD(text = stringResource(if (entry.daily) R.string.reading_daily else R.string.reading_title)) },
                navigationIcon = { BarButton(Icons.Back, stringResource(R.string.cd_back), onBack) },
            )
        },
    ) { padding ->
        PagedColumn(Modifier.padding(padding).fillMaxSize().testTag("reading")) {
            item(key = "date") {
                Column(Modifier.padding(horizontal = SIDE).padding(top = 10.dp)) {
                    TextMMD(text = "${longDate(entry.epochDay)} · $method", style = MaterialTheme.typography.labelSmall)
                    if (entry.question.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        TextMMD(text = entry.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            readingItems(entry.reading, state.book, words, go)
            item(key = "note") {
                Column(Modifier.padding(horizontal = SIDE)) {
                    if (entry.note.isNotBlank()) {
                        Heading(stringResource(R.string.note))
                        TextMMD(text = entry.note, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    HouseButton(stringResource(if (entry.note.isBlank()) R.string.note_add else R.string.note_edit), onClick = { editing = true }, modifier = WideRow)
                    ArmedRow(stringResource(R.string.delete_reading), stringResource(R.string.delete_reading_armed)) {
                        state.delete(entry.id)
                        onBack()
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (editing) {
        NoteDialog(entry.note, onDismiss = { editing = false }) { state.put(entry.copy(note = it.trim())) }
    }
}

/** One of the 64 on its own page: its figure, its two trigrams, and all of Legge's text for it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexagramScreen(state: AppState, number: Int, onBack: () -> Unit, go: (Screen) -> Unit) {
    val h = KingWen.number(number)
    val text = state.book.text(number)
    val names = rememberNames()
    val above = stringResource(R.string.trigram_above)
    val below = stringResource(R.string.trigram_below)
    val lines = stringResource(R.string.head_lines)
    val judgment = stringResource(R.string.head_judgment)
    val image = stringResource(R.string.head_image)
    val legge = stringResource(R.string.legge_name, text.name)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBarMMD(
                title = { TextMMD(text = "$number  ${names.of(h)}") },
                navigationIcon = { BarButton(Icons.Back, stringResource(R.string.cd_back), onBack) },
            )
        },
    ) { padding ->
        PagedColumn(Modifier.padding(padding).fillMaxSize().testTag("hexagram")) {
            item(key = "head") {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = SIDE, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Figure(youngLines(h.bits), 96.dp, marks = false, description = names.of(h))
                    Spacer(Modifier.width(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextMMD(text = buildAnnotatedString { hanzi(h.hanzi) }, style = MaterialTheme.typography.headlineMedium)
                        TextMMD(text = h.pinyin, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        TextMMD(text = legge, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item(key = "trigrams") {
                Column(Modifier.padding(horizontal = SIDE)) {
                    TextMMD(text = trigramLine(above, h.upper, names), style = MaterialTheme.typography.bodyMedium)
                    TextMMD(text = trigramLine(below, h.lower, names), style = MaterialTheme.typography.bodyMedium)
                }
            }
            item(key = "j") { Passage(judgment, text.judgment) }
            item(key = "i") { Passage(image, text.image) }
            text.lines.forEachIndexed { i, l -> item(key = "l$i") { if (i == 0) Passage(lines, l) else Body(l) } }
            text.allLines?.let { item(key = "all") { Body(it) } }
            item(key = "foot") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun NoteDialog(start: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(start) }
    EInkDialog(onDismiss = onDismiss) {
        TextMMD(text = stringResource(R.string.note), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        TextFieldMMD(
            value = text,
            onValueChange = { text = it },
            placeholder = { TextMMD(text = stringResource(R.string.note_hint), style = MaterialTheme.typography.bodyMedium) },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth().testTag("note_field"),
        )
        Spacer(Modifier.height(12.dp))
        HouseButton(stringResource(R.string.save), on = true, modifier = WideRow, onClick = {
            onSave(text)
            onDismiss()
        })
        HouseButton(stringResource(R.string.cancel), onClick = onDismiss, modifier = WideRow)
    }
}

fun methodName(m: com.wanderwildwood.ekikyo.iching.Method): Int = when (m) {
    com.wanderwildwood.ekikyo.iching.Method.COINS -> R.string.method_coins
    com.wanderwildwood.ekikyo.iching.Method.YARROW -> R.string.method_yarrow
}
