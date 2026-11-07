package com.wanderwildwood.ekikyo.iching

import org.json.JSONObject

/**
 * One translator's words for one hexagram: the judgment, the six lines from the bottom up, the
 * seventh paragraph that hexagrams 1 and 2 have for when all six lines change, and the image.
 * [name] is the translator's spelling.
 */
class Text(
    val number: Int,
    val name: String,
    val judgment: String,
    val lines: List<String>,
    val allLines: String?,
    val image: String,
)

/** The whole of a translation as the app carries it, in assets/legge.json or assets/wilhelm.json. */
class Book(val source: String, val texts: List<Text>) {
    fun text(number: Int): Text = texts[number - 1]

    companion object {
        fun parse(json: String): Book {
            val root = JSONObject(json)
            val hs = root.getJSONArray("hexagrams")
            val texts = (0 until hs.length()).map { i ->
                val o = hs.getJSONObject(i)
                val ls = o.getJSONArray("lines")
                Text(
                    number = o.getInt("n"),
                    name = o.getString("name"),
                    judgment = o.getString("judgment"),
                    lines = (0 until ls.length()).map { ls.getString(it) },
                    allLines = o.optString("allLines").ifBlank { null },
                    image = o.getString("image"),
                )
            }
            require(texts.size == 64 && texts.withIndex().all { (i, t) -> t.number == i + 1 && t.lines.size == 6 })
            return Book(root.getString("source"), texts)
        }
    }
}
