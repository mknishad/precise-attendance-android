package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.monir.preciseattendance.BuildConfig

/**
 * True only when a Maps API key was supplied at build time (see MAPS_KEY_AVAILABLE) and Google
 * Play services can run on this device. Otherwise the screen shows the placeholder.
 */
@Composable
fun rememberMapAvailable(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        BuildConfig.MAPS_KEY_AVAILABLE &&
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }
}
