package com.jakubjirak.secretstripwire.git

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.jakubjirak.secretstripwire.model.SecretFinding
import com.jakubjirak.secretstripwire.scanner.SecretScanner

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
 * Scans staged files for secrets before commit.
 * Blocks commit if secrets are found and shows a dialog with findings.
 */
class SecretCheckinHandler(
    private val panel: CheckinProjectPanel,
) : CheckinHandler() {

    override fun beforeCheckin(): ReturnResult {
        val changes = panel.selectedChanges
        if (changes.isEmpty()) return ReturnResult.COMMIT

        val allFindings = mutableListOf<StagedSecretFinding>()

        for (change in changes) {
            val virtualFile = change.virtualFile ?: continue
            try {
                val content = String(virtualFile.contentsToByteArray())
                val findings = SecretScanner.scan(content)
                for (finding in findings) {
                    allFindings.add(
                        StagedSecretFinding(
                            filePath = virtualFile.path,
                            finding = finding,
                        ),
                    )
                }
            } catch (e: Exception) {
                LOG.warn("Error scanning file: ${virtualFile.path}", e)
            }
        }

        if (allFindings.isEmpty()) return ReturnResult.COMMIT

        LOG.warn("Found ${allFindings.size} secrets in staged files, blocking commit")
        return ReturnResult.CANCEL
    }

    companion object {
        private val LOG = Logger.getInstance(SecretCheckinHandler::class.java)

        /**
         * Scans text content for secrets - extracted for unit testing without VCS dependencies.
         */
        fun scanContent(content: String, filePath: String): List<StagedSecretFinding> {
            val findings = SecretScanner.scan(content)
            return findings.map { finding ->
                StagedSecretFinding(
                    filePath = filePath,
                    finding = finding,
                )
            }
        }

        /**
         * Scans multiple file contents and returns aggregated results.
         */
        fun scanFiles(files: Map<String, String>): StagedSecretScanResult {
            val allFindings = mutableListOf<StagedSecretFinding>()
            for ((path, content) in files) {
                allFindings.addAll(scanContent(content, path))
            }
            return StagedSecretScanResult(
                findings = allFindings,
                filesScanned = files.size,
                blocked = allFindings.isNotEmpty(),
            )
        }
    }
}
