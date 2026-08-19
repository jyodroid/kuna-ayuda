package com.jyodroid.kunasismoayuda.core.data.remote

import android.content.pm.ApplicationInfo
import com.jyodroid.kunasismoayuda.core.data.offline.AndroidOutboxContext

// Android: production by default. A *debuggable* build (i.e. a debug install from the IDE) talks to the
// emulator's host loopback. A release APK/AAB is never debuggable, so a shipped build always uses prod —
// there's no way to accidentally release a build pointing at a developer's machine.
actual fun defaultServerBaseUrl(): String {
    val ctx = AndroidOutboxContext.appContext
    val debuggable = ctx != null &&
        (ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    return if (debuggable) "http://10.0.2.2:8080" else PROD_BASE_URL
}
