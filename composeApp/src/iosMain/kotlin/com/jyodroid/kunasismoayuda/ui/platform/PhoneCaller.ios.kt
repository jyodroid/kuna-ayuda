package com.jyodroid.kunasismoayuda.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

/**
 * Opens `tel:` through `UIApplication` directly. We build the `NSURL` from a **sanitized** number so
 * spaces/dashes can't yield a `nil` URL (the old failure), then:
 * - **iPhone:** `canOpenURL` is true, so `openURL:options:completionHandler:` launches the dialer.
 * - **Simulator / iPad / iPod:** there is no Phone app, so `tel:` can't open (an Apple limitation).
 *   Rather than a dead-looking tap, copy the number to the clipboard so it isn't lost.
 */
@Composable
actual fun rememberPhoneCaller(): PhoneCaller = remember {
    PhoneCaller { raw ->
        val number = sanitizePhoneNumber(raw) ?: return@PhoneCaller
        val url = NSURL(string = "tel:$number")
        val app = UIApplication.sharedApplication
        if (app.canOpenURL(url)) {
            app.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
        } else {
            UIPasteboard.generalPasteboard.string = number
        }
    }
}
