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
        cache ?: read().sortedByDescending { it.id }.also { cache = it }

    /**
     * The journal file, read. When it cannot be read at all, or only in part (readings skipped,
     * or written by a newer version), a copy of it as it is is kept first, under a name taken
     * from its contents (journal.json.bad-<hash>), so a second damaged journal never replaces
     * the copy of the first and the same file is not copied again at each start. The journal
     * then goes on with what could be read: a crash at every start would leave clearing the
     * app's data, and the file with it, as the only way on. If the copy cannot be made, the
     * journal is not saved over until it can, rather than lose what was not read.
     */
    private fun read(): List<Entry> {
        if (!file.isFile) return emptyList()
        val bytes = file.readBytes()
        val got = try {
            JournalCodec.decodeOwn(String(bytes, Charsets.UTF_8))
        } catch (e: org.json.JSONException) {
            null
        }
        if (got == null || !got.whole) keepAside(bytes)
        return got?.entries ?: emptyList()
    }

    /** Set when a journal that was not read whole could not be copied aside. */
    private var unkept = false

    private fun keepAside(bytes: ByteArray) {
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
            .take(8).joinToString("") { "%02x".format(it) }
        val kept = File(file.parentFile, "journal.json.bad-$hash")
        if (kept.isFile && kept.length() == bytes.size.toLong()) return
        val tmp = File(file.parentFile, "journal.json.bad.tmp")
        unkept = !runCatching {
            java.io.FileOutputStream(tmp).use { out ->
                out.write(bytes)
                out.fd.sync()
            }
            if (!tmp.renameTo(kept)) error("could not keep the journal")
        }.isSuccess
    }

    /**
     * The newest copy kept of a journal that was not read whole, if there is one, so it can be
     * saved out of the app's private storage without adb. The journal is read first, as that is
     * when a copy is made.
     */
    fun keptCopy(): File? {
        entries()
        val dir = file.parentFile ?: return null
        return dir.listFiles { f -> f.isFile && isKeptCopy(f.name) }?.maxByOrNull { it.lastModified() }
    }

    /** The name of the kept copy the Journal tab has already told about, so it says so once. */
    var keptToldOf: String?
        get() = prefs.getString("kept_told_of", null)
        set(v) { prefs.edit().putString("kept_told_of", v).apply() }

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
        // Saving now would replace readings that could not be read and could not be kept.
        if (unkept) error("could not keep the unread journal")
        val sorted = list.sortedByDescending { it.id }
        val tmp = File(file.parentFile, "journal.json.tmp")
        java.io.FileOutputStream(tmp).use { out ->
            out.write(JournalCodec.encode(sorted).toByteArray(Charsets.UTF_8))
            // On the disk before the rename, so a power cut cannot leave an empty journal behind it.
            out.fd.sync()
        }
        if (!tmp.renameTo(file)) error("could not save the journal")
        cache = sorted
    }
}

/** A copy kept by Store.keepAside, not the temporary file it is written to first. */
internal fun isKeptCopy(name: String): Boolean =
    name.startsWith("journal.json.bad-") && name.length > "journal.json.bad-".length
