package com.wanderwildwood.ekikyo

import android.content.Context
import com.wanderwildwood.ekikyo.iching.Entry
import com.wanderwildwood.ekikyo.iching.JournalCodec
import com.wanderwildwood.ekikyo.iching.Line
import com.wanderwildwood.ekikyo.iching.Method
import java.io.File
import java.util.Locale

/**
 * What the app keeps, all of it on the phone: the setting and the day's cast in a preferences
 * file, and the journal as one JSON file in the app's private storage, rewritten whole on each
 * change (a temporary file renamed over it, so a write cut short cannot leave half a journal).
 */
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("ekikyo", Context.MODE_PRIVATE)
    private val file = File(context.filesDir, "journal.json")

    var method: Method
        get() = Method.of(prefs.getString("method", null) ?: "") ?: Method.COINS
        set(v) { prefs.edit().putString("method", v.key).apply() }

    /** The I Ging text in German (Wilhelm) rather than English (Legge). */
    var germanText: Boolean
        get() = prefs.getBoolean("german_text", Locale.getDefault().language == "de")
        set(v) { prefs.edit().putBoolean("german_text", v).apply() }

    /** The hexagram cast for [epochDay], if it has been; kept so it is the same all day. */
    fun daily(epochDay: Long): Pair<List<Int>, Method>? {
        if (prefs.getLong("daily_day", Long.MIN_VALUE) != epochDay) return null
        val lines = prefs.getString("daily_lines", null)?.map { it.digitToIntOrNull() ?: -1 } ?: return null
        val method = Method.of(prefs.getString("daily_method", null) ?: "") ?: return null
        return if (lines.size == 6 && lines.all(Line::valid)) lines to method else null
    }

    fun setDaily(epochDay: Long, lines: List<Int>, method: Method) {
        prefs.edit()
            .putLong("daily_day", epochDay)
            .putString("daily_lines", lines.joinToString(""))
            .putString("daily_method", method.key)
            .apply()
    }

    private var cache: List<Entry>? = null

    /** Newest first. */
    fun entries(): List<Entry> =
        cache ?: (if (file.isFile) JournalCodec.decode(file.readText()) else emptyList())
            .sortedByDescending { it.id }
            .also { cache = it }

    fun put(e: Entry) = write(listOf(e) + entries().filter { it.id != e.id })

    fun delete(id: Long) = write(entries().filter { it.id != id })

    /**
     * Adds the readings of a restored file that are not here yet, in one write; a reading with
     * an id already in the journal is left as it is. Returns how many were added.
     */
    fun merge(restored: List<Entry>): Int {
        val have = entries()
        val ids = have.map { it.id }.toSet()
        val fresh = restored.filter { it.id !in ids }
        if (fresh.isNotEmpty()) write(have + fresh)
        return fresh.size
    }

    private fun write(list: List<Entry>) {
        val sorted = list.sortedByDescending { it.id }
        val tmp = File(file.parentFile, "journal.json.tmp")
        tmp.writeText(JournalCodec.encode(sorted))
        if (!tmp.renameTo(file)) error("could not save the journal")
        cache = sorted
    }
}
