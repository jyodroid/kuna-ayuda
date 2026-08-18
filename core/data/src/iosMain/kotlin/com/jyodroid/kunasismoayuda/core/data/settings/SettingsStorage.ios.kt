package com.jyodroid.kunasismoayuda.core.data.settings

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual fun settingsFileSystem(): FileSystem? = FileSystem.SYSTEM

internal actual fun settingsFilePath(): Path? {
    val documents = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).firstOrNull() as? String ?: return null
    return "$documents/country_settings.json".toPath()
}
