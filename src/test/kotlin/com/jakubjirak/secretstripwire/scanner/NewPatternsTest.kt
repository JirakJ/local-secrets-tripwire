package com.jakubjirak.secretstripwire.scanner

import com.jakubjirak.secretstripwire.model.SecretType
import com.jakubjirak.secretstripwire.model.SeverityLevel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * One positive detection test per pattern added in 2026.4.0,
 * plus negative tests guarding against false positives.
 * All sample tokens are synthetic.
 */
class NewPatternsTest {

    private fun detectedIds(text: String): Set<String> =
        SecretScanner.scan(text).map { it.pattern.id }.toSet()

    @Test
    fun `detects AWS temporary access key`() {
        assertTrue("aws_session = ASIAIOSFODNN7EXAMPLE".let { detectedIds(it) }.contains("aws_session_key"))
    }

    @Test
    fun `detects GitHub fine-grained token`() {
        val token = "github_pat_11ABCDEFG0" + "x".repeat(60)
        assertTrue(detectedIds("token = $token").contains("github_fine_grained"))
    }

    @Test
    fun `detects GitHub refresh token`() {
        val token = "ghr_" + "A".repeat(40)
        assertTrue(detectedIds("refresh = $token").contains("github_refresh_token"))
    }

    @Test
    fun `detects OpenAI project key`() {
        val token = "sk-proj-" + "a".repeat(24) + "T3BlbkFJ" + "b".repeat(24)
        assertTrue(detectedIds("OPENAI_API_KEY=$token").contains("openai_key"))
    }

    @Test
    fun `detects OpenAI legacy key`() {
        val token = "sk-" + "Ab1".repeat(16) // 48 alphanumeric chars
        assertTrue(detectedIds("key: $token").contains("openai_key"))
    }

    @Test
    fun `detects Anthropic key`() {
        val token = "sk-ant-api03-" + "A".repeat(85)
        assertTrue(detectedIds("ANTHROPIC_API_KEY=$token").contains("anthropic_key"))
    }

    @Test
    fun `detects GitLab PAT`() {
        assertTrue(detectedIds("gitlab: glpat-" + "x1".repeat(13)).contains("gitlab_pat"))
    }

    @Test
    fun `detects npm token`() {
        val token = "npm_" + "a1B2".repeat(9) // 36 chars
        assertTrue(detectedIds("//registry.npmjs.org/:_authToken=$token").contains("npm_token"))
    }

    @Test
    fun `detects PyPI token`() {
        val token = "pypi-AgEIcHlwaS5vcmc" + "C".repeat(60)
        assertTrue(detectedIds("password = $token").contains("pypi_token"))
    }

    @Test
    fun `detects Hugging Face token`() {
        val token = "hf_" + "abcdefghijklmnopqrstuvwxyzABCDEFGH" // 34 letters
        assertTrue(detectedIds("HF_TOKEN=$token").contains("huggingface_token"))
    }

    @Test
    fun `detects Slack app token`() {
        val token = "xapp-1-A12345678-1234567890-" + "f".repeat(40)
        assertTrue(detectedIds(token).contains("slack_app_token"))
    }

    @Test
    fun `detects Slack webhook URL`() {
        val url = "https://hooks.slack.com/services/T00000001/B00000001/XXXXXXXXXXXXXXXXXXXXXXXX"
        assertTrue(detectedIds("url = $url").contains("slack_webhook"))
    }

    @Test
    fun `detects Stripe webhook secret`() {
        assertTrue(detectedIds("whsec_" + "a1".repeat(20)).contains("stripe_webhook_secret"))
    }

    @Test
    fun `detects Twilio API key`() {
        val token = "SK" + "0123456789abcdef".repeat(2)
        assertTrue(detectedIds("twilio_key = $token").contains("twilio_api_key"))
    }

    @Test
    fun `detects Telegram bot token`() {
        val token = "123456789:AA" + "Ab-_1".repeat(6) + "xyz" // 33 chars after AA
        assertTrue(detectedIds("bot = $token").contains("telegram_bot_token"))
    }

    @Test
    fun `detects DigitalOcean token`() {
        val token = "dop_v1_" + "0a".repeat(32)
        assertTrue(detectedIds(token).contains("digitalocean_token"))
    }

    @Test
    fun `detects Shopify token`() {
        val token = "shpat_" + "0f".repeat(16)
        assertTrue(detectedIds(token).contains("shopify_token"))
    }

