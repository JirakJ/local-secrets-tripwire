package com.jakubjirak.secretstripwire.settings

import com.jakubjirak.secretstripwire.model.SecretPattern
import com.jakubjirak.secretstripwire.model.SecretType
import com.jakubjirak.secretstripwire.model.SeverityLevel
import com.jakubjirak.secretstripwire.scanner.EntropyAnalyzer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecretsTripwireSettingsTest {

    @Test
    fun `defaults`() {
        val settings = SecretsTripwireSettings()
        assertTrue(settings.enabled)
        assertFalse(settings.entropyDetectionEnabled)
        assertTrue(settings.preCommitGuardEnabled)
        assertEquals(EntropyAnalyzer.DEFAULT_THRESHOLD, settings.entropyThreshold)
        assertTrue(settings.resolveCustomPatterns().isEmpty())
    }

    @Test
    fun `custom patterns round trip through the serializable form`() {
        val pattern = SecretPattern(
            id = "acme",
            name = "ACME token",
            regex = "acme_[a-z0-9]{32}",
            secretType = SecretType.entries.first(),
            severity = SeverityLevel.entries.first(),
            description = "internal",
            enabled = false,
        )
        val settings = SecretsTripwireSettings()
        settings.customPatterns.add(SerializablePattern.fromSecretPattern(pattern))
        assertEquals(listOf(pattern), settings.resolveCustomPatterns())
    }

    @Test
    fun `unknown enum names fall back to safe defaults`() {
        val resolved = SerializablePattern(id = "x", regex = "x", secretType = "NOPE", severity = "NOPE").toSecretPattern()
        assertEquals(SecretType.GENERIC_SECRET, resolved.secretType)
        assertEquals(SeverityLevel.WARNING, resolved.severity)
        assertTrue(resolved.enabled)
    }

    @Test
    fun `loadState copies all fields`() {
        val source = SecretsTripwireSettings().apply {
            enabled = false
            entropyThreshold = 3.2
            entropyDetectionEnabled = true
            preCommitGuardEnabled = false
            customPatterns.add(SerializablePattern(id = "a", regex = "a"))
        }
        val target = SecretsTripwireSettings()
        target.loadState(source)
        assertFalse(target.enabled)
        assertEquals(3.2, target.entropyThreshold)
        assertTrue(target.entropyDetectionEnabled)
        assertFalse(target.preCommitGuardEnabled)
        assertEquals(1, target.customPatterns.size)
    }
}
