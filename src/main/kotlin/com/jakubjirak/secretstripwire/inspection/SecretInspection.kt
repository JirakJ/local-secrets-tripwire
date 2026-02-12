package com.jakubjirak.secretstripwire.inspection

import com.jakubjirak.secretstripwire.allowlist.AllowlistLoader
import com.jakubjirak.secretstripwire.scanner.SecretScanner
import com.jakubjirak.secretstripwire.settings.SecretsTripwireSettings
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile

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
        val findings = SecretScanner.scan(text, patterns)

        val filteredFindings = findings
            .filter { finding -> allowlist.none { finding.matchedText.contains(it) } }
            .filter { it.startOffset < text.length && it.endOffset <= text.length }

        for (finding in filteredFindings) {
            val element = file.findElementAt(finding.startOffset) ?: continue
            val message = "${finding.pattern.name}: ${finding.maskedValue}"
            holder.registerProblem(element, message)
        }
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
