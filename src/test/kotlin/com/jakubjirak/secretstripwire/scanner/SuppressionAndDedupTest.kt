package com.jakubjirak.secretstripwire.scanner

import com.jakubjirak.secretstripwire.model.SecretPattern
import com.jakubjirak.secretstripwire.model.SecretType
import com.jakubjirak.secretstripwire.model.SeverityLevel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SuppressionAndDedupTest {

    @Test
    fun `tripwire-ignore comment suppresses findings on that line`() {
        val text = """
            key1 = AKIAIOSFODNN7EXAMPLE // tripwire:ignore
            key2 = AKIAIOSFODNN7EXAMPLE
        """.trimIndent()
        val findings = SecretScanner.scan(text)
        assertEquals(1, findings.size)
        assertEquals(2, findings[0].lineNumber)
    }

    @Test
    fun `hash style suppression works for properties and yaml`() {
        val text = "password = \"supersecretvalue123\" # tripwire:ignore"
        assertTrue(SecretScanner.scan(text).isEmpty())
    }

    @Test
    fun `suppression marker on a different line does not affect findings`() {
        val text = """
            # tripwire:ignore
            key = AKIAIOSFODNN7EXAMPLE
        """.trimIndent()
        assertEquals(1, SecretScanner.scan(text).size)
    }

    @Test
    fun `filterSuppressed is a no-op without the marker`() {
        val text = "key = AKIAIOSFODNN7EXAMPLE"
        val findings = SecretScanner.scan(text)
        assertEquals(findings, SecretScanner.filterSuppressed(text, findings))
    }

    @Test
    fun `same-offset matches collapse to the most severe finding`() {
        val warningTwin = SecretPattern(
            id = "aws_twin",
            name = "AWS Twin",
            regex = """AKIA[0-9A-Z]{16}""",
            secretType = SecretType.GENERIC_SECRET,
            severity = SeverityLevel.WARNING,
        )
        val findings = SecretScanner.scan(
            "AKIAIOSFODNN7EXAMPLE",
            SecretScanner.BUILTIN_PATTERNS + warningTwin,
        )
        assertEquals(1, findings.size)
        assertEquals(SeverityLevel.ERROR, findings[0].severity)
        assertEquals("aws_access_key", findings[0].pattern.id)
    }

    @Test
    fun `findings are sorted by offset`() {
        val text = """
            b = ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn
            a = AKIAIOSFODNN7EXAMPLE
        """.trimIndent()
        val findings = SecretScanner.scan(text)
        assertEquals(2, findings.size)
        assertTrue(findings[0].startOffset < findings[1].startOffset)
    }
}
