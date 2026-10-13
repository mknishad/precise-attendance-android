package com.monir.preciseattendance.data.location

import android.location.Location
import android.os.Looper
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.monir.preciseattendance.domain.model.FixResult
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.LocationException
import com.monir.preciseattendance.domain.model.LocationFix
import com.monir.preciseattendance.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class FusedLocationRepository @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val servicesObserver: LocationServicesObserver,
) : LocationRepository {

    /** Latest fix wins: if the collector is slow, older fixes are dropped rather than queued. */
    override fun locationUpdates(): Flow<LocationFix> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.mapNotNull(Location::toLocationFix).forEach { trySend(it) }
            }
        }
        startUpdates(callback)
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }.conflate()

    override fun isLocationEnabled(): Flow<Boolean> = servicesObserver.observe()

    override suspend fun getCurrentLocation(): FixResult {
        if (!servicesObserver.isEnabled()) return FixResult.Failure(LocationError.ServicesDisabled)
        val attempt = withTimeoutOrNull(CURRENT_FIX_TIMEOUT_MILLIS) { requestCurrentFix() }
            ?: return FixResult.Failure(LocationError.Timeout)
        return attempt.toFixResult()
    }

    /** Closes the flow with a typed error if the updates cannot be started. */
    private fun ProducerScope<LocationFix>.startUpdates(callback: LocationCallback) {
        try {
            fusedLocationClient
                .requestLocationUpdates(updateRequest(), callback, Looper.getMainLooper())
                .addOnFailureListener { close(LocationException(it.toLocationError())) }
        } catch (e: SecurityException) {
            close(LocationException(LocationError.PermissionDenied))
        }
    }

    private suspend fun requestCurrentFix(): FixAttempt = suspendCancellableCoroutine { continuation ->
        val cancellation = CancellationTokenSource()
        continuation.invokeOnCancellation { cancellation.cancel() }
        try {
            fusedLocationClient.getCurrentLocation(currentFixRequest(), cancellation.token)
                .addOnSuccessListener { location -> continuation.resume(FixAttempt.Completed(location)) }
                .addOnFailureListener { error -> continuation.resume(FixAttempt.Failed(error.toLocationError())) }
                .addOnCanceledListener { continuation.resume(FixAttempt.Failed(LocationError.Unavailable)) }
        } catch (e: SecurityException) {
            continuation.resume(FixAttempt.Failed(LocationError.PermissionDenied))
        }
    }

    private fun updateRequest(): LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MILLIS)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MILLIS)
            .build()

    /**
     * Max update age of zero rejects cached positions, so the fix is genuinely fresh. The timeout
     * is enforced by [getCurrentLocation], which cancels this request when it expires.
     */
    private fun currentFixRequest(): CurrentLocationRequest =
        CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()

    private fun Exception.toLocationError(): LocationError =
        if (this is SecurityException) LocationError.PermissionDenied else LocationError.ProviderFailure

    private sealed interface FixAttempt {
        /** [location] is null when the provider finished without a position. */
        data class Completed(val location: Location?) : FixAttempt
        data class Failed(val error: LocationError) : FixAttempt

        fun toFixResult(): FixResult = when (this) {
            is Failed -> FixResult.Failure(error)
            is Completed -> location?.toLocationFix()
                ?.let { FixResult.Success(it) }
                ?: FixResult.Failure(LocationError.Unavailable)
        }
    }

    private companion object {
        const val UPDATE_INTERVAL_MILLIS = 2_000L
        const val FASTEST_UPDATE_INTERVAL_MILLIS = 1_000L
        const val CURRENT_FIX_TIMEOUT_MILLIS = 15_000L
    }
}
