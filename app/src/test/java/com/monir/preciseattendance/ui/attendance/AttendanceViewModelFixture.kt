@file:OptIn(ExperimentalCoroutinesApi::class)

package com.monir.preciseattendance.ui.attendance

import com.monir.preciseattendance.domain.model.AttendanceWindow
import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.domain.usecase.MarkAttendance
import com.monir.preciseattendance.domain.usecase.ObserveProximity
import com.monir.preciseattendance.domain.usecase.SetOfficeLocation
import com.monir.preciseattendance.fakes.FakeAttendanceRepository
import com.monir.preciseattendance.fakes.FakeLocationRepository
import com.monir.preciseattendance.fakes.FakeOfficeLocationRepository
import com.monir.preciseattendance.fakes.MutableTestClock
import com.monir.preciseattendance.fakes.melbourneInstant
import com.monir.preciseattendance.fakes.testOffice
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent

/**
 * Starts from a state where every condition for marking attendance holds (09:30, office set,
 * in range, location on, not yet marked), so each test breaks exactly the one thing it is about.
 */
class AttendanceViewModelFixture {
    val clock = MutableTestClock(melbourneInstant("2026-10-15", "09:30:00"))
    val location = FakeLocationRepository()
    val offices = FakeOfficeLocationRepository(initial = testOffice())
    val attendance = FakeAttendanceRepository()
    val proximity = MutableStateFlow<ProximityResult>(ProximityResult.InRange(10.0))
    val setOfficeLocation = mockk<SetOfficeLocation>()
    val markAttendance = mockk<MarkAttendance>()

    /** How many collectors currently hold the proximity (location updates) flow open. */
    var proximityCollectors = 0
        private set

    private val observeProximity = mockk<ObserveProximity>().also { useCase ->
        every { useCase() } returns proximity
            .onStart { proximityCollectors++ }
            .onCompletion { proximityCollectors-- }
    }

    fun createViewModel(
        permission: LocationPermissionState? = LocationPermissionState.Granted,
        markAttendance: MarkAttendance = this.markAttendance,
    ) = AttendanceViewModel(
        observeProximity = observeProximity,
        setOfficeLocation = setOfficeLocation,
        markAttendance = markAttendance,
        locationRepository = location,
        officeLocationRepository = offices,
        attendanceRepository = attendance,
        window = AttendanceWindow(),
        clock = clock,
    ).also { viewModel -> permission?.let { viewModel.onIntent(AttendanceIntent.PermissionResult(it)) } }

    fun setTime(time: String, date: String = "2026-10-15") {
        clock.now = melbourneInstant(date, time)
    }
}

/** Keeps the state flow subscribed, as a started screen would, then lets pending work run. */
fun TestScope.keepStateActive(viewModel: AttendanceViewModel) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
    runCurrent()
}
