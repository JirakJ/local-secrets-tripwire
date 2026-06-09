package com.jakubjirak.secretstripwire.scanner

import com.jakubjirak.secretstripwire.model.SecretFinding
import com.jakubjirak.secretstripwire.model.SecretPattern
import com.jakubjirak.secretstripwire.model.SecretType
import com.jakubjirak.secretstripwire.model.SeverityLevel
import com.intellij.openapi.diagnostic.Logger

/**
 * Main scanning engine that detects secrets in text content using
 * regex-based pattern matching and Shannon entropy analysis.
 */
object SecretScanner {

    private val LOG = Logger.getInstance(SecretScanner::class.java)
    private const val MAX_FILE_SIZE = 1_048_576 // 1 MB

    val BUILTIN_PATTERNS: List<SecretPattern> = listOf(
        SecretPattern(
            id = "aws_access_key",
            name = "AWS Access Key",
            regex = """AKIA[0-9A-Z]{16}""",
            secretType = SecretType.AWS_KEY,
            severity = SeverityLevel.ERROR,
            description = "AWS Access Key ID",
        ),
        SecretPattern(
            id = "aws_secret_key",
            name = "AWS Secret Key",
            regex = """(?i)aws_secret_access_key\s*[=:]\s*['"]?([A-Za-z0-9/+=]{40})['"]?""",
            secretType = SecretType.AWS_KEY,
            severity = SeverityLevel.ERROR,
            description = "AWS Secret Access Key",
        ),
        SecretPattern(
            id = "github_token",
            name = "GitHub Token",
            regex = """gh[ps]_[A-Za-z0-9_]{36,}""",
            secretType = SecretType.GITHUB_TOKEN,
            severity = SeverityLevel.ERROR,
            description = "GitHub Personal Access Token or Service Token",
        ),
        SecretPattern(
            id = "github_oauth",
            name = "GitHub OAuth Token",
            regex = """gho_[A-Za-z0-9_]{36,}""",
            secretType = SecretType.GITHUB_TOKEN,
            severity = SeverityLevel.ERROR,
            description = "GitHub OAuth Access Token",
        ),
        SecretPattern(
            id = "generic_api_key",
            name = "Generic API Key",
            regex = """(?i)[aA][pP][iI][_\-]?[kK][eE][yY]\s*[:=]\s*['"][A-Za-z0-9]{20,}['"]""",
            secretType = SecretType.API_KEY,
            severity = SeverityLevel.WARNING,
            description = "Generic API key assignment",
        ),
        SecretPattern(
            id = "private_key",
            name = "Private Key",
            regex = """-----BEGIN (RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----""",
            secretType = SecretType.PRIVATE_KEY,
            severity = SeverityLevel.ERROR,
            description = "Private key header",
        ),
        SecretPattern(
            id = "jwt_token",
            name = "JWT Token",
            regex = """eyJ[A-Za-z0-9_-]+\.eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+""",
            secretType = SecretType.JWT,
            severity = SeverityLevel.WARNING,
            description = "JSON Web Token",
        ),
        SecretPattern(
            id = "slack_bot_token",
            name = "Slack Bot Token",
            regex = """xoxb-[0-9]{10,13}-[A-Za-z0-9-]+""",
            secretType = SecretType.SLACK_TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Slack Bot Token",
        ),
        SecretPattern(
            id = "slack_user_token",
            name = "Slack User Token",
            regex = """xoxp-[0-9]{10,13}-[0-9]{10,13}-[A-Za-z0-9-]+""",
            secretType = SecretType.SLACK_TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Slack User Token",
        ),
        SecretPattern(
            id = "stripe_key",
            name = "Stripe Secret Key",
            regex = """sk_(live|test)_[A-Za-z0-9]{24,}""",
            secretType = SecretType.STRIPE_KEY,
            severity = SeverityLevel.ERROR,
            description = "Stripe API Secret Key",
        ),
        SecretPattern(
            id = "generic_password",
            name = "Generic Password",
            regex = """(?i)(password|passwd|pwd)\s*[=:]\s*['"][^'"]{8,}['"]""",
            secretType = SecretType.PASSWORD,
            severity = SeverityLevel.WARNING,
            description = "Hardcoded password assignment",
        ),
        SecretPattern(
            id = "generic_secret",
            name = "Generic Secret",
            regex = """(?i)(secret|token)\s*[=:]\s*['"][^'"]{16,}['"]""",
            secretType = SecretType.GENERIC_SECRET,
            severity = SeverityLevel.WARNING,
            description = "Hardcoded secret or token assignment",
        ),
        SecretPattern(
            id = "connection_string",
            name = "Connection String",
            regex = """(?i)(?:jdbc:[a-z]+|mongodb(?:\+srv)?|redis)://[^:]+:[^@]+@[^\s'"]+""",
            secretType = SecretType.CONNECTION_STRING,
            severity = SeverityLevel.ERROR,
            description = "Connection string with embedded credentials",
        ),
        SecretPattern(
            id = "google_api_key",
            name = "Google API Key",
            regex = """AIza[0-9A-Za-z\-_]{35}""",
            secretType = SecretType.API_KEY,
            severity = SeverityLevel.ERROR,
            description = "Google API Key",
        ),
        SecretPattern(
            id = "sendgrid_key",
            name = "SendGrid API Key",
            regex = """SG\.[A-Za-z0-9_-]{22}\.[A-Za-z0-9_-]{43}""",
            secretType = SecretType.API_KEY,
            severity = SeverityLevel.ERROR,
            description = "SendGrid API Key",
        ),
        SecretPattern(
            id = "aws_session_key",
            name = "AWS Temporary Access Key",
            regex = """ASIA[0-9A-Z]{16}""",
            secretType = SecretType.AWS_KEY,
            severity = SeverityLevel.ERROR,
            description = "AWS temporary (STS) Access Key ID",
        ),
        SecretPattern(
            id = "github_fine_grained",
            name = "GitHub Fine-Grained Token",
            regex = """github_pat_[A-Za-z0-9_]{36,}""",
            secretType = SecretType.GITHUB_TOKEN,
            severity = SeverityLevel.ERROR,
            description = "GitHub fine-grained Personal Access Token",
        ),
        SecretPattern(
            id = "github_refresh_token",
            name = "GitHub Refresh Token",
            regex = """ghr_[A-Za-z0-9_]{36,}""",
            secretType = SecretType.GITHUB_TOKEN,
            severity = SeverityLevel.ERROR,
            description = "GitHub OAuth refresh token",
        ),
        SecretPattern(
            id = "openai_key",
            name = "OpenAI API Key",
            regex = """sk-(?:proj-|svcacct-|admin-)?[A-Za-z0-9_-]{20,}T3BlbkFJ[A-Za-z0-9_-]{20,}|sk-[A-Za-z0-9]{48}\b""",
            secretType = SecretType.API_KEY,
            severity = SeverityLevel.ERROR,
            description = "OpenAI API key (project, service-account, or legacy format)",
        ),
        SecretPattern(
            id = "anthropic_key",
            name = "Anthropic API Key",
            regex = """sk-ant-[a-z0-9]{2,8}-[A-Za-z0-9_-]{80,}""",
            secretType = SecretType.API_KEY,
            severity = SeverityLevel.ERROR,
            description = "Anthropic Claude API key",
        ),
        SecretPattern(
            id = "gitlab_pat",
            name = "GitLab Personal Access Token",
            regex = """glpat-[A-Za-z0-9_-]{20,}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "GitLab Personal Access Token",
        ),
        SecretPattern(
            id = "npm_token",
            name = "npm Access Token",
            regex = """npm_[A-Za-z0-9]{36}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "npm registry access token",
        ),
        SecretPattern(
            id = "pypi_token",
            name = "PyPI API Token",
            regex = """pypi-AgEIcHlwaS5vcmc[A-Za-z0-9_-]{50,}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "PyPI upload API token",
        ),
        SecretPattern(
            id = "huggingface_token",
            name = "Hugging Face Token",
            regex = """hf_[A-Za-z]{34,}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Hugging Face user access token",
        ),
        SecretPattern(
            id = "slack_app_token",
            name = "Slack App Token",
            regex = """xapp-\d-[A-Z0-9]+-\d+-[a-f0-9]{32,}""",
            secretType = SecretType.SLACK_TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Slack app-level token",
        ),
        SecretPattern(
            id = "slack_webhook",
            name = "Slack Webhook URL",
            regex = """https://hooks\.slack\.com/services/T[A-Za-z0-9]+/B[A-Za-z0-9]+/[A-Za-z0-9]{20,}""",
            secretType = SecretType.SLACK_TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Slack incoming webhook URL with embedded secret",
        ),
        SecretPattern(
            id = "stripe_webhook_secret",
            name = "Stripe Webhook Secret",
            regex = """whsec_[A-Za-z0-9]{32,}""",
            secretType = SecretType.STRIPE_KEY,
            severity = SeverityLevel.ERROR,
            description = "Stripe webhook signing secret",
        ),
        SecretPattern(
            id = "twilio_api_key",
            name = "Twilio API Key",
            regex = """\bSK[a-f0-9]{32}\b""",
            secretType = SecretType.API_KEY,
            severity = SeverityLevel.ERROR,
            description = "Twilio API key SID",
        ),
        SecretPattern(
            id = "telegram_bot_token",
            name = "Telegram Bot Token",
            regex = """\b\d{8,10}:AA[A-Za-z0-9_-]{33}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Telegram bot API token",
        ),
        SecretPattern(
            id = "digitalocean_token",
            name = "DigitalOcean Token",
            regex = """do[pors]_v1_[a-f0-9]{64}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "DigitalOcean personal access / OAuth token",
        ),
        SecretPattern(
            id = "shopify_token",
            name = "Shopify Access Token",
            regex = """shp(?:at|ca|pa|ss)_[a-fA-F0-9]{32}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Shopify private app / access token",
        ),
        SecretPattern(
            id = "databricks_token",
            name = "Databricks Token",
            regex = """dapi[a-f0-9]{32}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Databricks personal access token",
        ),
        SecretPattern(
            id = "atlassian_token",
            name = "Atlassian API Token",
            regex = """ATATT3[A-Za-z0-9_=-]{20,}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Atlassian (Jira/Confluence) API token",
        ),
        SecretPattern(
            id = "doppler_token",
            name = "Doppler Token",
            regex = """dp\.(?:pt|st|ct|sa)\.[A-Za-z0-9]{40,}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "Doppler service/personal token",
        ),
        SecretPattern(
            id = "vault_token",
            name = "HashiCorp Vault Token",
            regex = """hv[sb]\.[A-Za-z0-9_-]{24,}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.ERROR,
            description = "HashiCorp Vault service/batch token",
        ),
        SecretPattern(
            id = "azure_storage_key",
            name = "Azure Storage Account Key",
            regex = """AccountKey=[A-Za-z0-9+/]{86}==""",
            secretType = SecretType.CONNECTION_STRING,
            severity = SeverityLevel.ERROR,
            description = "Azure Storage account key in a connection string",
        ),
        SecretPattern(
            id = "mailchimp_key",
            name = "Mailchimp API Key",
            regex = """\b[a-f0-9]{32}-us\d{1,2}\b""",
            secretType = SecretType.API_KEY,
            severity = SeverityLevel.ERROR,
            description = "Mailchimp API key",
        ),
        SecretPattern(
            id = "fcm_server_key",
            name = "Firebase Cloud Messaging Key",
            regex = """APA91b[A-Za-z0-9_-]{100,}""",
            secretType = SecretType.API_KEY,
            severity = SeverityLevel.ERROR,
            description = "Firebase Cloud Messaging server key / registration token",
        ),
        SecretPattern(
            id = "basic_auth_url",
            name = "Credentials in URL",
            regex = """https?://[^:@/\s'"]+:[^:@/\s'"]{6,}@[^\s'"]+""",
            secretType = SecretType.CONNECTION_STRING,
            severity = SeverityLevel.WARNING,
            description = "URL with embedded basic-auth credentials",
        ),
        SecretPattern(
            id = "bearer_token",
            name = "Bearer Token",
            regex = """(?i)authorization['"]?\s*[:=]\s*['"]?Bearer\s+[A-Za-z0-9._+/=-]{20,}""",
            secretType = SecretType.TOKEN,
            severity = SeverityLevel.WARNING,
            description = "Hardcoded Authorization: Bearer header",
        ),
        SecretPattern(
            id = "pgp_private_key",
            name = "PGP Private Key",
            regex = """-----BEGIN PGP PRIVATE KEY BLOCK-----""",
            secretType = SecretType.PRIVATE_KEY,
            severity = SeverityLevel.ERROR,
            description = "PGP private key block header",
        ),
    )

