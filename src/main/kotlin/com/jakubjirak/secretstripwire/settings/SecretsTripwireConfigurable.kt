package com.jakubjirak.secretstripwire.settings

import com.jakubjirak.secretstripwire.scanner.EntropyAnalyzer
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.JBTable
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class SecretsTripwireConfigurable : BoundConfigurable("Secrets Tripwire") {

    private val settings get() = SecretsTripwireSettings.getInstance()

    private val columnNames = arrayOf("Name", "Regex", "Severity", "Enabled")
    private val patternsTableModel = object : DefaultTableModel(columnNames, 0) {
        override fun getColumnClass(columnIndex: Int): Class<*> = when (columnIndex) {
            3 -> java.lang.Boolean::class.java
            else -> String::class.java
        }

        override fun isCellEditable(row: Int, column: Int): Boolean = true
    }

    private val patternsTable = JBTable(patternsTableModel)

    private val tablePanel: JPanel = ToolbarDecorator.createDecorator(patternsTable)
        .setAddAction {
            patternsTableModel.addRow(arrayOf<Any>("New Pattern", ".*", "WARNING", true))
        }
        .setRemoveAction {
            val selectedRow = patternsTable.selectedRow
            if (selectedRow >= 0) {
                patternsTableModel.removeRow(selectedRow)
            }
        }
        .createPanel()

    init {
        loadTableFromSettings()
    }

    override fun createPanel() = panel {
        group("General") {
            row {
                checkBox("Enable Secrets Tripwire")
                    .bindSelected(settings::enabled)
                    .comment("Enable real-time scanning for secrets in your code")
            }
            row {
                checkBox("Pre-commit guard")
                    .bindSelected(settings::preCommitGuardEnabled)
                    .comment("Scan staged files before commit and warn when potential secrets are found")
            }
        }
        group("Entropy Analysis") {
            row {
                checkBox("Detect high-entropy strings")
                    .bindSelected(settings::entropyDetectionEnabled)
                    .comment("Flag quoted strings with unusually high randomness even when no known pattern matches")
            }
            row("Entropy threshold:") {
                textField()
                    .bindText(
                        { settings.entropyThreshold.toString() },
                        { settings.entropyThreshold = it.toDoubleOrNull() ?: EntropyAnalyzer.DEFAULT_THRESHOLD }
                    )
                    .comment(
                        "Shannon entropy threshold for detecting high-entropy strings " +
                            "(default: ${EntropyAnalyzer.DEFAULT_THRESHOLD}). Lower values catch more potential " +
                            "secrets but may produce false positives."
                    )
            }
        }
        group("Custom Patterns (paid feature: max 10 free)") {
            row {
                cell(tablePanel)
                    .align(Align.FILL)
            }.resizableRow()
        }
    }

    override fun isModified(): Boolean {
        if (super.isModified()) return true
        return getTablePatterns() != settings.customPatterns
    }

    override fun apply() {
        super.apply()
        settings.customPatterns = getTablePatterns().toMutableList()
    }

    override fun reset() {
        super.reset()
        loadTableFromSettings()
    }

    private fun loadTableFromSettings() {
        patternsTableModel.rowCount = 0
        settings.customPatterns.forEach { pattern ->
            patternsTableModel.addRow(arrayOf<Any>(pattern.name, pattern.regex, pattern.severity, pattern.enabled))
        }
    }

    private fun getTablePatterns(): List<SerializablePattern> {
        return (0 until patternsTableModel.rowCount).map { row ->
            val name = patternsTableModel.getValueAt(row, 0) as? String ?: ""
            val regex = patternsTableModel.getValueAt(row, 1) as? String ?: ""
            // Preserve fields not shown in the table (type, description) from the saved pattern
            val existing = settings.customPatterns.find { it.name == name && it.regex == regex }
            SerializablePattern(
                id = existing?.id ?: "custom_$row",
                name = name,
                regex = regex,
                secretType = existing?.secretType ?: "GENERIC_SECRET",
                severity = patternsTableModel.getValueAt(row, 2) as? String ?: "WARNING",
                description = existing?.description ?: "",
                enabled = patternsTableModel.getValueAt(row, 3) as? Boolean ?: true,
            )
        }
    }
}
