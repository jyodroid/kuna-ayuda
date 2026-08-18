package com.jyodroid.kunasismoayuda.core.data.offline

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual fun outboxFileSystem(): FileSystem? = FileSystem.SYSTEM

internal actual fun outboxFilePath(): Path? {
    val documents = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).firstOrNull() as? String ?: return null
    return "$documents/sos_outbox.json".toPath()
}
