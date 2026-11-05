package com.wanderwildwood.ekikyo.iching

import java.security.SecureRandom
import java.util.Random

/**
 * A line as cast, by its traditional number: 6 old yin (broken, changing), 7 young yang (whole),
 * 8 young yin (broken), 9 old yang (whole, changing).
 */
object Line {
    const val OLD_YIN = 6
    const val YOUNG_YANG = 7
    const val YOUNG_YIN = 8
    const val OLD_YANG = 9

    fun valid(v: Int) = v in 6..9
    fun yang(v: Int) = v == YOUNG_YANG || v == OLD_YANG
    fun changing(v: Int) = v == OLD_YIN || v == OLD_YANG
    /** Whole after the change: an old line turns into its opposite, a young one stays. */
    fun yangAfter(v: Int) = if (changing(v)) !yang(v) else yang(v)
}

/**
 * How a line is made. Three coins give the changing lines 1 in 8 each; the yarrow stalks give
 * old yin 1 in 16 and old yang 3 in 16, so a whole line changes three times as often as a
 * broken one.
 */
enum class Method(val key: String) {
    COINS("coins"),
    YARROW("yarrow");

    companion object {
        fun of(key: String): Method? = entries.firstOrNull { it.key == key }
    }
}

object Cast {
    private val secure: Random = SecureRandom()

    /** One line by [method]. */
    fun line(method: Method, random: Random = secure): Int = when (method) {
        // Each coin is 3 heads up or 2 tails up; three of them sum to 6, 7, 8 or 9.
        Method.COINS -> (0 until 3).sumOf { if (random.nextBoolean()) 3 else 2 }
        // Sixteen parts: one old yin, five young yang, seven young yin, three old yang.
        Method.YARROW -> when (random.nextInt(16)) {
            0 -> Line.OLD_YIN
            in 1..5 -> Line.YOUNG_YANG
            in 6..12 -> Line.YOUNG_YIN
            else -> Line.OLD_YANG
        }
    }

    /** Six lines, from the bottom up. */
    fun six(method: Method, random: Random = secure): List<Int> = List(6) { line(method, random) }
}

/** Six cast lines, from the bottom up, read as the hexagram they make and the one they become. */
data class Reading(val lines: List<Int>) {
    init {
        require(lines.size == 6 && lines.all(Line::valid)) { "six lines of 6 to 9" }
    }

    val primary: Hexagram get() = KingWen.ofBits(bits { Line.yang(it) })

    /** Lines that change, counted from 0 at the bottom. */
    val changing: List<Int> get() = lines.indices.filter { Line.changing(lines[it]) }

    /** The hexagram the changing lines turn it into; null when no line changes. */
    val relating: Hexagram? get() = if (changing.isEmpty()) null else KingWen.ofBits(bits { Line.yangAfter(it) })

    private fun bits(whole: (Int) -> Boolean): Int =
        lines.foldIndexed(0) { i, acc, v -> if (whole(v)) acc or (1 shl i) else acc }
}
