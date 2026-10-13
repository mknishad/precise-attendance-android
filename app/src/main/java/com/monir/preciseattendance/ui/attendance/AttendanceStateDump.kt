package com.monir.preciseattendance.ui.attendance

/** Temporary: every field of the state as plain lines, for exercising each state on a device. */
internal fun AttendanceUiState.dumpLines(): List<String> = listOf(
    "permission: $permissionState",
    "location enabled: $isLocationEnabled",
    "office: ${officeLocation?.point?.let { "${it.latitude}, ${it.longitude}" } ?: "none"}",
    "proximity: $proximity",
    "distance: ${distanceMeters ?: "none"}",
    "ring progress: $distanceProgress",
    "window: ${attendanceWindow.start} to ${attendanceWindow.end}",
    "within window: $isWithinWindow",
    "already marked today: $alreadyMarkedToday",
    "setting office: $isSettingOffice",
    "marking attendance: $isMarkingAttendance",
    "mark enabled: $isMarkAttendanceEnabled",
)
