package com.monir.preciseattendance.ui.attendance

/** Everything the screen can ask of the ViewModel: user actions and system callbacks. */
sealed interface AttendanceIntent {
    /** The latest permission state, after a request and whenever the screen resumes. */
    data class PermissionResult(val state: LocationPermissionState) : AttendanceIntent
    data object SetOfficeLocationClicked : AttendanceIntent
    data object MarkAttendanceClicked : AttendanceIntent
    data class LocationSettingsResolved(val success: Boolean) : AttendanceIntent
    data object OpenSettingsClicked : AttendanceIntent
}
