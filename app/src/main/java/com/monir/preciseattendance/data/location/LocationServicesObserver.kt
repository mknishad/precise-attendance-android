package com.monir.preciseattendance.data.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

/** Reports whether device location services are on, following changes from the system. */
class LocationServicesObserver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val locationManager: LocationManager? = context.getSystemService(LocationManager::class.java)

    /** False when the platform has no location manager at all. */
    fun isEnabled(): Boolean = locationManager?.let(LocationManagerCompat::isLocationEnabled) ?: false

    fun observe(): Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(isEnabled())
            }
        }
        // Register before reading the initial state so a change in between is not missed.
        ContextCompat.registerReceiver(context, receiver, changeFilter(), ContextCompat.RECEIVER_NOT_EXPORTED)
        trySend(isEnabled())
        awaitClose { context.unregisterReceiver(receiver) }
    }.conflate().distinctUntilChanged()

    private fun changeFilter() = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION).apply {
        // MODE_CHANGED_ACTION is the master location switch, only public from API 28.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) addAction(LocationManager.MODE_CHANGED_ACTION)
    }
}
