package com.monir.preciseattendance.ui.attendance

import android.content.Context
import com.monir.preciseattendance.R
import com.monir.preciseattendance.ui.location.LocationSettingsResolver
import com.monir.preciseattendance.ui.location.openAppSettings

/** Performs one-off effects. Anything the user should read goes through [showMessage]. */
internal fun handleAttendanceEvent(
    event: AttendanceUiEvent,
    context: Context,
    settingsResolver: LocationSettingsResolver,
    showMessage: (String) -> Unit,
) {
    when (event) {
        is AttendanceUiEvent.ShowMessage -> showMessage(context.resources.statusText(event.message))
        AttendanceUiEvent.RequestLocationSettingsResolution -> settingsResolver.resolve()
        AttendanceUiEvent.OpenAppSettings -> {
            if (!context.openAppSettings()) {
                showMessage(context.resources.statusText(StatusMessage.CouldNotOpenSettings))
            }
        }
        AttendanceUiEvent.AttendanceMarked -> showMessage(context.getString(R.string.status_attendance_marked))
    }
}
