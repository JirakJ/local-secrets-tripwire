package com.jakubjirak.secretstripwire.model

data class SecretFinding(
    val pattern: SecretPattern,
    val matchedText: String,
    val maskedValue: String,
    val startOffset: Int,
    val endOffset: Int,
    val lineNumber: Int,
    val entropy: Double = 0.0,
) {
    val secretType: SecretType get() = pattern.secretType
    val severity: SeverityLevel get() = pattern.severity

    companion object {
        private const val MASK_VISIBLE_CHARS = 4

        fun maskSecret(value: String): String {
            if (value.length <= MASK_VISIBLE_CHARS * 2) {
                return "*".repeat(value.length)
            }
            val prefix = value.substring(0, MASK_VISIBLE_CHARS)
            val suffix = value.substring(value.length - MASK_VISIBLE_CHARS)
            val masked = "*".repeat(value.length - MASK_VISIBLE_CHARS * 2)
            return "$prefix$masked$suffix"
        }
    }
}
