package com.jakubjirak.secretstripwire.settings

import com.jakubjirak.secretstripwire.model.SecretPattern
import com.jakubjirak.secretstripwire.model.SecretType
import com.jakubjirak.secretstripwire.model.SeverityLevel
import com.jakubjirak.secretstripwire.scanner.EntropyAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection

@State(
    name = "SecretsTripwireSettings",
    storages = [Storage(value = "secretsTripwireSettings.xml", roamingType = RoamingType.DEFAULT)],
)
class SecretsTripwireSettings : PersistentStateComponent<SecretsTripwireSettings> {

    var enabled: Boolean = true
    var entropyThreshold: Double = EntropyAnalyzer.DEFAULT_THRESHOLD
    var entropyDetectionEnabled: Boolean = false
    var preCommitGuardEnabled: Boolean = true

    @XCollection(style = XCollection.Style.v2)
    var customPatterns: MutableList<SerializablePattern> = mutableListOf()

    override fun getState(): SecretsTripwireSettings = this

    override fun loadState(state: SecretsTripwireSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun resolveCustomPatterns(): List<SecretPattern> = customPatterns.map { it.toSecretPattern() }

    companion object {
        fun getInstance(): SecretsTripwireSettings =
            ApplicationManager.getApplication().getService(SecretsTripwireSettings::class.java)
    }
}

@Tag("pattern")
data class SerializablePattern(
    var id: String = "",
    var name: String = "",
    var regex: String = "",
    var secretType: String = "GENERIC_SECRET",
    var severity: String = "WARNING",
    var description: String = "",
    var enabled: Boolean = true,
) {
    fun toSecretPattern(): SecretPattern = SecretPattern(
        id = id,
        name = name,
        regex = regex,
        secretType = try {
            SecretType.valueOf(secretType)
        } catch (_: IllegalArgumentException) {
            SecretType.GENERIC_SECRET
        },
        severity = try {
            SeverityLevel.valueOf(severity)
        } catch (_: IllegalArgumentException) {
            SeverityLevel.WARNING
        },
        description = description,
        enabled = enabled,
    )

    companion object {
        fun fromSecretPattern(pattern: SecretPattern): SerializablePattern = SerializablePattern(
            id = pattern.id,
            name = pattern.name,
            regex = pattern.regex,
            secretType = pattern.secretType.name,
            severity = pattern.severity.name,
            description = pattern.description,
            enabled = pattern.enabled,
        )
    }
}
