package com.wanderwildwood.ekikyo.iching

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * One reading as kept: when, the question if one was asked, how it was cast, the six lines
 * from the bottom up, and a note. [id] is the moment it was cast, in milliseconds, and is
 * unique. [daily] marks the hexagram for the day.
 */
data class Entry(
    val id: Long,
    val epochDay: Long,
    val lines: List<Int>,
    val method: Method,
    val question: String = "",
    val note: String = "",
    val daily: Boolean = false,
) {
    val reading: Reading get() = Reading(lines)
}

/** The journal in and out of JSON. The app's own file and the export share the shape. */
object JournalCodec {
    const val VERSION = 1

    /** [names] adds each hexagram's name to the export, for a person reading the file. */
    fun encode(entries: List<Entry>, names: ((Int) -> String)? = null): String {
        val list = JSONArray()
        for (e in entries) {
            val o = JSONObject()
                .put("id", e.id)
                .put("date", LocalDate.ofEpochDay(e.epochDay).toString())
                .put("lines", JSONArray(e.lines))
                .put("method", e.method.key)
                .put("question", e.question)
                .put("note", e.note)
                .put("daily", e.daily)
            if (names != null) {
                val r = e.reading
                o.put("hexagram", "${r.primary.number} ${names(r.primary.number)}")
                r.relating?.let { o.put("becomes", "${it.number} ${names(it.number)}") }
            }
            list.put(o)
        }
        return JSONObject().put("version", VERSION).put("readings", list).toString(1)
    }

    /** Entries that cannot be read are skipped rather than losing the rest. */
    fun decode(text: String): List<Entry> {
        if (text.isBlank()) return emptyList()
        val list = JSONObject(text).optJSONArray("readings") ?: return emptyList()
        val out = ArrayList<Entry>(list.length())
        for (i in 0 until list.length()) {
            runCatching { out += entry(list.getJSONObject(i)) }
        }
        return out
    }

    private fun entry(o: JSONObject): Entry {
        val ls = o.getJSONArray("lines")
        val lines = (0 until ls.length()).map { ls.getInt(it) }
        require(lines.size == 6 && lines.all(Line::valid))
        return Entry(
            id = o.getLong("id").also { require(it > 0) },
            epochDay = LocalDate.parse(o.getString("date")).toEpochDay(),
            lines = lines,
            method = Method.of(o.getString("method"))!!,
            question = o.optString("question"),
            note = o.optString("note"),
            daily = o.optBoolean("daily"),
        )
    }

    /** Why a file was refused, as a kind the screen turns into a plain sentence. */
    class JournalException(val kind: Kind) : Exception(kind.name) {
        enum class Kind { NOT_A_JOURNAL, NEWER, DAMAGED }
    }

    /**
     * Reads a journal file whole, for a restore: unlike [decode], which skips what it cannot
     * read so the app's own file never loses the rest, this refuses the whole file if any
     * reading in it is unreadable or repeats an id, or if the file is not a journal export at
     * all. Nothing is half-read.
     */
    fun decodeStrict(text: String): List<Entry> {
        val root = try {
            JSONObject(text)
        } catch (e: Exception) {
            throw JournalException(JournalException.Kind.NOT_A_JOURNAL)
        }
        val list = root.optJSONArray("readings") ?: throw JournalException(JournalException.Kind.NOT_A_JOURNAL)
        if (!root.has("version")) throw JournalException(JournalException.Kind.NOT_A_JOURNAL)
        val version = root.optInt("version", -1)
        if (version > VERSION) throw JournalException(JournalException.Kind.NEWER)
        if (version < 1) throw JournalException(JournalException.Kind.DAMAGED)
        try {
            val out = (0 until list.length()).map { entry(list.getJSONObject(it)) }
            require(out.map { it.id }.toSet().size == out.size)
            return out
        } catch (e: Exception) {
            throw JournalException(JournalException.Kind.DAMAGED)
        }
    }
}
