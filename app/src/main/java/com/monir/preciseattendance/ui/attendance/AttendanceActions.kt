package com.monir.preciseattendance.ui.attendance

import androidx.compose.runtime.Immutable

/** What the screen content can ask for; keeps the content free of ViewModel and framework types. */
@Immutable
data class AttendanceActions(
    val onBack: () -> Unit = {},
    val onRequestPermission: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
    val onEnableLocation: () -> Unit = {},
    val onSetOfficeLocation: () -> Unit = {},
    val onMarkAttendance: () -> Unit = {},
)
