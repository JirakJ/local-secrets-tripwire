package com.jakubjirak.secretstripwire.model

data class SecretPattern(
    val id: String,
    val name: String,
    val regex: String,
    val secretType: SecretType,
    val severity: SeverityLevel = SeverityLevel.WARNING,
    val description: String = "",
    val enabled: Boolean = true,
) {
    val compiledRegex: Regex by lazy { Regex(regex) }
}
