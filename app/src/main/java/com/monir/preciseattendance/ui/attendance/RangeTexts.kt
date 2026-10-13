package com.monir.preciseattendance.ui.attendance

import android.content.res.Resources
import com.monir.preciseattendance.R
import com.monir.preciseattendance.domain.model.AttendancePolicy
import com.monir.preciseattendance.domain.model.ProximityResult

/** The sentence about distance ("You are 120 m away from the office"), or null with no distance. */
fun AttendanceUiState.supportingText(resources: Resources): String? {
    val metres = displayDistanceMeters ?: return null
    return when (proximity) {
        is ProximityResult.InRange -> resources.getString(R.string.status_in_range, metres)
        is ProximityResult.OutOfRange -> resources.getString(R.string.status_out_of_range, metres)
        else -> null
    }
}

/** The small grey line under the chip: what to do next, for every state. */
fun AttendanceUiState.helperText(resources: Resources): String = when (val message = statusMessage) {
    is StatusMessage.OutOfRange -> {
        val radius = AttendancePolicy.ALLOWED_RADIUS_METERS.toInt()
        resources.getQuantityString(R.plurals.helper_out_of_range, radius, radius)
    }
    is StatusMessage.InRange -> resources.getString(R.string.helper_ready_to_check_in)
    else -> resources.statusText(message)
}
