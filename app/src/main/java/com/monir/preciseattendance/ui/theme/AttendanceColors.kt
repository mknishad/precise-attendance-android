package com.monir.preciseattendance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Semantic roles Material 3 does not define: success and warning, in the same four-part shape. */
@Immutable
data class AttendanceColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

val LightAttendanceColors = AttendanceColors(
    success = Green40,
    onSuccess = Color.White,
    successContainer = GreenTint,
    onSuccessContainer = GreenTintOn,
    warning = Amber40,
    onWarning = Color.White,
    warningContainer = AmberTint,
    onWarningContainer = AmberTintOn,
)

val DarkAttendanceColors = AttendanceColors(
    success = Green80,
    onSuccess = NightGreenTint,
    successContainer = NightGreenTint,
    onSuccessContainer = NightGreenTintOn,
    warning = Amber80,
    onWarning = NightAmberTint,
    warningContainer = NightAmberTint,
    onWarningContainer = NightAmberTintOn,
)

val LocalAttendanceColors = staticCompositionLocalOf { LightAttendanceColors }

val MaterialTheme.attendanceColors: AttendanceColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAttendanceColors.current
