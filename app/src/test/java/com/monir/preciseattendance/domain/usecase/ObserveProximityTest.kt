package com.monir.preciseattendance.domain.usecase

import app.cash.turbine.test
import com.monir.preciseattendance.domain.geo.DistanceCalculator
import com.monir.preciseattendance.domain.geo.ProximityEvaluator
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.LocationException
import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.fakes.FakeLocationRepository
import com.monir.preciseattendance.fakes.FakeOfficeLocationRepository
import com.monir.preciseattendance.fakes.testFix
import com.monir.preciseattendance.fakes.testOffice
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveProximityTest {

    private val location = FakeLocationRepository()
    private val offices = FakeOfficeLocationRepository()
    private var distanceMeters = 12.0
    private val observeProximity = ObserveProximity(
        locationRepository = location,
        officeLocationRepository = offices,
        proximityEvaluator = ProximityEvaluator(DistanceCalculator { _, _ -> distanceMeters }),
    )

    @Test
    fun noOffice_isNoOfficeSet_andTheGpsStaysOff() = runTest {
        observeProximity().test {
            assertEquals(ProximityResult.NoOfficeSet, awaitItem())
            assertEquals(0, location.fixes.subscriptionCount.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun officeWithoutAFix_isAcquiringFix() = runTest {
        offices.office.value = testOffice()

        observeProximity().test {
            assertEquals(ProximityResult.AcquiringFix, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fixes_areClassifiedAsTheyArrive() = runTest {
        offices.office.value = testOffice()

        observeProximity().test {
            assertEquals(ProximityResult.AcquiringFix, awaitItem())

            distanceMeters = 12.0
            location.emitFix(testFix(accuracyMeters = 5.0))
            assertEquals(ProximityResult.InRange(12.0), awaitItem())

            distanceMeters = 120.0
            location.emitFix(testFix(accuracyMeters = 5.0))
            assertEquals(ProximityResult.OutOfRange(120.0), awaitItem())

            location.emitFix(testFix(accuracyMeters = 80.0))
            assertEquals(ProximityResult.WeakSignal(80.0), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun settingTheOffice_startsMeasuring() = runTest {
        observeProximity().test {
            assertEquals(ProximityResult.NoOfficeSet, awaitItem())

            offices.office.value = testOffice()
            assertEquals(ProximityResult.AcquiringFix, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun locationFailure_isReportedAsLocationUnavailable() = runTest {
        offices.office.value = testOffice()
        location.updates = flow { throw LocationException(LocationError.PermissionDenied) }

        observeProximity().test {
            assertEquals(ProximityResult.AcquiringFix, awaitItem())
            assertEquals(ProximityResult.LocationUnavailable(LocationError.PermissionDenied), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unexpectedFailure_isNotSwallowed() = runTest {
        offices.office.value = testOffice()
        location.updates = flow { throw IllegalStateException("boom") }

        observeProximity().test {
            assertEquals(ProximityResult.AcquiringFix, awaitItem())
            assertTrue(awaitError() is IllegalStateException)
        }
    }
}
