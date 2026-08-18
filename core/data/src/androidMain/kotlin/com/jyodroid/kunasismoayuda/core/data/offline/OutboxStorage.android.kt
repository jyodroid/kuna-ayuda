package com.jyodroid.kunasismoayuda.core.data.offline

import android.content.Context
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Holds the application context so the offline outbox can find the app-private files dir. Set once
 * from the Android app (e.g. in MainActivity), the same pattern the location provider uses. Until
 * it is set, the outbox degrades to in-memory persistence.
 */
object AndroidOutboxContext {
    @Volatile
    var appContext: Context? = null
}

internal actual fun outboxFileSystem(): FileSystem? =
    if (AndroidOutboxContext.appContext != null) FileSystem.SYSTEM else null

internal actual fun outboxFilePath(): Path? =
    AndroidOutboxContext.appContext?.filesDir?.resolve(OUTBOX_FILE_NAME)?.absolutePath?.toPath()

private const val OUTBOX_FILE_NAME = "sos_outbox.json"
