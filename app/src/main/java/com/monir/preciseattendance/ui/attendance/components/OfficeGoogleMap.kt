package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.monir.preciseattendance.domain.model.AttendancePolicy
import com.monir.preciseattendance.domain.model.OfficeLocation

private const val OFFICE_ZOOM = 18f
private const val NEUTRAL_ZOOM = 1f
private val NEUTRAL_CENTRE = LatLng(0.0, 0.0)
private const val RADIUS_FILL_ALPHA = 0.18f

/**
 * A non-interactive map centred on the office, with the allowed radius drawn around it. All
 * gestures and controls are off so the page can still be scrolled with a finger on the map. With
 * no office it shows a neutral world view and no marker.
 */
@Composable
fun OfficeGoogleMap(office: OfficeLocation?, modifier: Modifier = Modifier) {
    val target = office?.let { LatLng(it.point.latitude, it.point.longitude) }
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(target ?: NEUTRAL_CENTRE, if (target != null) OFFICE_ZOOM else NEUTRAL_ZOOM)
    }
    FollowOffice(cameraState, target)

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        uiSettings = noControls,
        // Shifts the visual centre down so the marker sits below the coordinate pill.
        contentPadding = PaddingValues(top = PIN_OFFSET),
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
    ) {
        if (target != null) OfficeOverlays(target)
    }
}

@Composable
private fun FollowOffice(cameraState: CameraPositionState, target: LatLng?) {
    LaunchedEffect(target) {
        if (target != null) cameraState.animate(CameraUpdateFactory.newLatLngZoom(target, OFFICE_ZOOM))
    }
}

@Composable
private fun OfficeOverlays(target: LatLng) {
    val accent = MaterialTheme.colorScheme.primary
    Marker(state = rememberUpdatedMarkerState(position = target))
    Circle(
        center = target,
        radius = AttendancePolicy.ALLOWED_RADIUS_METERS,
        fillColor = accent.copy(alpha = RADIUS_FILL_ALPHA),
        strokeColor = accent,
        strokeWidth = 4f,
    )
}

private val noControls = MapUiSettings(
    compassEnabled = false,
    indoorLevelPickerEnabled = false,
    mapToolbarEnabled = false,
    myLocationButtonEnabled = false,
    rotationGesturesEnabled = false,
    scrollGesturesEnabled = false,
    scrollGesturesEnabledDuringRotateOrZoom = false,
    tiltGesturesEnabled = false,
    zoomControlsEnabled = false,
    zoomGesturesEnabled = false,
)
