package com.monir.preciseattendance.ui.attendance

import android.content.Context
import android.widget.Toast
import com.monir.preciseattendance.R
import com.monir.preciseattendance.ui.location.LocationSettingsResolver
import com.monir.preciseattendance.ui.location.openAppSettings

/** Performs one-off effects. Messages are shown as toasts until the real UI replaces them. */
internal fun handleAttendanceEvent(
    event: AttendanceUiEvent,
    context: Context,
    settingsResolver: LocationSettingsResolver,
) {
    when (event) {
        is AttendanceUiEvent.ShowMessage -> toast(context, context.resources.statusText(event.message))
        AttendanceUiEvent.RequestLocationSettingsResolution -> settingsResolver.resolve()
        AttendanceUiEvent.OpenAppSettings -> {
            if (!context.openAppSettings()) {
                toast(context, context.resources.statusText(StatusMessage.CouldNotOpenSettings))
            }
        }
        AttendanceUiEvent.AttendanceMarked -> toast(context, context.getString(R.string.status_attendance_marked))
    }
}

private fun toast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}
