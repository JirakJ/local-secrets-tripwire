package com.jakubjirak.secretstripwire.git

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.jakubjirak.secretstripwire.allowlist.AllowlistLoader
import com.jakubjirak.secretstripwire.model.SecretFinding
import com.jakubjirak.secretstripwire.model.SecretPattern
import com.jakubjirak.secretstripwire.scanner.SecretScanner
import com.jakubjirak.secretstripwire.settings.SecretsTripwireSettings

/**
 * Represents the result of scanning staged files for secrets before commit.
 */
data class StagedSecretScanResult(
    val findings: List<StagedSecretFinding>,
    val filesScanned: Int,
    val blocked: Boolean,
)

/**
 * A secret finding associated with a specific staged file.
 */
data class StagedSecretFinding(
    val filePath: String,
    val finding: SecretFinding,
)

/**
 * Factory for creating [SecretCheckinHandler] instances that scan staged files
 * for secrets before allowing a commit to proceed.
 */
class SecretCheckinHandlerFactory : CheckinHandlerFactory() {

    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return SecretCheckinHandler(panel)
    }
}

/**
 * Scans staged files for secrets before commit. When secrets are found, shows
 * a dialog listing the findings (masked) and lets the user cancel or commit anyway.
 */
class SecretCheckinHandler(
    private val panel: CheckinProjectPanel,
) : CheckinHandler() {

    override fun beforeCheckin(): ReturnResult {
        val settings = SecretsTripwireSettings.getInstance()
        if (!settings.enabled || !settings.preCommitGuardEnabled) return ReturnResult.COMMIT

        val changes = panel.selectedChanges
        if (changes.isEmpty()) return ReturnResult.COMMIT

        val allowlist = AllowlistLoader.loadAllowlist(panel.project)
        val patterns = SecretScanner.BUILTIN_PATTERNS + settings.resolveCustomPatterns()

        val files = mutableMapOf<String, String>()
        for (change in changes) {
            val virtualFile = change.virtualFile ?: continue
            try {
                files[virtualFile.path] = String(virtualFile.contentsToByteArray())
            } catch (e: Exception) {
                LOG.warn("Error reading file: ${virtualFile.path}", e)
            }
        }

        val result = scanFiles(files, patterns, allowlist)
        if (!result.blocked) return ReturnResult.COMMIT

        LOG.warn("Found ${result.findings.size} potential secrets in staged files")
        return if (confirmCommitWithSecrets(result)) ReturnResult.COMMIT else ReturnResult.CANCEL
    }

    private fun confirmCommitWithSecrets(result: StagedSecretScanResult): Boolean {
        // Headless safety: never silently allow in non-interactive environments
        if (ApplicationManager.getApplication().isHeadlessEnvironment) return false

        val answer = Messages.showYesNoDialog(
            panel.project,
            buildDialogMessage(result),
            "Potential Secrets Detected",
            "Commit Anyway",
            "Cancel Commit",
            Messages.getWarningIcon(),
        )
        return answer == Messages.YES
    }

    companion object {
        private val LOG = Logger.getInstance(SecretCheckinHandler::class.java)
        private const val MAX_FINDINGS_IN_DIALOG = 10

        /** Builds the warning dialog text; pure for unit testing. */
        fun buildDialogMessage(result: StagedSecretScanResult): String {
            val details = result.findings
                .take(MAX_FINDINGS_IN_DIALOG)
                .joinToString("\n") { staged ->
                    val name = staged.filePath.substringAfterLast('/')
                    "• $name:${staged.finding.lineNumber} — ${staged.finding.pattern.name} (${staged.finding.maskedValue})"
                }
            val more = (result.findings.size - MAX_FINDINGS_IN_DIALOG).coerceAtLeast(0)
            val suffix = if (more > 0) "\n…and $more more" else ""

            return "Secrets Tripwire found ${result.findings.size} potential secret(s) " +
                "in the files you are committing:\n\n$details$suffix\n\n" +
                "Committing secrets to version control is a common cause of security breaches. " +
                "Commit anyway?"
        }

        /**
         * Scans text content for secrets - extracted for unit testing without VCS dependencies.
         */
        fun scanContent(
            content: String,
            filePath: String,
            patterns: List<SecretPattern> = SecretScanner.BUILTIN_PATTERNS,
            allowlist: List<String> = emptyList(),
        ): List<StagedSecretFinding> {
            return SecretScanner.scan(content, patterns)
                .filter { finding -> allowlist.none { finding.matchedText.contains(it) } }
                .map { finding -> StagedSecretFinding(filePath = filePath, finding = finding) }
        }

        /**
         * Scans multiple file contents and returns aggregated results.
         */
        fun scanFiles(
            files: Map<String, String>,
            patterns: List<SecretPattern> = SecretScanner.BUILTIN_PATTERNS,
            allowlist: List<String> = emptyList(),
        ): StagedSecretScanResult {
            val allFindings = mutableListOf<StagedSecretFinding>()
            for ((path, content) in files) {
                allFindings.addAll(scanContent(content, path, patterns, allowlist))
            }
            return StagedSecretScanResult(
                findings = allFindings,
                filesScanned = files.size,
                blocked = allFindings.isNotEmpty(),
            )
        }
    }
}
