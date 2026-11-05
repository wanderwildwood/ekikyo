package com.wanderwildwood.ekikyo.iching

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalTest {
    private val a = Entry(1_700_000_000_000, 19_700, listOf(9, 8, 8, 8, 7, 8), Method.COINS, question = "Ada Whitlock’s garden?", note = "rain")
    private val b = Entry(1_700_000_000_001, 19_701, listOf(7, 7, 7, 7, 7, 7), Method.YARROW, daily = true)

    @Test fun roundTrips() {
        assertEquals(listOf(a, b), JournalCodec.decode(JournalCodec.encode(listOf(a, b))))
        assertEquals(listOf(a, b), JournalCodec.decodeStrict(JournalCodec.encode(listOf(a, b))))
    }

    @Test fun exportNamesTheHexagrams() {
        val text = JournalCodec.encode(listOf(a)) { "name$it" }
        assertTrue(text.contains("3 name3"))
        assertTrue(text.contains("8 name8"))
    }

    @Test fun badEntriesAreSkippedInTheAppsOwnFile() {
        val text = org.json.JSONObject(JournalCodec.encode(listOf(a, b)))
            .also { it.getJSONArray("readings").getJSONObject(1).put("method", "sticks") }.toString()
        assertEquals(listOf(a), JournalCodec.decode(text))
    }

    private fun kind(text: String) = try {
        JournalCodec.decodeStrict(text); null
    } catch (e: JournalCodec.JournalException) { e.kind }

    @Test fun strictRefusesWhatIsNotWhole() {
        assertEquals(JournalCodec.JournalException.Kind.NOT_A_JOURNAL, kind("hello"))
        assertEquals(JournalCodec.JournalException.Kind.NOT_A_JOURNAL, kind("{\"readings\":[]}"))
        assertEquals(JournalCodec.JournalException.Kind.NEWER, kind("{\"version\":99,\"readings\":[]}"))
        val good = JournalCodec.encode(listOf(a))
        val badLine = org.json.JSONObject(good).also { it.getJSONArray("readings").getJSONObject(0).getJSONArray("lines").put(0, 5) }
        assertEquals(JournalCodec.JournalException.Kind.DAMAGED, kind(badLine.toString()))
        assertEquals(JournalCodec.JournalException.Kind.DAMAGED, kind(JournalCodec.encode(listOf(a, a))))
    }
}
