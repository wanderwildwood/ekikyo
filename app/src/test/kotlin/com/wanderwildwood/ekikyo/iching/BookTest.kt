package com.wanderwildwood.ekikyo.iching

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The text the app carries: whole, in order, and free of what the scans' pages left behind. */
class BookTest {
    private val book = Book.parse(File("src/main/assets/legge.json").readText())

    @Test fun allSixtyFourWithSixLines() {
        assertEquals((1..64).toList(), book.texts.map { it.number })
        assertTrue(book.texts.all { t -> t.lines.size == 6 && t.lines.all { it.isNotBlank() } })
    }

    @Test fun onlyTheFirstTwoHaveASeventhText() {
        assertNotNull(book.text(1).allLines)
        assertNotNull(book.text(2).allLines)
        for (n in 3..64) assertNull("hexagram $n", book.text(n).allLines)
    }

    @Test fun noPageFurnitureOrOcrDebris() {
        val bad = Regex("""¬|THE Y[IÎ] KING|APPENDIX|HEX\. \d|\s{2,}|\bSECTION\b""")
        val problems = book.texts.flatMap { t ->
            (listOf(t.judgment, t.image) + t.lines + listOfNotNull(t.allLines)).filter { bad.containsMatchIn(it) }.map { "${t.number}: $it" }
        }
        assertEquals(emptyList<String>(), problems)
    }
}