    @Test
    fun `detects Databricks token`() {
        val token = "dapi" + "0e".repeat(16)
        assertTrue(detectedIds(token).contains("databricks_token"))
    }

    @Test
    fun `detects Atlassian token`() {
        val token = "ATATT3xFfGF0" + "T".repeat(20)
        assertTrue(detectedIds("jira = $token").contains("atlassian_token"))
    }

    @Test
    fun `detects Doppler token`() {
        val token = "dp.pt." + "a1".repeat(21)
        assertTrue(detectedIds(token).contains("doppler_token"))
    }

    @Test
    fun `detects Vault token`() {
        val token = "hvs." + "Aa1".repeat(10)
        assertTrue(detectedIds("VAULT_TOKEN=$token").contains("vault_token"))
    }

    @Test
    fun `detects Azure storage key`() {
        val key = "AccountKey=" + "A".repeat(86) + "=="
        assertTrue(detectedIds("DefaultEndpointsProtocol=https;$key").contains("azure_storage_key"))
    }

    @Test
    fun `detects Mailchimp key`() {
        val key = "0123456789abcdef0123456789abcdef-us12"
        assertTrue(detectedIds("mailchimp = $key").contains("mailchimp_key"))
    }

    @Test
    fun `detects FCM server key`() {
        val key = "APA91b" + "C".repeat(110)
        assertTrue(detectedIds(key).contains("fcm_server_key"))
    }

    @Test
    fun `detects credentials in URL`() {
        assertTrue(detectedIds("https://admin:supersecret@internal.example.com/api").contains("basic_auth_url"))
    }

    @Test
    fun `detects Bearer token`() {
        assertTrue(
            detectedIds(""""Authorization": "Bearer abc123def456ghi789jkl012mno345"""")
                .contains("bearer_token")
        )
    }

    @Test
    fun `detects PGP private key`() {
        assertTrue(detectedIds("-----BEGIN PGP PRIVATE KEY BLOCK-----").contains("pgp_private_key"))
    }

    // --- negative tests ---

    @Test
    fun `does not flag ordinary code and prose`() {
        val text = """
            // Configure the SDK client with your credentials from the environment
            val client = HttpClient(baseUrl = "https://api.example.com/v2")
            val skipped = listOf("SKIPPED", "OK")
            git commit 0123456789abcdef0123456789abcdef01234567
            val versionHash = "0123456789abcdef0123456789abcdef"
        """.trimIndent()
        assertEquals(emptySet<String>(), detectedIds(text))
    }

    @Test
    fun `does not flag URL without password`() {
        assertTrue(detectedIds("https://user@example.com/path").isEmpty())
        assertTrue(detectedIds("https://example.com:8080/path").isEmpty())
    }

    @Test
    fun `does not flag stripe-like identifiers as twilio`() {
        // Stripe keys use sk_ prefix and mixed case — must not hit the Twilio pattern
        val findings = SecretScanner.scan("sk_live_" + "Ab1".repeat(9))
        assertTrue(findings.none { it.pattern.id == "twilio_api_key" })
    }

    @Test
    fun `builtin catalog has 40 plus unique enabled patterns`() {
        val ids = SecretScanner.BUILTIN_PATTERNS.map { it.id }
        assertTrue(ids.size >= 40, "Catalog has only ${ids.size} patterns")
        assertEquals(ids.size, ids.distinct().size, "Pattern ids must be unique")
        assertTrue(SecretScanner.BUILTIN_PATTERNS.all { it.enabled })
        // Every regex must compile
        SecretScanner.BUILTIN_PATTERNS.forEach { it.compiledRegex }
    }

    @Test
    fun `all error patterns stay errors`() {
        val critical = listOf("aws_access_key", "private_key", "openai_key", "anthropic_key", "github_token")
        for (id in critical) {
            val pattern = SecretScanner.BUILTIN_PATTERNS.first { it.id == id }
            assertEquals(SeverityLevel.ERROR, pattern.severity, "$id should be ERROR")
        }
    }

    @Test
    fun `connection string type assigned to azure storage key`() {
        val pattern = SecretScanner.BUILTIN_PATTERNS.first { it.id == "azure_storage_key" }
        assertEquals(SecretType.CONNECTION_STRING, pattern.secretType)
    }
}
