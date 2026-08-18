package com.jyodroid.kunasismoayuda.core.data.offline

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Durable storage for the SOS outbox: the whole list is (de)serialized to a single JSON file with
 * an atomic temp-file swap so a crash mid-write can never corrupt an in-flight SOS. Falls back to an
 * in-memory list when no writable location is available (see [OutboxStorage]).
 *
 * The file system and path are injectable so tests can supply an in-memory `FakeFileSystem`.
 */
class SosOutboxStore(
    private val fileSystem: FileSystem? = outboxFileSystem(),
    private val path: Path? = outboxFilePath(),
) {

    private val json = Json { ignoreUnknownKeys = true }

    // Session-only fallback used when durable storage is unavailable.
    private var memory: List<QueuedSos> = emptyList()

    suspend fun read(): List<QueuedSos> = withContext(Dispatchers.Default) {
        val fs = fileSystem
        val p = path
        if (fs == null || p == null) return@withContext memory
        if (!fs.exists(p)) return@withContext emptyList()
        runCatching {
            val text = fs.read(p) { readUtf8() }
            json.decodeFromString<List<QueuedSos>>(text)
        }.getOrDefault(emptyList())
    }

    suspend fun write(items: List<QueuedSos>) = withContext(Dispatchers.Default) {
        val fs = fileSystem
        val p = path
        if (fs == null || p == null) {
            memory = items
            return@withContext
        }
        p.parent?.let { fs.createDirectories(it) }
        val text = json.encodeToString(items)
        val tmp = "${p}.tmp".toPath()
        fs.write(tmp) { writeUtf8(text) }
        fs.atomicMove(tmp, p)
    }
}
