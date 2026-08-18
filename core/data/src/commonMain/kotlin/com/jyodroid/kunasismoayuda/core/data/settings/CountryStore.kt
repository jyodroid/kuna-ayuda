package com.jyodroid.kunasismoayuda.core.data.settings

import com.jyodroid.kunasismoayuda.core.domain.model.Country
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Persists the user's chosen [Country] to a tiny JSON file (`{"country":"CO"}`), so the app skips the
 * first-run picker on subsequent launches. [get] returns `null` until a country has ever been chosen.
 * Falls back to in-memory storage when no writable location is available (see [SettingsStorage]).
 */
class CountryStore(
    private val fileSystem: FileSystem? = settingsFileSystem(),
    private val path: Path? = settingsFilePath(),
) {

    @Serializable
    private data class Settings(val country: String? = null)

    private val json = Json { ignoreUnknownKeys = true }

    // Session-only fallback used when durable storage is unavailable.
    private var memory: String? = null

    /** The chosen country, or `null` if the user has never picked one (⇒ show the picker). */
    suspend fun get(): Country? = withContext(Dispatchers.Default) {
        val code = readCode()
        code?.let { Country.fromCode(it) }
    }

    suspend fun set(country: Country) = withContext(Dispatchers.Default) {
        writeCode(country.code)
    }

    private fun readCode(): String? {
        val fs = fileSystem
        val p = path
        if (fs == null || p == null) return memory
        if (!fs.exists(p)) return null
        return runCatching {
            val text = fs.read(p) { readUtf8() }
            json.decodeFromString<Settings>(text).country
        }.getOrNull()
    }

    private fun writeCode(code: String) {
        val fs = fileSystem
        val p = path
        if (fs == null || p == null) {
            memory = code
            return
        }
        p.parent?.let { fs.createDirectories(it) }
        val text = json.encodeToString(Settings(country = code))
        val tmp = "${p}.tmp".toPath()
        fs.write(tmp) { writeUtf8(text) }
        fs.atomicMove(tmp, p)
    }
}
