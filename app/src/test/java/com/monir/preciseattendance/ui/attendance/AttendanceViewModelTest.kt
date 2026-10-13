package com.monir.preciseattendance.ui.attendance

import app.cash.turbine.test
import com.monir.preciseattendance.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AttendanceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = AttendanceViewModel(
        observeDistanceToOffice = mockk(),
        setOfficeLocation = mockk(),
        markAttendance = mockk(),
    )

    @Test
    fun initialState_isAcquiringFix() = runTest {
        viewModel.state.test {
            assertEquals(LocationStatus.AcquiringFix, awaitItem().locationStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
