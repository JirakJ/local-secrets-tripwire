package com.jakubjirak.secretstripwire.scanner

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EntropyAnalyzerTest {

    @Test
    fun testHighEntropyString() {
        val highEntropy = "aBcDeFgHiJkLmNoPqRsTuVwXyZ123456"
        val entropy = EntropyAnalyzer.shannonEntropy(highEntropy)
        assertTrue(entropy > 4.0, "High-entropy string should have entropy > 4.0, got $entropy")
    }

    @Test
    fun testLowEntropyString() {
        val lowEntropy = "aaaaaaaaaaaaaaaa"
        val entropy = EntropyAnalyzer.shannonEntropy(lowEntropy)
        assertTrue(entropy < 1.0, "Low-entropy string should have entropy < 1.0, got $entropy")
    }

    @Test
    fun testEmptyString() {
        val entropy = EntropyAnalyzer.shannonEntropy("")
        assertEquals(0.0, entropy, "Empty string should have zero entropy")
    }

    @Test
    fun testShortString() {
        val entropy = EntropyAnalyzer.shannonEntropy("abc")
        assertEquals(0.0, entropy, "Short strings below minimum length should return 0")
    }

    @Test
    fun testIsHighEntropyTrue() {
        val highEntropy = "K8sF3jR2mQ9xL5vN7pT1wBcGhYdAeUi"
        assertTrue(EntropyAnalyzer.isHighEntropy(highEntropy), "Should detect as high entropy")
    }

    @Test
    fun testIsHighEntropyFalse() {
        val lowEntropy = "password12345678"
        assertFalse(
            EntropyAnalyzer.isHighEntropy(lowEntropy, 4.5),
            "Simple password pattern should not be high entropy at 4.5 threshold",
        )
    }

    @Test
    fun testCustomThreshold() {
        val text = "abcdefghijklmnop"
        val entropy = EntropyAnalyzer.shannonEntropy(text)
        assertTrue(EntropyAnalyzer.isHighEntropy(text, 2.0), "Should be high entropy at low threshold")
    }

    @Test
    fun testKnownEntropyValue() {
        // "abcdefgh" has 8 unique chars, each appearing once → entropy = log2(8) = 3.0
        val text = "abcdefgh"
        val entropy = EntropyAnalyzer.shannonEntropy(text)
        assertEquals(3.0, entropy, 0.01, "8 unique chars should have entropy ≈ 3.0")
    }

    @Test
    fun testBinaryLikeString() {
        val binary = "01010101010101010101"
        val entropy = EntropyAnalyzer.shannonEntropy(binary)
        assertTrue(entropy <= 1.0, "Binary-like string should have low entropy, got $entropy")
    }
}
