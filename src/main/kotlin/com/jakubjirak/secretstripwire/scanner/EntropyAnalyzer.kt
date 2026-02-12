package com.jakubjirak.secretstripwire.scanner

import kotlin.math.ln

/**
 * Calculates Shannon entropy for strings to detect high-entropy values
 * that may be secrets (API keys, tokens, etc.).
 */
object EntropyAnalyzer {

    const val DEFAULT_THRESHOLD = 4.5
    private const val LOG_BASE_2 = 0.6931471805599453 // ln(2)
    private const val MIN_LENGTH_FOR_ANALYSIS = 8

    fun shannonEntropy(input: String): Double {
        if (input.length < MIN_LENGTH_FOR_ANALYSIS) return 0.0

        val frequencyMap = mutableMapOf<Char, Int>()
        for (char in input) {
            frequencyMap[char] = (frequencyMap[char] ?: 0) + 1
        }

        val length = input.length.toDouble()
        var entropy = 0.0

        for (count in frequencyMap.values) {
            val probability = count / length
            if (probability > 0) {
                entropy -= probability * (ln(probability) / LOG_BASE_2)
            }
        }

        return entropy
    }

    fun isHighEntropy(input: String, threshold: Double = DEFAULT_THRESHOLD): Boolean {
        return shannonEntropy(input) > threshold
    }
}
