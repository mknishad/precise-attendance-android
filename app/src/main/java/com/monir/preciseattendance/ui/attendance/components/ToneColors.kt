package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.monir.preciseattendance.ui.attendance.Tone
import com.monir.preciseattendance.ui.theme.attendanceColors

/** A tone resolved against the current theme: a strong [accent] and a soft container pair. */
@Immutable
data class ToneColors(val accent: Color, val container: Color, val onContainer: Color)

@Composable
fun Tone.colors(): ToneColors {
    val scheme = MaterialTheme.colorScheme
    val extra = MaterialTheme.attendanceColors
    return when (this) {
        Tone.Success -> ToneColors(extra.success, extra.successContainer, extra.onSuccessContainer)
        Tone.Error -> ToneColors(scheme.error, scheme.errorContainer, scheme.onErrorContainer)
        Tone.Warning -> ToneColors(extra.warning, extra.warningContainer, extra.onWarningContainer)
        Tone.Neutral -> ToneColors(scheme.outline, scheme.surfaceVariant, scheme.onSurfaceVariant)
    }
}
