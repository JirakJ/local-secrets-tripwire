package com.jakubjirak.secretstripwire.licensing

import com.jakubjirak.secretstripwire.model.SecretPattern
import com.jakubjirak.secretstripwire.model.SecretType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FreemiumGateTest {

    private fun patterns(count: Int): List<SecretPattern> =
        (1..count).map {
            SecretPattern(id = "p$it", name = "Pattern $it", regex = "x$it", secretType = SecretType.GENERIC_SECRET)
        }

    @Test
    fun `free tier is limited to 10 custom patterns`() {
        val limited = FreemiumGate.effectiveCustomPatterns(patterns(15), isPremium = false)
        assertEquals(FreemiumGate.FREE_CUSTOM_PATTERN_LIMIT, limited.size)
        assertEquals("p1", limited.first().id)
        assertEquals("p10", limited.last().id)
    }

    @Test
    fun `free tier keeps all patterns when under the limit`() {
        assertEquals(3, FreemiumGate.effectiveCustomPatterns(patterns(3), isPremium = false).size)
    }

    @Test
    fun `premium tier has no limit`() {
        assertEquals(50, FreemiumGate.effectiveCustomPatterns(patterns(50), isPremium = true).size)
    }

    @Test
    fun `product code matches the marketplace descriptor`() {
        assertEquals("PSECRETSTRIPWRE", FreemiumGate.PRODUCT_CODE)
    }
}
