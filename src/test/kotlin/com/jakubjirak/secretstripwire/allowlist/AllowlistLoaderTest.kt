package com.jakubjirak.secretstripwire.allowlist

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AllowlistLoaderTest {

    @Test
    fun `parses entries and skips comments and blanks`() {
        val content = """
            # known false positives
            AKIAIOSFODNN7EXAMPLE

            sample-token-for-docs
              padded-entry
        """.trimIndent()
        val entries = AllowlistLoader.parseAllowlist(content)
        assertEquals(listOf("AKIAIOSFODNN7EXAMPLE", "sample-token-for-docs", "padded-entry"), entries)
    }

    @Test
    fun `empty content yields no entries`() {
        assertTrue(AllowlistLoader.parseAllowlist("").isEmpty())
        assertTrue(AllowlistLoader.parseAllowlist("# only comments\n").isEmpty())
    }
}