    /** Inline suppression marker: any finding on a line containing it is ignored. */
    const val SUPPRESS_MARKER = "tripwire:ignore"

    fun scan(text: String, patterns: List<SecretPattern> = BUILTIN_PATTERNS): List<SecretFinding> {
        if (text.length > MAX_FILE_SIZE) {
            LOG.info("Skipping file: exceeds maximum size of $MAX_FILE_SIZE bytes")
            return emptyList()
        }

        val findings = mutableListOf<SecretFinding>()
        val activePatterns = patterns.filter { it.enabled }

        for (pattern in activePatterns) {
            try {
                scanWithPattern(text, pattern, findings)
            } catch (e: Exception) {
                LOG.warn("Error scanning with pattern '${pattern.name}': ${e.message}")
            }
        }

        return deduplicate(filterSuppressed(text, findings))
    }

    /**
     * Drops findings on lines carrying the [SUPPRESS_MARKER] comment
     * (e.g. `// tripwire:ignore` or `# tripwire:ignore`).
     */
    fun filterSuppressed(text: String, findings: List<SecretFinding>): List<SecretFinding> {
        if (findings.isEmpty() || !text.contains(SUPPRESS_MARKER)) return findings
        val suppressedLines = buildSet {
            text.lines().forEachIndexed { index, line ->
                if (line.contains(SUPPRESS_MARKER)) add(index + 1)
            }
        }
        return findings.filter { it.lineNumber !in suppressedLines }
    }

