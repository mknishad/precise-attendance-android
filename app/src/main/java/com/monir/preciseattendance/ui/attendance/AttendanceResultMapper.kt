package com.monir.preciseattendance.ui.attendance

import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.usecase.MarkAttendanceResult
import com.monir.preciseattendance.domain.usecase.SetOfficeLocationResult

private fun message(message: StatusMessage): AttendanceUiEvent = AttendanceUiEvent.ShowMessage(message)

/** Every [SetOfficeLocationResult] becomes exactly one event. */
internal fun SetOfficeLocationResult.toEvent(): AttendanceUiEvent = when (this) {
    is SetOfficeLocationResult.Success -> message(StatusMessage.OfficeSaved)
    is SetOfficeLocationResult.WeakSignal ->
        message(StatusMessage.OfficeWeakAccuracy(accuracyMeters.accuracyToWholeMetersOrNull()))
    is SetOfficeLocationResult.LocationUnavailable -> error.toEvent()
    SetOfficeLocationResult.StorageFailure -> message(StatusMessage.OfficeSaveFailed)
}

/** Every [MarkAttendanceResult] becomes exactly one event. */
internal fun MarkAttendanceResult.toEvent(): AttendanceUiEvent = when (this) {
    is MarkAttendanceResult.Success -> AttendanceUiEvent.AttendanceMarked
    MarkAttendanceResult.NoOfficeSet -> message(StatusMessage.NoOfficeSet)
    is MarkAttendanceResult.OutsideAttendanceWindow ->
        message(StatusMessage.OutsideWindow(window.start, window.end))
    is MarkAttendanceResult.WeakSignal -> message(StatusMessage.WeakSignal(accuracyMeters.accuracyToWholeMetersOrNull()))
    is MarkAttendanceResult.OutOfRange -> message(outOfRangeMessage(distanceMeters))
    is MarkAttendanceResult.LocationUnavailable -> error.toEvent()
    MarkAttendanceResult.StorageFailure -> message(StatusMessage.AttendanceSaveFailed)
}

/** Turning location on is something the UI can offer to do, so it is an action, not a message. */
internal fun LocationError.toEvent(): AttendanceUiEvent = when (this) {
    LocationError.PermissionDenied -> message(StatusMessage.PermissionNeeded)
    LocationError.ServicesDisabled -> AttendanceUiEvent.RequestLocationSettingsResolution
    LocationError.Timeout -> message(StatusMessage.LocationTimedOut)
    LocationError.Unavailable,
    LocationError.ProviderFailure,
    -> message(StatusMessage.LocationRequestFailed)
}
