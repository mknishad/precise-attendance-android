package com.monir.preciseattendance.domain.model

/** Why a location could not be obtained. Mapped to user-facing messages in the UI layer. */
sealed interface LocationError {
    data object PermissionDenied : LocationError
    data object ServicesDisabled : LocationError
    data object Timeout : LocationError

    /** The provider completed without a position (for example no recent signal). */
    data object Unavailable : LocationError

    /** The provider reported a failure of its own. */
    data object ProviderFailure : LocationError
}

/** Carries a [LocationError] through a Flow that has to terminate because of it. */
class LocationException(val error: LocationError) : Exception("Location unavailable: $error")
