package com.jyodroid.kunasismoayuda.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Kuna Ayuda brand theme. The palette is harmonically derived from the logo teal **#0F5E66**
 * (primary): a teal-family primary/secondary, a harmonic blue tertiary accent, and teal-tinted
 * neutrals. `error` stays a strong red on purpose — SOS/danger must never read as brand teal.
 *
 * Dark mode is a pending follow-up (iOS 27 expects it) — see the architecture/backlog notes. When it
 * lands, add `DarkKunaColors` and switch on `isSystemInDarkTheme()` here; callers won't change.
 */

// Brand teal seed and its tonal relatives.
private val Primary = Color(0xFF0F5E66)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFA2EEF6)
private val OnPrimaryContainer = Color(0xFF00201F)

private val Secondary = Color(0xFF4A6466)
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFCCE8EA)
private val OnSecondaryContainer = Color(0xFF051F21)

// Harmonic blue accent (analogous to teal), for highlights that aren't the primary action.
private val Tertiary = Color(0xFF4C5F7C)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFD5E3FF)
private val OnTertiaryContainer = Color(0xFF05192F)

// Teal-tinted neutrals so surfaces feel part of the brand, not clinically grey.
private val Background = Color(0xFFF5FAFB)
private val OnBackground = Color(0xFF171D1D)
private val Surface = Color(0xFFF5FAFB)
private val OnSurface = Color(0xFF171D1D)
private val SurfaceVariant = Color(0xFFDAE4E5)
private val OnSurfaceVariant = Color(0xFF3F484A)
private val Outline = Color(0xFF6F797A)
private val InversePrimary = Color(0xFF83D3DA)

// Danger stays red — never brand teal (accessibility + SOS semantics).
private val ErrorColor = Color(0xFFBA1A1A)
private val OnError = Color(0xFFFFFFFF)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF410002)

private val LightKunaColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    inversePrimary = InversePrimary,
    surfaceTint = Primary,
    error = ErrorColor,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
)

// ---- Dark scheme: same teal identity, lifted to readable tones on dark surfaces. ----
// Primary/secondary/tertiary become lighter tints (legible on near-black); containers go dark.
private val DarkKunaColors = darkColorScheme(
    primary = Color(0xFF83D3DA),
    onPrimary = Color(0xFF00363B),
    primaryContainer = Color(0xFF004E54),
    onPrimaryContainer = Color(0xFFA2EEF6),
    secondary = Color(0xFFB0CBCD),
    onSecondary = Color(0xFF1B3437),
    secondaryContainer = Color(0xFF324B4D),
    onSecondaryContainer = Color(0xFFCCE8EA),
    tertiary = Color(0xFFB4C8E9),
    onTertiary = Color(0xFF1D314B),
    tertiaryContainer = Color(0xFF344863),
    onTertiaryContainer = Color(0xFFD5E3FF),
    background = Color(0xFF0E1415),
    onBackground = Color(0xFFDEE4E4),
    surface = Color(0xFF0E1415),
    onSurface = Color(0xFFDEE4E4),
    surfaceVariant = Color(0xFF3F484A),
    onSurfaceVariant = Color(0xFFBEC8C9),
    outline = Color(0xFF899393),
    inversePrimary = Color(0xFF0F5E66),
    surfaceTint = Color(0xFF83D3DA),
    // Danger stays a STRONG red (not the pale Material dark-error tone) so the SOS button and
    // alert cards remain unmistakably alarming in dark mode too — white text on a solid red.
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun KunaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkKunaColors else LightKunaColors,
        content = content,
    )
}
