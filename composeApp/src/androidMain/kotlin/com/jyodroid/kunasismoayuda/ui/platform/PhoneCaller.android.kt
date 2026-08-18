package com.jyodroid.kunasismoayuda.ui.platform

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Launches the system dialer (pre-filled, not auto-dialing) via `ACTION_DIAL` — no permission needed. */
@Composable
actual fun rememberPhoneCaller(): PhoneCaller {
    val context = LocalContext.current
    return remember(context) {
        PhoneCaller { raw ->
            val number = sanitizePhoneNumber(raw) ?: return@PhoneCaller
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(intent) }
        }
    }
}
