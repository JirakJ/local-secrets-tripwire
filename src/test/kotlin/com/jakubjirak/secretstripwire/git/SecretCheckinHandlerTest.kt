package com.jakubjirak.secretstripwire.git

import com.jakubjirak.secretstripwire.model.SecretType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecretCheckinHandlerTest {

    @Test
    fun `scan content with AWS key detects secret`() {
        val content = "aws_access_key_id = AKIAIOSFODNN7EXAMPLE"
        val findings = SecretCheckinHandler.scanContent(content, "config.properties")

        assertTrue(findings.isNotEmpty(), "Should detect AWS key in content")
        assertEquals("config.properties", findings.first().filePath)
        assertEquals(SecretType.AWS_KEY, findings.first().finding.secretType)
    }

    @Test
    fun `scan content with no secrets returns empty`() {
        val content = "server.port=8080\napp.name=MyApp"
        val findings = SecretCheckinHandler.scanContent(content, "application.properties")

        assertTrue(findings.isEmpty(), "Should not detect secrets in safe content")
    }

    @Test
    fun `scan content with GitHub token detects secret`() {
        val content = "token = ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn"
        val findings = SecretCheckinHandler.scanContent(content, "config.yml")

        assertTrue(findings.isNotEmpty(), "Should detect GitHub token")
        assertEquals(SecretType.GITHUB_TOKEN, findings.first().finding.secretType)
    }

    @Test
    fun `scan content with private key header detects secret`() {
        val content = "-----BEGIN RSA PRIVATE KEY-----\nMIIE..."
        val findings = SecretCheckinHandler.scanContent(content, "key.pem")

        assertTrue(findings.isNotEmpty(), "Should detect private key header")
        assertEquals(SecretType.PRIVATE_KEY, findings.first().finding.secretType)
    }

    @Test
    fun `scan multiple files aggregates results`() {
        val files = mapOf(
            "config.properties" to "aws_access_key_id = AKIAIOSFODNN7EXAMPLE",
            "app.yml" to "token = ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn",
            "readme.md" to "This is a safe file with no secrets",
        )

        val result = SecretCheckinHandler.scanFiles(files)

        assertEquals(3, result.filesScanned)
        assertTrue(result.findings.size >= 2, "Should detect secrets from multiple files")
        assertTrue(result.blocked, "Should block commit when secrets found")
    }

    @Test
    fun `scan empty files returns not blocked`() {
        val files = mapOf(
            "empty.txt" to "",
            "safe.txt" to "Hello, World!",
        )

        val result = SecretCheckinHandler.scanFiles(files)

        assertEquals(2, result.filesScanned)
        assertTrue(result.findings.isEmpty())
        assertFalse(result.blocked, "Should not block commit with no secrets")
    }

    @Test
    fun `scan content preserves file path in findings`() {
        val content = "password = \"mySuperSecretPassword123\""
        val findings = SecretCheckinHandler.scanContent(content, "src/main/resources/db.properties")

        assertTrue(findings.isNotEmpty())
        assertEquals("src/main/resources/db.properties", findings.first().filePath)
    }

    @Test
    fun `scan content with multiple secrets in same file`() {
        val content = """
            aws_key = AKIAIOSFODNN7EXAMPLE
            token = ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn
            -----BEGIN RSA PRIVATE KEY-----
        """.trimIndent()

        val findings = SecretCheckinHandler.scanContent(content, "secrets.txt")

        assertTrue(findings.size >= 3, "Should detect all secrets in single file")
    }

    @Test
    fun `allowlisted values do not block the commit`() {
        val files = mapOf("config.properties" to "aws_access_key_id = AKIAIOSFODNN7EXAMPLE")
        val result = SecretCheckinHandler.scanFiles(files, allowlist = listOf("AKIAIOSFODNN7EXAMPLE"))

        assertFalse(result.blocked, "Allowlisted secret must not block the commit")
        assertTrue(result.findings.isEmpty())
    }

    @Test
    fun `custom patterns participate in pre-commit scanning`() {
        val custom = com.jakubjirak.secretstripwire.model.SecretPattern(
            id = "internal_token",
            name = "Internal Token",
            regex = """INT-[0-9]{12}""",
            secretType = SecretType.GENERIC_SECRET,
        )
        val files = mapOf("notes.txt" to "deploy key INT-123456789012")
        val result = SecretCheckinHandler.scanFiles(
            files,
            patterns = com.jakubjirak.secretstripwire.scanner.SecretScanner.BUILTIN_PATTERNS + custom,
        )

        assertTrue(result.blocked)
        assertEquals("internal_token", result.findings.single().finding.pattern.id)
    }

    @Test
    fun `suppressed lines do not block the commit`() {
        val files = mapOf("demo.kt" to "val sample = \"AKIAIOSFODNN7EXAMPLE\" // tripwire:ignore")
        assertFalse(SecretCheckinHandler.scanFiles(files).blocked)
    }

    @Test
    fun `dialog message lists findings masked and capped`() {
        val files = mapOf("config.properties" to "aws_access_key_id = AKIAIOSFODNN7EXAMPLE")
        val result = SecretCheckinHandler.scanFiles(files)
        val message = SecretCheckinHandler.buildDialogMessage(result)

        assertTrue(message.contains("1 potential secret"))
        assertTrue(message.contains("config.properties:1"))
        assertTrue(message.contains("AWS Access Key"))
        assertTrue(message.contains("AKIA********PLE".take(4)), "Masked value expected")
        assertFalse(message.contains("AKIAIOSFODNN7EXAMPLE"), "Dialog must never show the raw secret")
    }

    @Test
    fun `dialog message truncates long finding lists`() {
        val files = (1..15).associate { "file$it.txt" to "key = AKIAIOSFODNN7EXAMPLE" }
        val result = SecretCheckinHandler.scanFiles(files)
        val message = SecretCheckinHandler.buildDialogMessage(result)

        assertEquals(15, result.findings.size)
        assertTrue(message.contains("…and 5 more"))
    }

    @Test
    fun `staged secret scan result model properties`() {
        val result = StagedSecretScanResult(
            findings = emptyList(),
            filesScanned = 5,
            blocked = false,
        )

        assertEquals(5, result.filesScanned)
        assertFalse(result.blocked)
        assertTrue(result.findings.isEmpty())
    }
}
