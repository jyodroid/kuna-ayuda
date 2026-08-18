package com.jyodroid.kunasismoayuda.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.desktop_number_copied
import org.jetbrains.compose.resources.stringResource
import java.awt.Color
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import javax.swing.JLabel
import javax.swing.JWindow
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.border.EmptyBorder

/**
 * Desktop has no dialer. Best effort: try a `tel:` hand-off (macOS FaceTime or a registered VoIP
 * app can take it); if the platform can't browse `tel:`, copy the sanitized number to the clipboard
 * and pop a brief, auto-dismissing toast so the user knows it happened (otherwise the tap looks dead).
 */
@Composable
actual fun rememberPhoneCaller(): PhoneCaller {
    val copiedLabel = stringResource(Res.string.desktop_number_copied)
    return remember(copiedLabel) {
        PhoneCaller { raw ->
            val number = sanitizePhoneNumber(raw) ?: return@PhoneCaller
            val handedOff = runCatching {
                val desktop = Desktop.getDesktop().takeIf { Desktop.isDesktopSupported() }
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI("tel:$number"))
                    true
                } else {
                    false
                }
            }.getOrDefault(false)
            if (!handedOff) {
                val copied = runCatching {
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(number), null)
                }.isSuccess
                if (copied) showToast("$copiedLabel: $number")
            }
        }
    }
}

/** A small undecorated toast shown bottom-right of the screen for ~2.5 s, then disposed. EDT-safe. */
private fun showToast(message: String) {
    SwingUtilities.invokeLater {
        runCatching {
            val window = JWindow()
            val label = JLabel(message).apply {
                isOpaque = true
                background = Color(0x2B, 0x2B, 0x2B)
                foreground = Color.WHITE
                border = EmptyBorder(12, 18, 12, 18)
            }
            window.contentPane.add(label)
            window.pack()
            val screen = Toolkit.getDefaultToolkit().screenSize
            window.setLocation(screen.width - window.width - 24, screen.height - window.height - 80)
            window.isAlwaysOnTop = true
            window.isVisible = true
            Timer(2500) { window.dispose() }.apply { isRepeats = false; start() }
        }
    }
}
