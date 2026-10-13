package com.monir.preciseattendance.domain.usecase

import com.monir.preciseattendance.domain.geo.ProximityEvaluator
import com.monir.preciseattendance.domain.model.LocationException
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.domain.repository.LocationRepository
import com.monir.preciseattendance.domain.repository.OfficeLocationRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class ObserveProximity @Inject constructor(
    private val locationRepository: LocationRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val proximityEvaluator: ProximityEvaluator,
) {
    /**
     * Live proximity to the saved office. Location updates are only requested while an office is
     * set, so the GPS stays off until there is something to measure against.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<ProximityResult> =
        officeLocationRepository.observeOfficeLocation().flatMapLatest { office ->
            if (office == null) flowOf(ProximityResult.NoOfficeSet) else proximityTo(office)
        }

    private fun proximityTo(office: OfficeLocation): Flow<ProximityResult> =
        locationRepository.locationUpdates()
            .map { fix -> proximityEvaluator.evaluate(office, fix) }
            .onStart { emit(ProximityResult.AcquiringFix) }
            .catch { cause ->
                if (cause is LocationException) {
                    emit(ProximityResult.LocationUnavailable(cause.error))
                } else {
                    throw cause
                }
            }
}
