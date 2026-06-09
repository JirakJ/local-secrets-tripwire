package com.jakubjirak.secretstripwire.allowlist

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads allowlist patterns from a .secrets-allowlist file in the project root.
 * Each line in the file is a pattern to ignore during scanning.
 * Lines starting with # are comments.
 *
 * Results are cached per project and invalidated by the file's modification
 * stamp, because the inspection runs on every editor pass.
 */
object AllowlistLoader {

    private val LOG = Logger.getInstance(AllowlistLoader::class.java)
    const val ALLOWLIST_FILENAME = ".secrets-allowlist"

    private data class CacheEntry(val lastModified: Long, val entries: List<String>)

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    fun loadAllowlist(project: Project): List<String> {
        val basePath = project.basePath ?: return emptyList()
        val allowlistFile = File(basePath, ALLOWLIST_FILENAME)

        if (!allowlistFile.exists()) {
            cache.remove(basePath)
            return emptyList()
        }

        val stamp = allowlistFile.lastModified()
        cache[basePath]?.let { cached ->
            if (cached.lastModified == stamp) return cached.entries
        }

        return try {
            val entries = parseAllowlist(allowlistFile.readText())
            cache[basePath] = CacheEntry(stamp, entries)
            entries
        } catch (e: Exception) {
            LOG.warn("Failed to load allowlist from $ALLOWLIST_FILENAME: ${e.message}")
            emptyList()
        }
    }

    /** Parses allowlist content; pure logic shared with tests. */
    fun parseAllowlist(content: String): List<String> =
        content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
}
