package com.monir.preciseattendance.ui.attendance

enum class LocationPermissionState {
    /** The user has not been asked yet. */
    NotRequested,

    /** Precise location is granted. */
    Granted,

    /** Only approximate location is granted, which is not accurate enough for a 50 m check. */
    ApproximateOnly,

    /** Denied, but the system will still show the permission dialog again. */
    Denied,

    /** Denied and the system will no longer show the dialog; only app settings can change it. */
    PermanentlyDenied,
}

/**
 * Works out the permission state from what the platform reports. `shouldShowRationale` is false
 * both before the first request and after a permanent denial, so [hasRequested] is what tells
 * those two apart.
 */
fun resolveLocationPermission(
    fineGranted: Boolean,
    coarseGranted: Boolean,
    shouldShowRationale: Boolean,
    hasRequested: Boolean,
): LocationPermissionState = when {
    fineGranted -> LocationPermissionState.Granted
    coarseGranted -> LocationPermissionState.ApproximateOnly
    shouldShowRationale -> LocationPermissionState.Denied
    hasRequested -> LocationPermissionState.PermanentlyDenied
    else -> LocationPermissionState.NotRequested
}
