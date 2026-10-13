@file:OptIn(ExperimentalCoroutinesApi::class)

package com.monir.preciseattendance.ui.attendance

import app.cash.turbine.test
import com.monir.preciseattendance.MainDispatcherRule
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.usecase.SetOfficeLocationResult
import com.monir.preciseattendance.fakes.testOffice
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AttendanceViewModelSetOfficeTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixture = AttendanceViewModelFixture()

    private fun message(message: StatusMessage) = AttendanceUiEvent.ShowMessage(message)

    private fun returning(result: SetOfficeLocationResult) {
        coEvery { fixture.setOfficeLocation() } returns result
    }

    private suspend fun assertTapProduces(expected: AttendanceUiEvent) {
        val viewModel = fixture.createViewModel()
        viewModel.events.test {
            viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked)
            assertEventEquals(expected, awaitItem())
            expectNoEvents()
        }
    }

    private fun assertEventEquals(expected: AttendanceUiEvent, actual: AttendanceUiEvent) =
        assertEquals(expected, actual)

    @Test
    fun success_reportsOfficeSaved() = runTest {
        returning(SetOfficeLocationResult.Success(testOffice()))

        assertTapProduces(message(StatusMessage.OfficeSaved))
    }

    @Test
    fun weakAccuracy_reportsTheAccuracyRoundedUp() = runTest {
        returning(SetOfficeLocationResult.WeakSignal(45.2))

        assertTapProduces(message(StatusMessage.OfficeWeakAccuracy(46)))
    }

    @Test
    fun weakAccuracyWithNoUsableNumber_reportsWithoutOne() = runTest {
        returning(SetOfficeLocationResult.WeakSignal(Double.NaN))

        assertTapProduces(message(StatusMessage.OfficeWeakAccuracy(null)))
    }

    @Test
    fun timeout_reportsTimedOut() = runTest {
        returning(SetOfficeLocationResult.LocationUnavailable(LocationError.Timeout))

        assertTapProduces(message(StatusMessage.LocationTimedOut))
    }

    @Test
    fun permissionRevokedBehindOurBack_reportsPermissionNeeded() = runTest {
        returning(SetOfficeLocationResult.LocationUnavailable(LocationError.PermissionDenied))

        assertTapProduces(message(StatusMessage.PermissionNeeded))
    }

    @Test
    fun unknownLocationFailures_reportALocationFailure() = runTest {
        for (error in listOf(LocationError.Unavailable, LocationError.ProviderFailure)) {
            returning(SetOfficeLocationResult.LocationUnavailable(error))

            assertTapProduces(message(StatusMessage.LocationRequestFailed))
        }
    }

    @Test
    fun storageFailure_reportsSaveFailed() = runTest {
        returning(SetOfficeLocationResult.StorageFailure)

        assertTapProduces(message(StatusMessage.OfficeSaveFailed))
    }

    @Test
    fun noPermission_explainsWhy_andNeverCallsTheUseCase() = runTest {
        val expected = mapOf(
            LocationPermissionState.NotRequested to StatusMessage.PermissionNeeded,
            LocationPermissionState.Denied to StatusMessage.PermissionDenied,
            LocationPermissionState.PermanentlyDenied to StatusMessage.PermissionPermanentlyDenied,
            LocationPermissionState.ApproximateOnly to StatusMessage.ApproximateLocationOnly,
        )
        for ((permission, expectedMessage) in expected) {
            val viewModel = fixture.createViewModel(permission = permission)
            viewModel.events.test {
                viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked)
                assertEventEquals(message(expectedMessage), awaitItem())
                expectNoEvents()
            }
        }
        coVerify(exactly = 0) { fixture.setOfficeLocation() }
    }

    @Test
    fun locationOff_requestsSettingsResolutionOnce_andNeverCallsTheUseCase() = runTest {
        fixture.location.locationEnabled.value = false

        assertTapProduces(AttendanceUiEvent.RequestLocationSettingsResolution)

        coVerify(exactly = 0) { fixture.setOfficeLocation() }
    }

    @Test
    fun locationTurnedOffDuringTheRequest_requestsSettingsResolutionOnce() = runTest {
        returning(SetOfficeLocationResult.LocationUnavailable(LocationError.ServicesDisabled))

        assertTapProduces(AttendanceUiEvent.RequestLocationSettingsResolution)
    }

    @Test
    fun locationOff_eachTapIsOneUserActionAndOneRequest() = runTest {
        fixture.location.locationEnabled.value = false
        val viewModel = fixture.createViewModel()

        viewModel.events.test {
            viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked)
            assertEventEquals(AttendanceUiEvent.RequestLocationSettingsResolution, awaitItem())
            expectNoEvents()

            viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked)
            assertEventEquals(AttendanceUiEvent.RequestLocationSettingsResolution, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun whileSaving_showsLoading_andAnExtraTapIsIgnored() = runTest {
        val gate = CompletableDeferred<SetOfficeLocationResult>()
        coEvery { fixture.setOfficeLocation() } coAnswers { gate.await() }
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)
        assertFalse(viewModel.state.value.isSettingOffice)

        viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked)
        viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked)
        runCurrent()
        assertTrue(viewModel.state.value.isSettingOffice)

        viewModel.events.test {
            gate.complete(SetOfficeLocationResult.Success(testOffice()))
            assertEventEquals(message(StatusMessage.OfficeSaved), awaitItem())
            expectNoEvents()
        }
        runCurrent()
        assertFalse(viewModel.state.value.isSettingOffice)
        coVerify(exactly = 1) { fixture.setOfficeLocation() }
    }

    @Test
    fun afterAFailure_loadingClears_andTheUserCanTryAgain() = runTest {
        returning(SetOfficeLocationResult.LocationUnavailable(LocationError.Timeout))
        val viewModel = fixture.createViewModel()
        keepStateActive(viewModel)

        viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked)
        runCurrent()
        assertFalse(viewModel.state.value.isSettingOffice)

        viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked)
        runCurrent()
        coVerify(exactly = 2) { fixture.setOfficeLocation() }
    }
}
