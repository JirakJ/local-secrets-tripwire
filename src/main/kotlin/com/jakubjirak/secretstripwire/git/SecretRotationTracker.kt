package com.jakubjirak.secretstripwire.git

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Tracks the age of a known secret across commits.
 */
data class SecretAge(
    val identifier: String,
    val firstSeen: Instant,
    val lastRotated: Instant,
    val ageInDays: Long,
    val needsRotation: Boolean,
)

/**
 * Tracks secret ages across commits and warns when secrets exceed
 * a configurable rotation threshold. Default threshold is 90 days.
 */
@Service(Service.Level.PROJECT)
class SecretRotationTracker(
    @Suppress("UNUSED_PARAMETER") private val project: Project,
) {

    private val trackedSecrets = mutableMapOf<String, SecretAge>()
    private var rotationThresholdDays: Long = DEFAULT_ROTATION_THRESHOLD_DAYS

    fun setRotationThreshold(days: Long) {
        rotationThresholdDays = days
        LOG.info("Rotation threshold set to $days days")
    }

    fun getRotationThreshold(): Long = rotationThresholdDays

    fun trackSecret(identifier: String, firstSeen: Instant, lastRotated: Instant) {
        val ageInDays = ChronoUnit.DAYS.between(lastRotated, Instant.now())
        val needsRotation = ageInDays > rotationThresholdDays
        val secretAge = SecretAge(
            identifier = identifier,
            firstSeen = firstSeen,
            lastRotated = lastRotated,
            ageInDays = ageInDays,
            needsRotation = needsRotation,
        )
        trackedSecrets[identifier] = secretAge
        LOG.info("Tracking secret '$identifier': age=${ageInDays}d, needsRotation=$needsRotation")
    }

    fun recordRotation(identifier: String) {
        val existing = trackedSecrets[identifier] ?: return
        val updated = existing.copy(
            lastRotated = Instant.now(),
            ageInDays = 0,
            needsRotation = false,
        )
        trackedSecrets[identifier] = updated
        LOG.info("Recorded rotation for secret '$identifier'")
    }

    fun getSecretAge(identifier: String): SecretAge? = trackedSecrets[identifier]

    fun getSecretsNeedingRotation(): List<SecretAge> =
        trackedSecrets.values.filter { it.needsRotation }

    fun getAllTrackedSecrets(): List<SecretAge> = trackedSecrets.values.toList()

    fun checkAge(identifier: String, firstSeen: Instant, lastRotated: Instant): SecretAge {
        val ageInDays = ChronoUnit.DAYS.between(lastRotated, Instant.now())
        val needsRotation = ageInDays > rotationThresholdDays
        return SecretAge(
            identifier = identifier,
            firstSeen = firstSeen,
            lastRotated = lastRotated,
            ageInDays = ageInDays,
            needsRotation = needsRotation,
        )
    }

    companion object {
        private val LOG = Logger.getInstance(SecretRotationTracker::class.java)
        const val DEFAULT_ROTATION_THRESHOLD_DAYS = 90L
    }
}
