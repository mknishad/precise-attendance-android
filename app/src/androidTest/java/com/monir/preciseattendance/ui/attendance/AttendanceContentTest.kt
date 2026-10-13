package com.monir.preciseattendance.ui.attendance

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.monir.preciseattendance.R
import com.monir.preciseattendance.domain.model.AttendanceWindow
import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.ui.attendance.components.AttendanceTestTags
import com.monir.preciseattendance.ui.theme.AttendanceTheme
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AttendanceContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(id: Int, vararg args: Any) = context.getString(id, *args)

    private val ready = AttendanceUiState(
        permissionState = LocationPermissionState.Granted,
        isLocationEnabled = true,
        officeLocation = OfficeLocation(GeoPoint(40.7128, -74.0060), savedAtMillis = 0L),
        proximity = ProximityResult.InRange(12.0),
        isWithinWindow = true,
    )

    /** The state on screen. Tests that check several states change it instead of re-composing. */
    private val shown = mutableStateOf(ready)

    private fun show(state: AttendanceUiState, actions: AttendanceActions = AttendanceActions()) {
        shown.value = state
        composeRule.setContent { AttendanceTheme { AttendanceContent(state = shown.value, actions = actions) } }
    }

    private fun change(state: AttendanceUiState) {
        shown.value = state
        composeRule.waitForIdle()
    }

    private fun markButton() = composeRule.onNodeWithTag(AttendanceTestTags.MARK_ATTENDANCE_BUTTON)
    private fun setOfficeButton() = composeRule.onNodeWithTag(AttendanceTestTags.SET_OFFICE_BUTTON)

    // Mark Attendance

    @Test
    fun markAttendance_isDisabledWhenOutOfRange() {
        show(ready.copy(proximity = ProximityResult.OutOfRange(120.0)))

        markButton().assertIsNotEnabled()
    }

    @Test
    fun markAttendance_isEnabledInRange_andCallsBack() {
        var marked = 0
        show(ready, AttendanceActions(onMarkAttendance = { marked++ }))

        markButton().performScrollTo().assertIsEnabled().performClick()

        assertEquals(1, marked)
    }

    @Test
    fun markAttendance_isDisabledInEveryOtherSituation() {
        val disabled = listOf(
            ready.copy(proximity = ProximityResult.OutOfRange(51.0)),
            ready.copy(proximity = ProximityResult.WeakSignal(45.0)),
            ready.copy(proximity = ProximityResult.AcquiringFix),
            ready.copy(proximity = ProximityResult.NoOfficeSet),
            ready.copy(isWithinWindow = false),
            ready.copy(alreadyMarkedToday = true, markedAtMillis = 1_760_484_600_000L),
        )
        show(ready)
        markButton().assertIsEnabled()
        for (state in disabled) {
            change(state)
            markButton().assertIsNotEnabled()
        }
    }

    @Test
    fun markAttendance_isDisabledWhileMarking() {
        show(ready.copy(isMarkingAttendance = true))

        markButton().assertIsNotEnabled()
    }

    // Status chip

    @Test
    fun statusChip_showsTheRightLabelForEachState() {
        val cases = listOf(
            ProximityResult.OutOfRange(120.0) to R.string.chip_out_of_range,
            ProximityResult.InRange(12.0) to R.string.chip_in_range,
            ProximityResult.WeakSignal(45.0) to R.string.chip_weak_signal,
            ProximityResult.AcquiringFix to R.string.chip_acquiring,
            ProximityResult.NoOfficeSet to R.string.chip_no_office,
            ProximityResult.LocationUnavailable(LocationError.ProviderFailure) to R.string.chip_location_unavailable,
        )
        show(ready)
        for ((proximity, label) in cases) {
            change(ready.copy(proximity = proximity))
            composeRule.onNodeWithText(string(label)).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun outOfRange_showsTheDistanceTheSentenceAndTheHelper() {
        show(ready.copy(proximity = ProximityResult.OutOfRange(120.0)))

        composeRule.onNodeWithText(string(R.string.status_out_of_range, 120), useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(context.resources.getQuantityString(R.plurals.helper_out_of_range, 50, 50)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.ring_distance_value, 120), useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun ring_announcesTheDistanceAndTheRangeStatus() {
        show(ready.copy(proximity = ProximityResult.OutOfRange(120.0)))

        val sentence = string(R.string.status_out_of_range, 120)
        val expected = string(R.string.ring_description, sentence, string(R.string.chip_out_of_range))
        composeRule.onNodeWithContentDescription(expected).assertIsDisplayed()
    }

    // Permission and location cards

    @Test
    fun permissionNotRequested_showsTheGrantButton_andNoRangeOrCheckIn() {
        var requested = 0
        show(ready.copy(permissionState = LocationPermissionState.NotRequested), AttendanceActions(onRequestPermission = { requested++ }))

        composeRule.onNodeWithText(string(R.string.action_grant_location_access)).performScrollTo().performClick()

        assertEquals(1, requested)
        markButton().assertDoesNotExist()
    }

    @Test
    fun permissionDenied_offersToAskAgain() {
        var requested = 0
        show(ready.copy(permissionState = LocationPermissionState.Denied), AttendanceActions(onRequestPermission = { requested++ }))

        composeRule.onNodeWithText(string(R.string.action_grant_location_access)).performScrollTo().performClick()

        assertEquals(1, requested)
    }

    @Test
    fun permissionPermanentlyDenied_offersAppSettings_notAnotherRequest() {
        var settings = 0
        show(ready.copy(permissionState = LocationPermissionState.PermanentlyDenied), AttendanceActions(onOpenSettings = { settings++ }))

        composeRule.onNodeWithText(string(R.string.action_grant_location_access)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.action_open_app_settings)).performScrollTo().performClick()

        assertEquals(1, settings)
    }

    @Test
    fun approximateOnly_explainsAndOffersBothRequestAndSettings() {
        var requested = 0
        var settings = 0
        show(
            ready.copy(permissionState = LocationPermissionState.ApproximateOnly),
            AttendanceActions(onRequestPermission = { requested++ }, onOpenSettings = { settings++ }),
        )

        composeRule.onNodeWithText(string(R.string.permission_title_precise)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_allow_precise_location)).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.action_open_app_settings)).performScrollTo().performClick()

        assertEquals(1, requested)
        assertEquals(1, settings)
    }

    @Test
    fun gpsOff_showsTheEnableButton_andNoCheckIn() {
        var enabled = 0
        show(ready.copy(isLocationEnabled = false), AttendanceActions(onEnableLocation = { enabled++ }))

        composeRule.onNodeWithText(string(R.string.location_off_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_enable_location)).performScrollTo().performClick()

        assertEquals(1, enabled)
        markButton().assertDoesNotExist()
    }

    // Set Office Location

    @Test
    fun setOfficeLocation_isEnabled_andCallsBack() {
        var set = 0
        show(ready, AttendanceActions(onSetOfficeLocation = { set++ }))

        setOfficeButton().assertIsEnabled().performClick()

        assertEquals(1, set)
    }

    @Test
    fun setOfficeLocation_isDisabledWhileLoading() {
        show(ready.copy(isSettingOffice = true))

        setOfficeButton().assertIsNotEnabled()
    }

    // Office context and check-in card

    @Test
    fun noOffice_showsPlaceholderCoordinates_andAnOfficeShowsFourDecimals() {
        show(ready.copy(officeLocation = null, proximity = ProximityResult.NoOfficeSet))
        composeRule.onNode(hasText(string(R.string.office_coordinates, "--", "--")), useUnmergedTree = true).assertIsDisplayed()

        change(ready)
        composeRule.onNode(hasText(string(R.string.office_coordinates, "40.7128", "-74.0060")), useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun checkInCard_showsTheWindowCaption() {
        show(ready)

        val locale = context.resources.configuration.locales[0]
        val window = AttendanceWindow()
        val caption = string(
            R.string.check_in_window_caption,
            formatLocalTime(window.start, locale),
            formatLocalTime(window.end, locale),
        ).uppercase(locale)
        composeRule.onNodeWithText(caption).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun marked_showsWhenItWasMarked_andKeepsTheButtonDisabled() {
        val markedAt = 1_760_484_600_000L
        show(ready.copy(alreadyMarkedToday = true, markedAtMillis = markedAt))

        val locale = context.resources.configuration.locales[0]
        val time = formatEpochMillis(markedAt, ZoneId.systemDefault(), locale)
        composeRule.onNodeWithText(string(R.string.check_in_marked_at, time)).performScrollTo().assertIsDisplayed()
        markButton().assertIsNotEnabled()
    }

    @Test
    fun backArrow_callsBack() {
        var back = 0
        show(ready, AttendanceActions(onBack = { back++ }))

        composeRule.onNodeWithContentDescription(string(R.string.back_description)).performClick()

        assertEquals(1, back)
    }

    @Test
    fun largeFont_stillReachesTheCheckInButton() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                AttendanceTheme { AttendanceContent(ready, AttendanceActions()) }
            }
        }

        markButton().performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.chip_in_range)).performScrollTo().assertIsDisplayed()
    }
}

