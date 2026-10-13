package com.monir.preciseattendance.ui.attendance

import androidx.compose.runtime.Immutable

/** What the device can currently tell us about the user's position relative to the office. */
@Immutable
sealed interface LocationStatus {
    data object PermissionMissing : LocationStatus
    data object PermissionPermanentlyDenied : LocationStatus
    data object GpsDisabled : LocationStatus
    data object AcquiringFix : LocationStatus
    data class InRange(val distanceMeters: Double) : LocationStatus
    data class OutOfRange(val distanceMeters: Double) : LocationStatus
}
