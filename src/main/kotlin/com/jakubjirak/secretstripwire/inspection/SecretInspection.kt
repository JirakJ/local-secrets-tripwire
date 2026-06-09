package com.jakubjirak.secretstripwire.inspection

import com.jakubjirak.secretstripwire.allowlist.AllowlistLoader
import com.jakubjirak.secretstripwire.model.SeverityLevel
import com.jakubjirak.secretstripwire.scanner.SecretScanner
import com.jakubjirak.secretstripwire.settings.SecretsTripwireSettings
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import java.io.File

class SecretInspection : LocalInspectionTool() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession,
    ): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                inspectFile(file, holder)
            }
        }
    }

    private fun inspectFile(file: PsiFile, holder: ProblemsHolder) {
        val settings = SecretsTripwireSettings.getInstance()
        if (!settings.enabled) return

        val text = file.text ?: return
        val project = file.project
        val allowlist = AllowlistLoader.loadAllowlist(project)
        val patterns = SecretScanner.BUILTIN_PATTERNS + settings.resolveCustomPatterns()

        val findings = if (settings.entropyDetectionEnabled) {
            SecretScanner.scanWithEntropy(text, settings.entropyThreshold, patterns)
        } else {
            SecretScanner.scan(text, patterns)
        }

        val filteredFindings = findings
            .filter { finding -> allowlist.none { finding.matchedText.contains(it) } }
            .filter { it.startOffset < text.length && it.endOffset <= text.length }

        for (finding in filteredFindings) {
            val message = buildString {
                append("Possible secret — ${finding.pattern.name}: ${finding.maskedValue}")
                if (finding.entropy > 0) {
                    append(" (entropy %.2f)".format(finding.entropy))
                }
            }
            holder.registerProblem(
                file,
                message,
                highlightTypeFor(finding.severity),
                TextRange(finding.startOffset, finding.endOffset),
                AddToAllowlistQuickFix(finding.matchedText),
                SuppressLineQuickFix(finding.lineNumber),
            )
        }
    }

    private fun highlightTypeFor(severity: SeverityLevel): ProblemHighlightType = when (severity) {
        SeverityLevel.ERROR -> ProblemHighlightType.GENERIC_ERROR
        SeverityLevel.WARNING -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        SeverityLevel.INFO -> ProblemHighlightType.WEAK_WARNING
    }

    override fun getGroupDisplayName(): String = "Security"

    override fun getDisplayName(): String = "Secret detection"

    override fun getShortName(): String = "SecretInspection"

    override fun isEnabledByDefault(): Boolean = true

    companion object {
        @Suppress("unused")
        private val LOG = Logger.getInstance(SecretInspection::class.java)
    }
}

/**
 * Appends the detected value to the project's .secrets-allowlist file so it
 * is treated as a known false positive from now on.
 */
class AddToAllowlistQuickFix(private val matchedText: String) : LocalQuickFix {

    override fun getName(): String = "Add to .secrets-allowlist (mark as false positive)"
    override fun getFamilyName(): String = "Secrets Tripwire"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val basePath = project.basePath ?: return
        val allowlistFile = File(basePath, AllowlistLoader.ALLOWLIST_FILENAME)
        val existing = if (allowlistFile.isFile) allowlistFile.readText() else ""
        if (existing.lines().any { it.trim() == matchedText }) return

        val separator = if (existing.isEmpty() || existing.endsWith("\n")) "" else "\n"
        allowlistFile.appendText("$separator$matchedText\n")
        LocalFileSystem.getInstance().refreshIoFiles(listOf(allowlistFile))
    }
}

/**
 * Appends an inline `tripwire:ignore` suppression comment to the offending line.
 */
class SuppressLineQuickFix(private val lineNumber: Int) : LocalQuickFix {

    override fun getName(): String = "Suppress with ${SecretScanner.SUPPRESS_MARKER} comment"
    override fun getFamilyName(): String = "Secrets Tripwire"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val file = descriptor.psiElement?.containingFile ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
        val lineIndex = lineNumber - 1
        if (lineIndex < 0 || lineIndex >= document.lineCount) return

        val comment = suppressComment(file.name)
        if (comment.isEmpty()) return
        document.insertString(document.getLineEndOffset(lineIndex), comment)
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }

    companion object {
        /** Comment syntax by file extension; pure for unit testing. */
        fun suppressComment(fileName: String): String {
            val ext = fileName.substringAfterLast(".", "").lowercase()
            val prefix = when (ext) {
                "yaml", "yml", "properties", "toml", "ini", "env", "sh", "conf", "txt", "" -> "#"
                "xml", "html" -> null // no safe end-of-line comment syntax
                else -> "//"
            }
            return if (prefix != null) " $prefix ${SecretScanner.SUPPRESS_MARKER}" else ""
        }
    }
}
