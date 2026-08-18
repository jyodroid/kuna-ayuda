package com.jyodroid.kunasismoayuda.core.data.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Device-local record of board posts this device created, keyed by post id → owner_secret (#4). Regular
 * users are anonymous, so this secret (issued by the server at creation) is the only proof that *this*
 * device owns a post and may resolve it. Persisted to a tiny JSON map next to the country settings;
 * falls back to in-memory when no writable location is available. Not sensitive beyond this device — a
 * lost file just means the owner can no longer self-resolve (an admin still can).
 */
class PostOwnershipStore(
    private val fileSystem: FileSystem? = settingsFileSystem(),
    // Sibling of the country-settings file (…/board_owners.json).
    private val path: Path? = settingsFilePath()?.parent?.let { "$it/board_owners.json".toPath() },
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var memory: Map<String, String> = emptyMap()

    /** The secret for [postId], or null if this device doesn't own it. */
    suspend fun secretFor(postId: Int): String? = withContext(Dispatchers.Default) {
        read()[postId.toString()]
    }

    /** Ids of posts this device owns (drives which posts show a "resolve" action). */
    suspend fun ownedIds(): Set<Int> = withContext(Dispatchers.Default) {
        read().keys.mapNotNull { it.toIntOrNull() }.toSet()
    }

    suspend fun remember(postId: Int, secret: String) = withContext(Dispatchers.Default) {
        mutex.withLock { write(read() + (postId.toString() to secret)) }
    }

    suspend fun forget(postId: Int) = withContext(Dispatchers.Default) {
        mutex.withLock { write(read() - postId.toString()) }
    }

    private fun read(): Map<String, String> {
        val fs = fileSystem
        val p = path
        if (fs == null || p == null) return memory
        if (!fs.exists(p)) return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, String>>(fs.read(p) { readUtf8() })
        }.getOrDefault(emptyMap())
    }

    private fun write(map: Map<String, String>) {
        val fs = fileSystem
        val p = path
        if (fs == null || p == null) {
            memory = map
            return
        }
        p.parent?.let { fs.createDirectories(it) }
        val tmp = "${p}.tmp".toPath()
        fs.write(tmp) { writeUtf8(json.encodeToString(map)) }
        fs.atomicMove(tmp, p)
    }
}
