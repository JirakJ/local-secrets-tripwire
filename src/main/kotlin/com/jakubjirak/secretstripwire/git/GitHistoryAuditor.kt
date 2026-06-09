package com.jakubjirak.secretstripwire.git

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jakubjirak.secretstripwire.scanner.SecretScanner
import java.time.Instant

/**
 * Represents a secret leak found in Git history.
 */
data class HistoricalLeak(
    val commitHash: String,
    val date: Instant,
    val author: String,
    val filePath: String,
    val secretType: String,
    val lineContent: String,
)

/**
 * Result of a Git history audit scan.
 */
data class HistoryAuditResult(
    val leaks: List<HistoricalLeak>,
    val commitsScanned: Int,
    val scanDuration: Long,
)

/**
 * Scans Git history for leaked secrets by iterating through commits
 * and checking diffs for secret patterns. Runs as a background task
 * with progress indication.
 */
@Service(Service.Level.PROJECT)
class GitHistoryAuditor(
    @Suppress("UNUSED_PARAMETER") private val project: Project,
) {

    /**
     * Scans a list of commit diffs for secrets.
     * Each entry in [commitDiffs] maps commit metadata to its diff content.
     */
    fun auditCommitDiffs(commitDiffs: List<CommitDiff>): HistoryAuditResult {
        val startTime = System.currentTimeMillis()
        val leaks = mutableListOf<HistoricalLeak>()

        for (commit in commitDiffs) {
            val findings = SecretScanner.scan(commit.diffContent)
            for (finding in findings) {
                leaks.add(
                    HistoricalLeak(
                        commitHash = commit.hash,
                        date = commit.date,
                        author = commit.author,
                        filePath = commit.filePath,
                        secretType = finding.pattern.name,
                        lineContent = finding.maskedValue,
                    ),
                )
            }
        }

        val duration = System.currentTimeMillis() - startTime
        LOG.info("History audit complete: ${leaks.size} leaks in ${commitDiffs.size} commits (${duration}ms)")

        return HistoryAuditResult(
            leaks = leaks,
            commitsScanned = commitDiffs.size,
            scanDuration = duration,
        )
    }

    /**
     * Parses raw `git log -p` output into structured commit diffs.
     */
    fun parseGitLogOutput(gitLogOutput: String): List<CommitDiff> {
        val commitBlocks = gitLogOutput.split(Regex("(?=^commit [a-f0-9]{40})", RegexOption.MULTILINE))
        return commitBlocks.filter { it.isNotBlank() }.mapNotNull { parseCommitBlock(it) }
    }

    private fun parseCommitBlock(block: String): CommitDiff? {
        val hashMatch = Regex("^commit ([a-f0-9]{40})", RegexOption.MULTILINE).find(block)
            ?: return null
        val authorMatch = Regex("^Author:\\s+(.+)", RegexOption.MULTILINE).find(block)
        val dateMatch = Regex("^Date:\\s+(.+)", RegexOption.MULTILINE).find(block)
        val diffContent = block.substringAfter("diff --git", "")

        return CommitDiff(
            hash = hashMatch.groupValues[1],
            author = authorMatch?.groupValues?.get(1)?.trim() ?: "unknown",
            date = parseGitDate(dateMatch?.groupValues?.get(1)?.trim()),
            filePath = extractFilePath(diffContent),
            diffContent = diffContent,
        )
    }

    private fun extractFilePath(diffContent: String): String {
        val match = Regex("a/(.+?)\\s").find(diffContent)
        return match?.groupValues?.get(1) ?: "unknown"
    }

    companion object {
        private val LOG = Logger.getInstance(GitHistoryAuditor::class.java)

        /** Parses git's default date format ("Mon Jun 8 12:34:56 2026 +0200"); falls back to now. */
        fun parseGitDate(raw: String?): Instant {
            if (raw == null) return Instant.now()
            return try {
                java.time.ZonedDateTime.parse(
                    raw,
                    java.time.format.DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z", java.util.Locale.ENGLISH),
                ).toInstant()
            } catch (_: Exception) {
                Instant.now()
            }
        }
    }
}

/**
 * Structured representation of a single commit diff.
 */
data class CommitDiff(
    val hash: String,
    val author: String,
    val date: Instant,
    val filePath: String,
    val diffContent: String,
)
