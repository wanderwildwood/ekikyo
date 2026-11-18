package com.wanderwildwood.ekikyo

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeptCopyTest {
    @Test
    fun `a copy kept under its own name is offered`() {
        assertTrue(isKeptCopy("journal.json.bad-0123456789abcdef"))
    }

    @Test
    fun `the journal, its temporary files and a bare prefix are not`() {
        assertFalse(isKeptCopy("journal.json"))
        assertFalse(isKeptCopy("journal.json.tmp"))
        assertFalse(isKeptCopy("journal.json.bad.tmp"))
        assertFalse(isKeptCopy("journal.json.bad-"))
    }
}
