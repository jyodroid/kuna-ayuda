package com.jyodroid.kunasismoayuda.core.data.settings

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

internal actual fun settingsFileSystem(): FileSystem? = FileSystem.SYSTEM

internal actual fun settingsFilePath(): Path? {
    val home = System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir") ?: "."
    return "$home/.kunasismoayuda/country_settings.json".toPath()
}
