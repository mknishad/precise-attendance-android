package com.monir.preciseattendance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// primary is the action blue, secondary is the brand navy used for headings, and
// secondaryContainer is the muted blue-grey of disabled controls.
private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = BlueTint,
    onPrimaryContainer = BlueTintOn,
    secondary = Navy40,
    onSecondary = Color.White,
    secondaryContainer = BlueGreyMuted,
    onSecondaryContainer = BlueGreyOnMuted,
    error = Red40,
    onError = Color.White,
    errorContainer = RedTint,
    onErrorContainer = RedTintOn,
    background = PaleBlueGrey,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = MistGrey,
    onSurfaceVariant = SlateGrey,
    outline = OutlineGrey,
    outlineVariant = OutlineSoftGrey,
)

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = NightBlueTint,
    primaryContainer = NightBlueTint,
    onPrimaryContainer = NightBlueTintOn,
    secondary = Navy80,
    onSecondary = Navy40,
    secondaryContainer = NightMuted,
    onSecondaryContainer = NightMutedOn,
    error = Red80,
    onError = NightRedTint,
    errorContainer = NightRedTint,
    onErrorContainer = NightRedTintOn,
    background = NightBackground,
    onBackground = NightOnSurface,
    surface = NightSurface,
    onSurface = NightOnSurface,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = NightOnSurfaceVariant,
    outline = NightOutline,
    outlineVariant = NightOutlineSoft,
)

@Composable
fun AttendanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAttendanceColors provides if (darkTheme) DarkAttendanceColors else LightAttendanceColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = AttendanceShapes,
            content = content,
        )
    }
}
