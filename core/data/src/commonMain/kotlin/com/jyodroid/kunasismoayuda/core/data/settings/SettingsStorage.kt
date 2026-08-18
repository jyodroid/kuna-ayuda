package com.jyodroid.kunasismoayuda.core.data.settings

import okio.FileSystem
import okio.Path

/**
 * Per-platform file location for small app settings (currently just the chosen country). Both return
 * `null` when durable storage is unavailable (e.g. the Android app context has not been set yet), in
 * which case [CountryStore] keeps the choice in memory for the current session only. Mirrors the SOS
 * outbox storage (`OutboxStorage`).
 */
internal expect fun settingsFileSystem(): FileSystem?

internal expect fun settingsFilePath(): Path?
