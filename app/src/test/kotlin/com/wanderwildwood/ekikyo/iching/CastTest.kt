package com.wanderwildwood.ekikyo.iching

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class CastTest {
    private fun share(method: Method, n: Int = 160_000): Map<Int, Double> {
        val r = Random(7)
        return (0 until n).map { Cast.line(method, r) }.groupingBy { it }.eachCount().mapValues { it.value.toDouble() / n }
    }

    private fun near(expected: Double, actual: Double?) =
        assertTrue("expected about $expected, got $actual", actual != null && kotlin.math.abs(expected - actual) < 0.006)

    @Test fun coinsGiveOneThreeThreeOneInEight() {
        val s = share(Method.COINS)
        assertEquals(setOf(6, 7, 8, 9), s.keys)
        near(1 / 8.0, s[6]); near(3 / 8.0, s[7]); near(3 / 8.0, s[8]); near(1 / 8.0, s[9])
    }

    @Test fun yarrowGivesOneFiveSevenThreeInSixteen() {
        val s = share(Method.YARROW)
        near(1 / 16.0, s[6]); near(5 / 16.0, s[7]); near(7 / 16.0, s[8]); near(3 / 16.0, s[9])
    }

    @Test fun noChangingLinesHasNoSecondHexagram() {
        val r = Reading(listOf(7, 7, 7, 7, 7, 7))
        assertEquals(1, r.primary.number)
        assertNull(r.relating)
        assertEquals(emptyList<Int>(), r.changing)
    }

    @Test fun allOldYangBecomesEarth() {
        val r = Reading(listOf(9, 9, 9, 9, 9, 9))
        assertEquals(1, r.primary.number)
        assertEquals(2, r.relating!!.number)
        assertEquals((0..5).toList(), r.changing)
    }

    @Test fun oneChangingLineAtTheFoot() {
        // 3 Zhun (water over thunder) with its first line, whole, changing: 8 Bi (water over earth).
        val r = Reading(listOf(9, 8, 8, 8, 7, 8))
        assertEquals(3, r.primary.number)
        assertEquals(8, r.relating!!.number)
        assertEquals(listOf(0), r.changing)
    }

    @Test fun oldYinTurnsWhole() {
        // 2 Kun with its top line changing: mountain over earth, 23 Bo.
        assertEquals(23, Reading(listOf(8, 8, 8, 8, 8, 6)).relating!!.number)
    }

    @Test(expected = IllegalArgumentException::class)
    fun fiveLinesAreNotAReading() { Reading(listOf(7, 7, 7, 7, 7)) }
}
