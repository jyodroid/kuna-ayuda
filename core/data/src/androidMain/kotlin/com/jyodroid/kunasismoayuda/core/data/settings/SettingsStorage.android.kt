package com.jyodroid.kunasismoayuda.core.data.settings

import com.jyodroid.kunasismoayuda.core.data.offline.AndroidOutboxContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

internal actual fun settingsFileSystem(): FileSystem? =
    if (AndroidOutboxContext.appContext != null) FileSystem.SYSTEM else null

internal actual fun settingsFilePath(): Path? =
    AndroidOutboxContext.appContext?.filesDir?.resolve(SETTINGS_FILE_NAME)?.absolutePath?.toPath()

private const val SETTINGS_FILE_NAME = "country_settings.json"
