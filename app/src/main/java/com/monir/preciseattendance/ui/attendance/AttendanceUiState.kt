package com.monir.preciseattendance.ui.attendance

import androidx.compose.runtime.Immutable
import com.monir.preciseattendance.domain.model.AttendanceStatus
import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.model.OfficeLocation

@Immutable
data class AttendanceUiState(
    val locationStatus: LocationStatus = LocationStatus.AcquiringFix,
    val currentLocation: GeoPoint? = null,
    val officeLocation: OfficeLocation? = null,
    val attendanceStatus: AttendanceStatus = AttendanceStatus.NotMarked,
)
