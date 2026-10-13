package com.monir.preciseattendance.domain.model

/** Outcome of a one-shot location request. */
sealed interface FixResult {
    data class Success(val fix: LocationFix) : FixResult
    data class Failure(val error: LocationError) : FixResult
}
