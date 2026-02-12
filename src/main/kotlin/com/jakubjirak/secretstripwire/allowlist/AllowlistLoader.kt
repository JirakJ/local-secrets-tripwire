package com.jakubjirak.secretstripwire.allowlist

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.File

/**
 * Loads allowlist patterns from a .secrets-allowlist file in the project root.
 * Each line in the file is a pattern to ignore during scanning.
 * Lines starting with # are comments.
 */
object AllowlistLoader {

    private val LOG = Logger.getInstance(AllowlistLoader::class.java)
    private const val ALLOWLIST_FILENAME = ".secrets-allowlist"

    fun loadAllowlist(project: Project): List<String> {
        val basePath = project.basePath ?: return emptyList()
        val allowlistFile = File(basePath, ALLOWLIST_FILENAME)

        if (!allowlistFile.exists()) return emptyList()

        return try {
            allowlistFile.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
        } catch (e: Exception) {
            LOG.warn("Failed to load allowlist from $ALLOWLIST_FILENAME: ${e.message}")
            emptyList()
        }
    }
}
