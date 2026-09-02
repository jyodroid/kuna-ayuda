package com.jyodroid.kunasismoayuda.ui.platform

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Hands off to the user's default maps app via a `geo:` `ACTION_VIEW` intent (shows a chooser when more
 * than one is installed). Android has no Apple-Maps constraint. The optional label becomes the pin title
 * (`geo:lat,lon?q=lat,lon(Label)`).
 */
@Composable
actual fun rememberMapLauncher(): MapLauncher {
    val context = LocalContext.current
    return remember(context) {
        MapLauncher { latitude, longitude, label ->
            val point = "$latitude,$longitude"
            val query = label?.takeIf { it.isNotBlank() }?.let { "$point(${Uri.encode(it)})" } ?: point
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$point?q=$query")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(intent) }
        }
    }
}