    /**
     * When multiple patterns match at the same offset, keeps the most severe
     * (and most specific) finding instead of reporting duplicates.
     */
    private fun deduplicate(findings: List<SecretFinding>): List<SecretFinding> =
        findings
            .sortedBy { it.severity.ordinal } // ERROR first
            .distinctBy { it.startOffset }
            .sortedBy { it.startOffset }

    private fun scanWithPattern(text: String, pattern: SecretPattern, findings: MutableList<SecretFinding>) {
        val matches = pattern.compiledRegex.findAll(text)
        for (match in matches) {
            val matchedText = match.value
            val entropy = EntropyAnalyzer.shannonEntropy(matchedText)
            val lineNumber = text.substring(0, match.range.first).count { it == '\n' } + 1

            findings.add(
                SecretFinding(
                    pattern = pattern,
                    matchedText = matchedText,
                    maskedValue = SecretFinding.maskSecret(matchedText),
                    startOffset = match.range.first,
                    endOffset = match.range.last + 1,
                    lineNumber = lineNumber,
                    entropy = entropy,
                ),
            )
        }
    }

    fun scanWithEntropy(
        text: String,
        threshold: Double = EntropyAnalyzer.DEFAULT_THRESHOLD,
        patterns: List<SecretPattern> = BUILTIN_PATTERNS,
    ): List<SecretFinding> {
        val patternFindings = scan(text, patterns)
        val entropyFindings = findHighEntropyStrings(text, threshold)
        return (patternFindings + entropyFindings).distinctBy { it.startOffset }
    }

    private fun findHighEntropyStrings(text: String, threshold: Double): List<SecretFinding> {
        val findings = mutableListOf<SecretFinding>()
        val highEntropyPattern = SecretPattern(
            id = "high_entropy",
            name = "High-Entropy String",
            regex = "",
            secretType = SecretType.HIGH_ENTROPY,
            severity = SeverityLevel.INFO,
            description = "String with unusually high entropy",
        )
        val wordRegex = Regex("""['"][A-Za-z0-9+/=_\-]{16,}['"]""")
        val matches = wordRegex.findAll(text)

        for (match in matches) {
            val content = match.value.removeSurrounding("\"").removeSurrounding("'")
            val entropy = EntropyAnalyzer.shannonEntropy(content)
            if (entropy > threshold) {
                val lineNumber = text.substring(0, match.range.first).count { it == '\n' } + 1
                findings.add(
                    SecretFinding(
                        pattern = highEntropyPattern,
                        matchedText = match.value,
                        maskedValue = SecretFinding.maskSecret(content),
                        startOffset = match.range.first,
                        endOffset = match.range.last + 1,
                        lineNumber = lineNumber,
                        entropy = entropy,
                    ),
                )
            }
        }

        return findings
    }
}
