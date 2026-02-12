package com.jakubjirak.secretstripwire.scanner

import com.jakubjirak.secretstripwire.model.SecretType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecretScannerTest {

    @Test
    fun testAwsAccessKeyDetection() {
        val text = "aws_access_key_id = AKIAIOSFODNN7EXAMPLE"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect AWS access key")
        assertEquals(SecretType.AWS_KEY, findings.first().secretType)
    }

    @Test
    fun testGitHubTokenDetection() {
        val text = "token = ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect GitHub token")
        assertEquals(SecretType.GITHUB_TOKEN, findings.first().secretType)
    }

    @Test
    fun testPrivateKeyDetection() {
        val text = "-----BEGIN RSA PRIVATE KEY-----"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect private key header")
        assertEquals(SecretType.PRIVATE_KEY, findings.first().secretType)
    }

    @Test
    fun testEcPrivateKeyDetection() {
        val text = "-----BEGIN EC PRIVATE KEY-----"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect EC private key header")
        assertEquals(SecretType.PRIVATE_KEY, findings.first().secretType)
    }

    @Test
    fun testGenericApiKeyDetection() {
        val text = """api_key = "ABCDEFGHIJKLMNOPQRSTuvwxyz123456" """
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect generic API key")
        assertEquals(SecretType.API_KEY, findings.first().secretType)
    }

    @Test
    fun testJwtTokenDetection() {
        val text = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect JWT token")
        assertEquals(SecretType.JWT, findings.first().secretType)
    }

    @Test
    fun testSlackBotTokenDetection() {
        val text = "SLACK_TOKEN=xoxb-1234567890123-ABCDEFGHIJKLMNopqrst"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect Slack bot token")
        assertEquals(SecretType.SLACK_TOKEN, findings.first().secretType)
    }

    @Test
    fun testStripeKeyDetection() {
        val text = """stripe_key = "sk_live_ABCDEFGHIJKLMNOPQRSTUVWXyz" """
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect Stripe key")
        assertEquals(SecretType.STRIPE_KEY, findings.first().secretType)
    }

    @Test
    fun testGenericPasswordDetection() {
        val text = """password = "mySuperSecretPassword123" """
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect generic password")
        assertEquals(SecretType.PASSWORD, findings.first().secretType)
    }

    @Test
    fun testConnectionStringDetection() {
        val text = "jdbc:mysql://user:password@localhost:3306/mydb"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect connection string")
        assertEquals(SecretType.CONNECTION_STRING, findings.first().secretType)
    }

    @Test
    fun testNoFalsePositiveOnSafeText() {
        val text = "This is a regular comment with no secrets"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isEmpty(), "Should not produce false positives on regular text")
    }

    @Test
    fun testMultipleSecrets() {
        val text = """
            aws_key = AKIAIOSFODNN7EXAMPLE
            token = ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn
            -----BEGIN RSA PRIVATE KEY-----
        """.trimIndent()
        val findings = SecretScanner.scan(text)
        assertTrue(findings.size >= 3, "Should detect multiple secrets")
    }

    @Test
    fun testMaskedValue() {
        val text = "AKIAIOSFODNN7EXAMPLE"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty())
        val masked = findings.first().maskedValue
        assertTrue(masked.contains("*"), "Masked value should contain asterisks")
        assertTrue(masked.startsWith("AKIA"), "Masked value should preserve prefix")
    }

    @Test
    fun testDisabledPattern() {
        val disabledPattern = SecretScanner.BUILTIN_PATTERNS.first().copy(enabled = false)
        val text = "AKIAIOSFODNN7EXAMPLE"
        val findings = SecretScanner.scan(text, listOf(disabledPattern))
        assertTrue(findings.isEmpty(), "Disabled patterns should not produce findings")
    }

    @Test
    fun testLineNumberTracking() {
        val text = "line1\nline2\nAKIAIOSFODNN7EXAMPLE\nline4"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty())
        assertEquals(3, findings.first().lineNumber, "Should report correct line number")
    }

    @Test
    fun testGoogleApiKeyDetection() {
        val text = "google_key = AIzaSyDaGmWKa4JsXZ-HjGw7ISLn_3namBGewQe"
        val findings = SecretScanner.scan(text)
        assertTrue(findings.isNotEmpty(), "Should detect Google API key")
        assertEquals(SecretType.API_KEY, findings.first().secretType)
    }
}
