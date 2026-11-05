package com.wanderwildwood.ekikyo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.text_field.TextFieldMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import com.wanderwildwood.ekikyo.R
import com.wanderwildwood.ekikyo.iching.Book
import com.wanderwildwood.ekikyo.iching.Hexagram
import com.wanderwildwood.ekikyo.iching.KingWen
import com.wanderwildwood.ekikyo.iching.Reading
import com.wanderwildwood.ekikyo.iching.Trigram

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
        if (relating == null) {
            Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                FigureWithName(reading.lines, primary, w.names, 96.dp, Modifier.width(200.dp), go)
            }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                FigureWithName(reading.lines, primary, w.names, 80.dp, Modifier.weight(1f), go)
                TextMMD(text = "→", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 36.dp))
                FigureWithName(youngLines(relating.bits), relating, w.names, 80.dp, Modifier.weight(1f), go)
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
                    append("${w.becomesHead} ${relating.number}, ${w.names.of(relating)}  ")
                    hanzi(relating.hanzi)
                },
                book.text(relating.number).judgment,
            )
        }
    }
    item(key = "end") { Spacer(Modifier.height(12.dp)) }
}

/**
 * A figure over its character, its English name and its number and pinyin, all centered; a press
 * opens the hexagram's own page. Both figures of a pair keep room for the marks (a figure of young
 * lines draws none), so they stand level.
 */
@Composable
private fun FigureWithName(
    lines: List<Int>, h: Hexagram, names: Names, size: Dp, modifier: Modifier, go: (Screen) -> Unit,
) {
    Column(
        modifier.clickable { go(Screen.Hexagram(h.number)) }.padding(vertical = 4.dp).testTag("figure_${h.number}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Figure(lines, size, description = "${h.number} ${names.of(h)}")
        Spacer(Modifier.height(12.dp))
        HexagramName(h, names)
    }
}

/** 屯 over "Sprouting" over "3 · Zhūn", centered. */
@Composable
fun HexagramName(h: Hexagram, names: Names, hanziStyle: TextStyle = MaterialTheme.typography.headlineMedium) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextMMD(text = h.hanzi, style = hanziStyle, fontFamily = HanziFont, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        TextMMD(
            text = names.of(h), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, maxLines = 2,
        )
        TextMMD(text = "${h.number} · ${h.pinyin}", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}

/** The date and how the cast was made on one quiet line, then the question if there was one. */
@Composable
fun ReadingHead(epochDay: Long, method: String, question: String) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = SIDE).padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextMMD(
            text = "${shortDate(epochDay)} · $method", style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center, maxLines = 1,
        )
        if (question.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            TextMMD(text = question, style = rubricStyle, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun Passage(heading: String, body: String) = Passage(AnnotatedString(heading), body)

/** A heading in the book's italic over a paragraph of Legge, kept together as one row. */
@Composable
fun Passage(heading: AnnotatedString, body: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = SIDE)) {
        Rule(Modifier.padding(top = 18.dp, bottom = 10.dp))
        TextMMD(text = heading, style = rubricStyle, modifier = Modifier.padding(bottom = 6.dp))
        TextMMD(text = body, style = bookStyle)
        Spacer(Modifier.height(4.dp))
    }
}

/** A paragraph of Legge under the one before, with the same heading. */
@Composable
fun Body(body: String) {
    TextMMD(
        text = body,
        style = bookStyle,
        modifier = Modifier.fillMaxWidth().padding(horizontal = SIDE).padding(top = 12.dp, bottom = 4.dp),
    )
}

/** A short line of ink at the head of a section, the way a book marks one. */
@Composable
fun Rule(modifier: Modifier = Modifier) {
    Box(modifier.width(28.dp).height(2.dp).background(MaterialTheme.colorScheme.onSurface))
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
            item(key = "date") { ReadingHead(entry.epochDay, method, entry.question) }
            readingItems(entry.reading, state.book, words, go)
            item(key = "note") {
                Column(Modifier.padding(horizontal = SIDE)) {
                    if (entry.note.isNotBlank()) {
                        Rule(Modifier.padding(top = 18.dp, bottom = 10.dp))
                        TextMMD(text = stringResource(R.string.note), style = rubricStyle, modifier = Modifier.padding(bottom = 6.dp))
                        TextMMD(text = entry.note, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(20.dp))
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
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = SIDE).padding(top = 16.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Figure(youngLines(h.bits), 96.dp, marks = false, description = names.of(h))
                    Spacer(Modifier.height(14.dp))
                    HexagramName(h, names, MaterialTheme.typography.headlineLarge)
                    TextMMD(text = legge, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                }
            }
            item(key = "trigrams") {
                Column(Modifier.fillMaxWidth().padding(horizontal = SIDE).padding(top = 14.dp)) {
                    TrigramRow(above, h.upper, names)
                    Spacer(Modifier.height(10.dp))
                    TrigramRow(below, h.lower, names)
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

/** A trigram's three lines beside "Above · Water" and "坎 Kǎn". */
@Composable
private fun TrigramRow(label: String, t: Trigram, names: Names) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
            Figure(youngLines(t.bits, rows = 3), 48.dp, marks = false, rows = 3)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            TextMMD(text = "$label · ${names.of(t)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            TextMMD(text = buildAnnotatedString { hanzi(t.hanzi); append(" ${t.pinyin}") }, style = MaterialTheme.typography.labelSmall)
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
