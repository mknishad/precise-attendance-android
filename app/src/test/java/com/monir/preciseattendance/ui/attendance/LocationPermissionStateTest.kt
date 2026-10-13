package com.monir.preciseattendance.ui.attendance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationPermissionStateTest {

    private fun resolve(
        fine: Boolean = false,
        coarse: Boolean = false,
        rationale: Boolean = false,
        requested: Boolean = false,
    ) = resolveLocationPermission(fine, coarse, rationale, requested)

    @Test
    fun neverAsked_isNotRequested() {
        assertEquals(LocationPermissionState.NotRequested, resolve())
    }

    @Test
    fun preciseGranted_isGranted() {
        assertEquals(LocationPermissionState.Granted, resolve(fine = true, coarse = true, requested = true))
        assertEquals(LocationPermissionState.Granted, resolve(fine = true))
    }

    @Test
    fun approximateOnly_isItsOwnState_notGranted() {
        assertEquals(LocationPermissionState.ApproximateOnly, resolve(coarse = true, requested = true))
    }

    @Test
    fun approximateOnly_winsOverARationale() {
        assertEquals(LocationPermissionState.ApproximateOnly, resolve(coarse = true, rationale = true, requested = true))
    }

    @Test
    fun deniedOnce_withRationale_canAskAgain() {
        assertEquals(LocationPermissionState.Denied, resolve(rationale = true, requested = true))
    }

    @Test
    fun deniedAndNoRationaleAfterAsking_isPermanent() {
        assertEquals(LocationPermissionState.PermanentlyDenied, resolve(requested = true))
    }

    @Test
    fun noRationaleBeforeAsking_isNotMistakenForPermanent() {
        assertEquals(LocationPermissionState.NotRequested, resolve(requested = false))
    }

    @Test
    fun onlyGrantedHasNoMissingMessage() {
        assertNull(LocationPermissionState.Granted.missingMessage())
        for (state in LocationPermissionState.entries - LocationPermissionState.Granted) {
            assertEquals(state.name, true, state.missingMessage() != null)
        }
    }
}
