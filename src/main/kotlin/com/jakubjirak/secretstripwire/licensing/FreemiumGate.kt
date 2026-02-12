package com.jakubjirak.secretstripwire.licensing

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

/**
 * Central gate for freemium feature access.
 * Free tier: 10 built-in patterns, basic scanning.
 * Paid tier: custom patterns, pre-commit hook, team allowlist.
 */
@Service(Service.Level.APP)
class FreemiumGate {

    enum class LicenseStatus {
        FREE, TRIAL, PAID, EXPIRED
    }

    @Suppress("UNUSED_PARAMETER")
    fun isPaidFeatureAvailable(featureId: String): Boolean {
        val status = getLicenseStatus()
        return status == LicenseStatus.PAID || status == LicenseStatus.TRIAL
    }

    fun getLicenseStatus(): LicenseStatus {
        // TODO: Integrate with JetBrains Marketplace Licensing API
        return LicenseStatus.FREE
    }

    fun getMaxCustomPatterns(): Int {
        return when (getLicenseStatus()) {
            LicenseStatus.FREE -> MAX_FREE_CUSTOM_PATTERNS
            LicenseStatus.EXPIRED -> MAX_FREE_CUSTOM_PATTERNS
            LicenseStatus.TRIAL, LicenseStatus.PAID -> Int.MAX_VALUE
        }
    }

    companion object {
        const val MAX_FREE_CUSTOM_PATTERNS = 10

        const val FEATURE_CUSTOM_PATTERNS = "custom_patterns"
        const val FEATURE_PRE_COMMIT = "pre_commit"
        const val FEATURE_TEAM_ALLOWLIST = "team_allowlist"
        const val FEATURE_GIT_HISTORY_SCAN = "git_history_scan"
        const val FEATURE_COMMIT_BLOCKING = "commit_blocking"
        const val FEATURE_SARIF_EXPORT = "sarif_export"

        fun getInstance(): FreemiumGate =
            ApplicationManager.getApplication().getService(FreemiumGate::class.java)
    }
}
