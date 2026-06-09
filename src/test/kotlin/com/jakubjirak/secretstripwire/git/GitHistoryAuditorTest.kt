package com.jakubjirak.secretstripwire.git

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class GitHistoryAuditorTest {

    @Test
    fun `parseGitDate parses the default git log format`() {
        val parsed = GitHistoryAuditor.parseGitDate("Mon Jun 8 12:34:56 2026 +0200")
        val expected = ZonedDateTime.of(2026, 6, 8, 12, 34, 56, 0, ZoneOffset.ofHours(2)).toInstant()
        assertEquals(expected, parsed)
    }

    @Test
    fun `parseGitDate falls back to now for unknown formats`() {
        val before = Instant.now()
        val parsed = GitHistoryAuditor.parseGitDate("2026-06-08")
        val after = Instant.now()
        assertTrue(parsed >= before && parsed <= after)
    }

    @Test
    fun `parseGitDate handles null`() {
        val before = Instant.now()
        assertTrue(GitHistoryAuditor.parseGitDate(null) >= before.minusSeconds(1))
    }
}
