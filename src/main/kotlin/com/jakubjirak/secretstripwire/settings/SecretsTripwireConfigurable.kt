package com.jakubjirak.secretstripwire.settings

import com.jakubjirak.secretstripwire.scanner.EntropyAnalyzer
import com.intellij.openapi.options.Configurable
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class SecretsTripwireConfigurable : Configurable {

    private var enabledCheckbox: JBCheckBox? = null
    private var entropyField: JBTextField? = null
    private var patternsTableModel: DefaultTableModel? = null
    private var mainPanel: JPanel? = null

    override fun getDisplayName(): String = "Secrets Tripwire"

    override fun createComponent(): JComponent {
        val settings = SecretsTripwireSettings.getInstance()

        enabledCheckbox = JBCheckBox("Enable Secrets Tripwire", settings.enabled)
        entropyField = JBTextField(settings.entropyThreshold.toString()).apply {
            emptyText.text = "${EntropyAnalyzer.DEFAULT_THRESHOLD} (default)"
        }

        val columnNames = arrayOf("Name", "Regex", "Severity", "Enabled")
        patternsTableModel = object : DefaultTableModel(columnNames, 0) {
            override fun getColumnClass(columnIndex: Int): Class<*> = when (columnIndex) {
                3 -> java.lang.Boolean::class.java
                else -> String::class.java
            }

            override fun isCellEditable(row: Int, column: Int): Boolean = true
        }

        settings.customPatterns.forEach { pattern ->
            patternsTableModel?.addRow(arrayOf<Any>(pattern.name, pattern.regex, pattern.severity, pattern.enabled))
        }

        val patternsTable = JBTable(patternsTableModel)

        val toolbarDecorator = ToolbarDecorator.createDecorator(patternsTable)
            .setAddAction {
                patternsTableModel?.addRow(arrayOf<Any>("New Pattern", ".*", "WARNING", true))
            }
            .setRemoveAction {
                val selectedRow = patternsTable.selectedRow
                if (selectedRow >= 0) {
                    patternsTableModel?.removeRow(selectedRow)
                }
            }

        val patternsPanel = toolbarDecorator.createPanel().apply {
            border = IdeBorderFactory.createTitledBorder("Custom Patterns (paid feature: max 10 free)")
        }

        mainPanel = FormBuilder.createFormBuilder()
            .addComponent(enabledCheckbox!!)
            .addLabeledComponent(JBLabel("Entropy threshold:"), entropyField!!)
            .addComponentFillVertically(patternsPanel, 10)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = SecretsTripwireSettings.getInstance()
        if (enabledCheckbox?.isSelected != settings.enabled) return true
        val threshold = entropyField?.text?.toDoubleOrNull() ?: EntropyAnalyzer.DEFAULT_THRESHOLD
        if (threshold != settings.entropyThreshold) return true
        return getTablePatterns().size != settings.customPatterns.size || getTablePatterns() != settings.customPatterns
    }

    override fun apply() {
        val settings = SecretsTripwireSettings.getInstance()
        settings.enabled = enabledCheckbox?.isSelected ?: true
        settings.entropyThreshold = entropyField?.text?.toDoubleOrNull() ?: EntropyAnalyzer.DEFAULT_THRESHOLD
        settings.customPatterns = getTablePatterns().toMutableList()
    }

    override fun reset() {
        val settings = SecretsTripwireSettings.getInstance()
        enabledCheckbox?.isSelected = settings.enabled
        entropyField?.text = settings.entropyThreshold.toString()

        patternsTableModel?.rowCount = 0
        settings.customPatterns.forEach { pattern ->
            patternsTableModel?.addRow(arrayOf<Any>(pattern.name, pattern.regex, pattern.severity, pattern.enabled))
        }
    }

    private fun getTablePatterns(): List<SerializablePattern> {
        val model = patternsTableModel ?: return emptyList()
        return (0 until model.rowCount).map { row ->
            SerializablePattern(
                id = "custom_${row}",
                name = model.getValueAt(row, 0) as? String ?: "",
                regex = model.getValueAt(row, 1) as? String ?: "",
                severity = model.getValueAt(row, 2) as? String ?: "WARNING",
                enabled = model.getValueAt(row, 3) as? Boolean ?: true,
            )
        }
    }

    override fun disposeUIResources() {
        enabledCheckbox = null
        entropyField = null
        patternsTableModel = null
        mainPanel = null
    }
}
