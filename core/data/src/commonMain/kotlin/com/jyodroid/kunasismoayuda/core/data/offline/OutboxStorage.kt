package com.jyodroid.kunasismoayuda.core.data.offline

import okio.FileSystem
import okio.Path

/**
 * Per-platform file location for the SOS outbox. Both return `null` when durable storage is
 * unavailable (e.g. the Android app context has not been set yet) — in that case the outbox falls
 * back to in-memory persistence for the current session. `FileSystem.SYSTEM` is referenced only in
 * the platform source sets where it is guaranteed to exist.
 */
internal expect fun outboxFileSystem(): FileSystem?

internal expect fun outboxFilePath(): Path?
