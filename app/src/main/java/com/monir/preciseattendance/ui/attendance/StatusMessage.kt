package com.monir.preciseattendance.ui.attendance

import java.time.LocalTime

/**
 * Everything the screen can tell the user, as data. The ViewModel never holds display text; the UI
 * maps each case to a string resource (see `StatusMessageText.kt`).
 */
sealed interface StatusMessage {

    // Standing status, shown for as long as it is true.
    data object PermissionNeeded : StatusMessage
    data object PermissionDenied : StatusMessage
    data object PermissionPermanentlyDenied : StatusMessage
    data object ApproximateLocationOnly : StatusMessage
    data object LocationOff : StatusMessage
    data object NoOfficeSet : StatusMessage
    data object AcquiringFix : StatusMessage

    /** [accuracyMeters] is null when the provider reported no usable accuracy. */
    data class WeakSignal(val accuracyMeters: Int?) : StatusMessage
    data object LocationUnavailable : StatusMessage
    data class OutOfRange(val distanceMeters: Int) : StatusMessage
    data class InRange(val distanceMeters: Int) : StatusMessage
    data class OutsideWindow(val start: LocalTime, val end: LocalTime) : StatusMessage
    data object AlreadyMarkedToday : StatusMessage

    // Outcomes of a user action, sent as one-off events.
    data object OfficeSaved : StatusMessage
    data object LocationTimedOut : StatusMessage
    data class OfficeWeakAccuracy(val accuracyMeters: Int?) : StatusMessage
    data object OfficeSaveFailed : StatusMessage
    data object LocationRequestFailed : StatusMessage
    data object AttendanceSaveFailed : StatusMessage
    data object LocationSettingsDeclined : StatusMessage
    data object CouldNotOpenSettings : StatusMessage
}
