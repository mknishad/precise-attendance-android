package com.monir.preciseattendance.ui.location

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

private const val SETTINGS_CHECK_INTERVAL_MILLIS = 2_000L

@Stable
class LocationSettingsResolver internal constructor(private val check: () -> Unit) {
    /** Checks location settings and, if needed, shows the system prompt to fix them. */
    fun resolve() = check()
}

/**
 * Reports `true` through [onResult] when location settings are (or become) good enough for a
 * high-accuracy request, and `false` when the user declines or the problem cannot be resolved here.
 */
@Composable
fun rememberLocationSettingsResolver(onResult: (Boolean) -> Unit): LocationSettingsResolver {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        currentOnResult(it.resultCode == Activity.RESULT_OK)
    }
    return remember(context, launcher) {
        LocationSettingsResolver { checkSettings(context, launcher) { currentOnResult(it) } }
    }
}

private fun checkSettings(
    context: Context,
    launcher: ActivityResultLauncher<IntentSenderRequest>,
    onResult: (Boolean) -> Unit,
) {
    val request = LocationSettingsRequest.Builder()
        .addLocationRequest(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, SETTINGS_CHECK_INTERVAL_MILLIS).build())
        .build()
    LocationServices.getSettingsClient(context)
        .checkLocationSettings(request)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { error -> resolveOrFail(error, launcher, onResult) }
}

private fun resolveOrFail(
    error: Exception,
    launcher: ActivityResultLauncher<IntentSenderRequest>,
    onResult: (Boolean) -> Unit,
) {
    if (error !is ResolvableApiException) return onResult(false)
    try {
        launcher.launch(IntentSenderRequest.Builder(error.resolution).build())
    } catch (e: IllegalStateException) {
        // The launcher was unregistered because the screen left composition before we could launch.
        onResult(false)
    }
}
