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
    )

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

        return findings
    }

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
