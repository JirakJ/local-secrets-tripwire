/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the JetBrains Marketplace EULA.
 */
package com.jakubjirak.secretstripwire.licensing

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.ui.LicensingFacade
import com.jakubjirak.secretstripwire.model.SecretPattern

@Service(Service.Level.APP)
class FreemiumGate {

    enum class LicenseStatus { FREE, TRIAL, PAID }

    fun getLicenseStatus(): LicenseStatus {
        val facade = LicensingFacade.getInstance() ?: return LicenseStatus.FREE
        val stamp = facade.getConfirmationStamp(PRODUCT_CODE) ?: return LicenseStatus.FREE
        return if (stamp.startsWith("eval:")) LicenseStatus.TRIAL else LicenseStatus.PAID
    }

    fun isPremiumUser(): Boolean = getLicenseStatus() != LicenseStatus.FREE

    companion object {
        const val PRODUCT_CODE = "PSECRETSTRIPWRE"
        const val FREE_CUSTOM_PATTERN_LIMIT = 10

        fun getInstance(): FreemiumGate =
            ApplicationManager.getApplication().getService(FreemiumGate::class.java)

        /**
         * Applies the free-tier limit on custom patterns. Pure function so the
         * gating logic is unit-testable without the IDE licensing facade.
         */
        fun effectiveCustomPatterns(patterns: List<SecretPattern>, isPremium: Boolean): List<SecretPattern> =
            if (isPremium) patterns else patterns.take(FREE_CUSTOM_PATTERN_LIMIT)
    }
}
