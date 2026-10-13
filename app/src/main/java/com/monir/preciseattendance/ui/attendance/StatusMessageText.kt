package com.monir.preciseattendance.ui.attendance

import android.content.res.Resources
import com.monir.preciseattendance.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

/** Maps a [StatusMessage] to its string resource. Exhaustive, so a new message cannot be forgotten. */
fun Resources.statusText(message: StatusMessage): String = when (message) {
    StatusMessage.PermissionNeeded -> getString(R.string.status_permission_needed)
    StatusMessage.PermissionDenied -> getString(R.string.status_permission_denied)
    StatusMessage.PermissionPermanentlyDenied -> getString(R.string.status_permission_permanently_denied)
    StatusMessage.ApproximateLocationOnly -> getString(R.string.status_approximate_location_only)
    StatusMessage.LocationOff -> getString(R.string.status_location_off)
    StatusMessage.NoOfficeSet -> getString(R.string.status_no_office_set)
    StatusMessage.AcquiringFix -> getString(R.string.status_acquiring_fix)
    is StatusMessage.WeakSignal -> accuracyText(message.accuracyMeters, R.string.status_weak_signal, R.string.status_weak_signal_unknown)
    StatusMessage.LocationUnavailable -> getString(R.string.status_location_unavailable)
    is StatusMessage.OutOfRange -> getString(R.string.status_out_of_range, message.distanceMeters)
    is StatusMessage.InRange -> getString(R.string.status_in_range, message.distanceMeters)
    is StatusMessage.OutsideWindow -> getString(R.string.status_outside_window, message.start.hhmm(), message.end.hhmm())
    StatusMessage.AlreadyMarkedToday -> getString(R.string.status_already_marked_today)
    StatusMessage.OfficeSaved -> getString(R.string.status_office_saved)
    StatusMessage.LocationTimedOut -> getString(R.string.status_location_timed_out)
    is StatusMessage.OfficeWeakAccuracy ->
        accuracyText(message.accuracyMeters, R.string.status_office_weak_accuracy, R.string.status_office_weak_accuracy_unknown)
    StatusMessage.OfficeSaveFailed -> getString(R.string.status_office_save_failed)
    StatusMessage.LocationRequestFailed -> getString(R.string.status_location_request_failed)
    StatusMessage.AttendanceSaveFailed -> getString(R.string.status_attendance_save_failed)
    StatusMessage.LocationSettingsDeclined -> getString(R.string.status_location_settings_declined)
    StatusMessage.CouldNotOpenSettings -> getString(R.string.status_could_not_open_settings)
}

private fun Resources.accuracyText(accuracyMeters: Int?, withValue: Int, withoutValue: Int): String =
    if (accuracyMeters != null) getString(withValue, accuracyMeters) else getString(withoutValue)

private fun LocalTime.hhmm(): String = format(timeFormat)
