package com.wanderwildwood.ekikyo.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReadAtMostTest {
    @Test
    fun `a file within the limit is read whole`() {
        assertEquals("{\"readings\":[]}", readAtMost("{\"readings\":[]}".byteInputStream(), 100))
    }

    @Test
    fun `a file past the limit is refused without reading on`() {
        val endless = object : java.io.InputStream() {
            var given = 0L
            override fun read(): Int { given++; return 'x'.code }
        }
        assertNull(readAtMost(endless, 1000))
        assert(endless.given < 1_000_000)
    }
}
