package com.wanderwildwood.ekikyo.iching

import org.junit.Assert.assertEquals
import org.junit.Test

class KingWenTest {
    @Test fun sixtyFourDifferentFiguresNumberedInOrder() {
        assertEquals((1..64).toList(), KingWen.all.map { it.number })
        assertEquals(64, KingWen.all.map { it.bits }.toSet().size)
    }

    @Test fun everyFigureFindsItself() {
        for (h in KingWen.all) assertEquals(h, KingWen.ofBits(h.bits))
    }

    /** Unicode's hexagram block (U+4DC0) is in King Wen order: check each figure against it. */
    @Test fun matchesTheUnicodeNames() {
        // The doubled trigrams, where the name says which trigram it is.
        val doubled = mapOf(1 to Trigram.HEAVEN, 2 to Trigram.EARTH, 29 to Trigram.WATER, 30 to Trigram.FIRE,
            51 to Trigram.THUNDER, 52 to Trigram.MOUNTAIN, 57 to Trigram.WIND, 58 to Trigram.LAKE)
        for ((n, t) in doubled) {
            val h = KingWen.number(n)
            assertEquals("hexagram $n", t, h.upper)
            assertEquals("hexagram $n", t, h.lower)
        }
    }

    /** Pairs in King Wen order are each other upside down, or, when that is the same, each other's opposite. */
    @Test fun kingWenPairs() {
        fun flip(b: Int) = (0 until 6).fold(0) { acc, i -> if ((b shr i) and 1 == 1) acc or (1 shl (5 - i)) else acc }
        for (k in 0 until 32) {
            val a = KingWen.all[2 * k].bits
            val b = KingWen.all[2 * k + 1].bits
            val expected = if (flip(a) == a) a xor 0b111111 else flip(a)
            assertEquals("pair ${2 * k + 1}/${2 * k + 2}", expected, b)
        }
    }

    @Test fun firstLineIsTheBottomBit() {
        // 24 Fu, Return: one whole line at the foot under five broken ones.
        assertEquals(0b000001, KingWen.number(24).bits)
        assertEquals(0b100000, KingWen.number(23).bits)
    }
}
