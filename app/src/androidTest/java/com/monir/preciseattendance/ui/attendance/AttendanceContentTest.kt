package com.monir.preciseattendance.ui.attendance

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.monir.preciseattendance.ui.theme.AttendanceTheme
import org.junit.Rule
import org.junit.Test

class AttendanceContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun markAttendance_isDisabledInDefaultState() {
        composeRule.setContent {
            AttendanceTheme {
                AttendanceContent(state = AttendanceUiState(), actions = AttendanceActions())
            }
        }

        composeRule.onNodeWithText("Mark Attendance").assertIsNotEnabled()
    }
}
