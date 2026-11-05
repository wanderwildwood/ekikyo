package com.wanderwildwood.ekikyo.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import com.wanderwildwood.ekikyo.R
import com.wanderwildwood.ekikyo.iching.JournalCodec
import com.wanderwildwood.ekikyo.iching.Method

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: AppState, onBack: () -> Unit, onAbout: () -> Unit, onGuide: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val names = rememberNames()
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val json = JournalCodec.encode(state.entries) { names.of(it) }
        runCatching {
            context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(json.toByteArray()) } ?: error("no stream")
        }.onSuccess {
            Toast.makeText(context, context.getString(R.string.export_done), Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, context.getString(R.string.export_failed), Toast.LENGTH_LONG).show()
        }
    }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = runCatching {
            context.contentResolver.openInputStream(uri)?.use { s ->
                val bytes = s.readBytes()
                if (bytes.size > 20 * 1024 * 1024) null else String(bytes, Charsets.UTF_8)
            }
        }.getOrNull()
        val message = if (text == null) {
            context.getString(R.string.restore_unreadable)
        } else {
            try {
                val readings = JournalCodec.decodeStrict(text)
                val added = state.restore(readings)
                context.getString(R.string.restore_done, added, readings.size - added)
            } catch (e: JournalCodec.JournalException) {
                context.getString(
                    when (e.kind) {
                        JournalCodec.JournalException.Kind.NOT_A_JOURNAL -> R.string.restore_not_ours
                        JournalCodec.JournalException.Kind.NEWER -> R.string.restore_newer
                        JournalCodec.JournalException.Kind.DAMAGED -> R.string.restore_damaged
                    },
                )
            } catch (e: Exception) {
                context.getString(R.string.restore_unreadable)
            }
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    val fileName = stringResource(R.string.export_file_name)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBarMMD(
                title = { TextMMD(text = stringResource(R.string.settings_title)) },
                navigationIcon = { BarButton(Icons.Back, stringResource(R.string.cd_back), onBack) },
                actions = { BarButton(Icons.Info, stringResource(R.string.cd_about), onAbout) },
            )
        },
    ) { padding ->
        PagedColumn(
            modifier = Modifier.padding(padding).fillMaxSize().testTag("settings"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 12.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item(key = "yarrow") {
                SwitchRow(
                    stringResource(R.string.settings_yarrow),
                    state.method == Method.YARROW,
                    note = stringResource(R.string.settings_yarrow_note),
                ) { state.method = if (it) Method.YARROW else Method.COINS }
            }
            item(key = "guide") { SettingRow(stringResource(R.string.settings_guide), null, onGuide) }
            item(key = "export") {
                SettingRow(stringResource(R.string.settings_export), stringResource(R.string.settings_export_note)) {
                    save.launch(fileName)
                }
            }
            item(key = "restore") {
                SettingRow(stringResource(R.string.settings_restore), stringResource(R.string.settings_restore_note)) {
                    runCatching { restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
                }
            }
        }
    }
}

private val GUIDE = listOf(
    R.string.guide_book to listOf(R.string.guide_book_1, R.string.guide_book_2, R.string.guide_book_3),
    R.string.guide_cast to listOf(R.string.guide_cast_1, R.string.guide_cast_2, R.string.guide_cast_3, R.string.guide_cast_4),
    R.string.guide_read to listOf(R.string.guide_read_1, R.string.guide_read_2, R.string.guide_read_3, R.string.guide_read_4),
    R.string.guide_text to listOf(R.string.guide_text_1, R.string.guide_text_2, R.string.guide_text_3),
)

/** A short guide to the book, the casting and the reading, one list of short sections, paged by swipe. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(onBack: () -> Unit) {
    val list = rememberLazyListState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBarMMD(
                title = { TextMMD(text = stringResource(R.string.guide_title)) },
                navigationIcon = { BarButton(Icons.Back, stringResource(R.string.cd_back), onBack) },
            )
        },
    ) { padding ->
        run {
            PagedColumn(state = list, modifier = Modifier.padding(padding).fillMaxSize().testTag("guide")) {
                for ((heading, paras) in GUIDE) {
                    item(key = "h$heading") {
                        TextMMD(
                            text = stringResource(heading),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 4.dp),
                        )
                    }
                    for (p in paras) {
                        item(key = "p$p") {
                            TextMMD(
                                text = stringResource(p),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 6.dp),
                            )
                        }
                    }
                    item(key = "r$heading") { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }
}
