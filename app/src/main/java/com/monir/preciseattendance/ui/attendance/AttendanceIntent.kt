package com.monir.preciseattendance.ui.attendance

/** User actions and system callbacks the screen sends to the ViewModel. */
sealed interface AttendanceIntent {
    data object SetOfficeLocationClicked : AttendanceIntent
    data object MarkAttendanceClicked : AttendanceIntent
    data object OpenAppSettingsClicked : AttendanceIntent
    data object OpenLocationSettingsClicked : AttendanceIntent
    data class PermissionResult(
        val granted: Boolean,
        val permanentlyDenied: Boolean,
    ) : AttendanceIntent
}
