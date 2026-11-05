package com.wanderwildwood.ekikyo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import com.wanderwildwood.ekikyo.R
import com.wanderwildwood.ekikyo.Store
import com.wanderwildwood.ekikyo.iching.Book
import com.wanderwildwood.ekikyo.iching.Cast
import com.wanderwildwood.ekikyo.iching.Entry
import com.wanderwildwood.ekikyo.iching.Method
import java.time.LocalDate

/** Where the app is. The tabs are one place; everything else is stacked on top of them. */
sealed interface Screen {
    data object Tabs : Screen
    data class Reading(val id: Long) : Screen
    data class Hexagram(val number: Int) : Screen
    data object Settings : Screen
    data object Guide : Screen
}

/** The journal, the setting, the day's cast and a cast in progress, held so a change redraws what shows them. */
class AppState(val store: Store, val book: Book) {
    var version by mutableIntStateOf(0)
        private set
    val entries get() = version.let { store.entries() }
    var method: Method
        get() = version.let { store.method }
        set(v) { store.method = v; version++ }

    fun entry(id: Long) = entries.firstOrNull { it.id == id }

    /** Today's lines and how they were cast, once cast. */
    fun daily(epochDay: Long) = version.let { store.daily(epochDay) }

    fun castDaily(epochDay: Long) {
        val m = store.method
        store.setDaily(epochDay, Cast.six(m), m)
        version++
    }

    /** The lines of the cast under way on the Cast tab, from the bottom up, and its question. */
    val building = mutableStateListOf<Int>()
    var question by mutableStateOf("")

    fun castLine() { if (building.size < 6) building.add(Cast.line(store.method)) }

    fun castRest() { while (building.size < 6) building.add(Cast.line(store.method)) }

    /** Keeps the finished cast in the journal and clears the Cast tab for the next. */
    fun finish(): Entry {
        val e = Entry(
            id = nextId(),
            epochDay = LocalDate.now().toEpochDay(),
            lines = building.toList(),
            method = store.method,
            question = question.trim(),
        )
        put(e)
        building.clear()
        question = ""
        return e
    }

    fun keepDaily(epochDay: Long, lines: List<Int>, method: Method) =
        put(Entry(nextId(), epochDay, lines, method, daily = true))

    private fun nextId() = maxOf(System.currentTimeMillis(), (store.entries().maxOfOrNull { it.id } ?: 0) + 1)

    /** Adds the readings of a restored journal; returns how many were new. */
    fun restore(readings: List<Entry>): Int = store.merge(readings).also { version++ }

    fun put(e: Entry) { store.put(e); version++ }

    /** The reading just deleted, kept until the next thing is done so it can be put back. */
    var deleted by mutableStateOf<Entry?>(null)
        private set

    fun delete(id: Long) {
        deleted = store.entries().firstOrNull { it.id == id }
        store.delete(id)
        version++
    }
    fun undelete() { deleted?.let { put(it) }; deleted = null }
    fun forgetDeleted() { deleted = null }
}

@Composable
fun IChingApp(store: Store) {
    val context = LocalContext.current
    val state = remember {
        AppState(store, Book.parse(context.assets.open("legge.json").bufferedReader().use { it.readText() }))
    }
    val stack = remember { mutableStateListOf<Screen>(Screen.Tabs) }
    var tab by remember { mutableIntStateOf(0) }
    var about by remember { mutableStateOf(false) }

    fun go(s: Screen) { state.forgetDeleted(); stack.add(s) }
    fun back() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }

    if (stack.size > 1) BackHandler { back() }

    when (val s = stack.last()) {
        Screen.Tabs -> Tabs(state, tab, onTab = { tab = it }, go = ::go)
        is Screen.Reading -> ReadingScreen(state, s.id, onBack = ::back, go = ::go)
        is Screen.Hexagram -> HexagramScreen(state, s.number, onBack = ::back, go = ::go)
        Screen.Settings -> SettingsScreen(state, onBack = ::back, onAbout = { about = true }, onGuide = { go(Screen.Guide) })
        Screen.Guide -> GuideScreen(onBack = ::back)
    }
    if (about) AboutDialog(onDismiss = { about = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Tabs(state: AppState, tab: Int, onTab: (Int) -> Unit, go: (Screen) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBarMMD(
                title = { TextMMD(text = stringResource(R.string.app_name)) },
                actions = { BarButton(Icons.Settings, stringResource(R.string.cd_settings)) { go(Screen.Settings) } },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            StoneTabs(
                labels = listOf(
                    stringResource(R.string.tab_today),
                    stringResource(R.string.tab_cast),
                    stringResource(R.string.tab_journal),
                    stringResource(R.string.tab_book),
                ),
                selected = tab,
                onSelect = { state.forgetDeleted(); onTab(it) },
            )
            state.deleted?.let { UndoBar(onUndo = state::undelete) }
            when (tab) {
                0 -> TodayTab(state, go)
                1 -> CastTab(state, go)
                2 -> JournalTab(state, go, onTab)
                else -> BookTab(go)
            }
        }
    }
}

/** Under the tabs after a reading is deleted, whichever tab is open: the way to put it back. */
@Composable
private fun UndoBar(onUndo: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 6.dp).testTag("undo_bar")) {
        TextMMD(text = stringResource(R.string.deleted_done), style = MaterialTheme.typography.bodyMedium)
        HouseButton(stringResource(R.string.deleted_undo), onClick = onUndo, modifier = WideRow)
    }
}
