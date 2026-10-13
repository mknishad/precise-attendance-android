package com.monir.preciseattendance.ui.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.monir.preciseattendance.ui.attendance.LocationPermissionState
import com.monir.preciseattendance.ui.attendance.resolveLocationPermission

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Stable
class LocationPermissionRequester internal constructor(private val launch: () -> Unit) {
    /** Shows the system permission dialog, unless the system has stopped offering it. */
    fun request() = launch()
}

/**
 * Reports the location permission state through [onStateChanged] after every request and every
 * time the screen resumes, since the user can change it in system settings while the app is away.
 */
@Composable
fun rememberLocationPermissionRequester(
    onStateChanged: (LocationPermissionState) -> Unit,
): LocationPermissionRequester {
    val context = LocalContext.current
    val currentOnStateChanged by rememberUpdatedState(onStateChanged)
    var hasRequested by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasRequested = true
        currentOnStateChanged(context.locationPermissionState(hasRequested))
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        currentOnStateChanged(context.locationPermissionState(hasRequested))
    }
    return remember(launcher) { LocationPermissionRequester { launcher.launch(LOCATION_PERMISSIONS) } }
}

private fun Context.locationPermissionState(hasRequested: Boolean): LocationPermissionState =
    resolveLocationPermission(
        fineGranted = isGranted(Manifest.permission.ACCESS_FINE_LOCATION),
        coarseGranted = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION),
        shouldShowRationale = shouldShowLocationRationale(),
        hasRequested = hasRequested,
    )

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/** False when there is no hosting activity to ask, which only makes a denial look permanent. */
private fun Context.shouldShowLocationRationale(): Boolean {
    val activity = findActivity() ?: return false
    return LOCATION_PERMISSIONS.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
}
