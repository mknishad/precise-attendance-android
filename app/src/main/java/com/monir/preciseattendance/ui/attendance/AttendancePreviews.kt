package com.monir.preciseattendance.ui.attendance

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.monir.preciseattendance.domain.model.GeoPoint
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.ui.theme.AttendanceTheme

private const val PREVIEW_HEIGHT_DP = 920

private val previewOffice = OfficeLocation(GeoPoint(40.7128, -74.0060), savedAtMillis = 0L)

/** Everything working, in the window, out of range at 120 m; each preview changes what it shows. */
private val baseState = AttendanceUiState(
    permissionState = LocationPermissionState.Granted,
    isLocationEnabled = true,
    officeLocation = previewOffice,
    proximity = ProximityResult.OutOfRange(120.0),
    isWithinWindow = true,
)

@Composable
private fun PreviewScreen(state: AttendanceUiState, darkTheme: Boolean = false) {
    AttendanceTheme(darkTheme = darkTheme) {
        AttendanceContent(state = state, actions = AttendanceActions())
    }
}

@Preview(name = "Out of range", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun OutOfRangePreview() = PreviewScreen(baseState)

@Preview(name = "In range", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun InRangePreview() = PreviewScreen(baseState.copy(proximity = ProximityResult.InRange(12.0)))

@Preview(name = "Weak signal", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun WeakSignalPreview() = PreviewScreen(baseState.copy(proximity = ProximityResult.WeakSignal(48.0)))

@Preview(name = "Acquiring", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun AcquiringPreview() = PreviewScreen(baseState.copy(proximity = ProximityResult.AcquiringFix))

@Preview(name = "No office set", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun NoOfficePreview() =
    PreviewScreen(baseState.copy(officeLocation = null, proximity = ProximityResult.NoOfficeSet))

@Preview(name = "Permission denied", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun PermissionDeniedPreview() = PreviewScreen(
    baseState.copy(permissionState = LocationPermissionState.Denied, proximity = ProximityResult.AcquiringFix),
)

@Preview(name = "Permanently denied", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun PermanentlyDeniedPreview() =
    PreviewScreen(baseState.copy(permissionState = LocationPermissionState.PermanentlyDenied))

@Preview(name = "Approximate only", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun ApproximateOnlyPreview() =
    PreviewScreen(baseState.copy(permissionState = LocationPermissionState.ApproximateOnly))

@Preview(name = "GPS off", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun GpsOffPreview() = PreviewScreen(
    baseState.copy(
        isLocationEnabled = false,
        proximity = ProximityResult.LocationUnavailable(LocationError.ServicesDisabled),
    ),
)

@Preview(name = "Already marked", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun AlreadyMarkedPreview() = PreviewScreen(
    baseState.copy(
        proximity = ProximityResult.InRange(8.0),
        alreadyMarkedToday = true,
        markedAtMillis = 1_760_484_600_000L,
    ),
)

@Preview(name = "Setting office", showBackground = true, heightDp = PREVIEW_HEIGHT_DP)
@Composable
private fun SettingOfficePreview() = PreviewScreen(baseState.copy(isSettingOffice = true))

@Preview(name = "Large font (200%)", showBackground = true, fontScale = 2f, heightDp = 1400)
@Composable
private fun LargeFontPreview() = PreviewScreen(baseState)

@Preview(
    name = "Dark, in range",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = PREVIEW_HEIGHT_DP,
)
@Composable
private fun DarkInRangePreview() =
    PreviewScreen(baseState.copy(proximity = ProximityResult.InRange(12.0)), darkTheme = true)

@Preview(
    name = "Dark, out of range",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = PREVIEW_HEIGHT_DP,
)
@Composable
private fun DarkOutOfRangePreview() = PreviewScreen(baseState, darkTheme = true)
