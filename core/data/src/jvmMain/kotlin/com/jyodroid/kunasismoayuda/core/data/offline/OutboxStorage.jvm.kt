package com.jyodroid.kunasismoayuda.core.data.offline

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

internal actual fun outboxFileSystem(): FileSystem? = FileSystem.SYSTEM

internal actual fun outboxFilePath(): Path? {
    val home = System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir") ?: "."
    return "$home/.kunasismoayuda/sos_outbox.json".toPath()
}
