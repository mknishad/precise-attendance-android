package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.monir.preciseattendance.R
import com.monir.preciseattendance.domain.model.OfficeLocation
import com.monir.preciseattendance.ui.attendance.formatCoordinate
import com.monir.preciseattendance.ui.theme.CoordinateTextStyle

private val MAP_HEIGHT = 160.dp
private val PILL_LIFT = (-28).dp

/**
 * The map area with the coordinate pill floating over it. [isMapAvailable] decides between the
 * real map and the placeholder; the pill is the same either way.
 */
@Composable
fun OfficeMap(
    office: OfficeLocation?,
    isMapAvailable: Boolean,
    modifier: Modifier = Modifier,
) {
    val mapDescription = stringResource(R.string.map_description)
    val mapModifier = Modifier
        .fillMaxWidth()
        .height(MAP_HEIGHT)
        .semantics { contentDescription = mapDescription }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MAP_HEIGHT)
            .clip(MaterialTheme.shapes.medium),
    ) {
        if (isMapAvailable) OfficeGoogleMap(office, mapModifier) else OfficeMapPlaceholder(mapModifier)
        CoordinatePill(
            office = office,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = PILL_LIFT)
                .padding(horizontal = 12.dp),
        )
    }
}

@Composable
private fun CoordinatePill(office: OfficeLocation?, modifier: Modifier = Modifier) {
    val latitude = office?.point?.latitude?.let(::formatCoordinate) ?: stringResource(R.string.coordinate_unset)
    val longitude = office?.point?.longitude?.let(::formatCoordinate) ?: stringResource(R.string.coordinate_unset)
    val description = if (office != null) {
        stringResource(R.string.office_coordinates_description, latitude, longitude)
    } else {
        stringResource(R.string.office_not_set_description)
    }
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = AttendanceIcons.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.office_coordinates, latitude, longitude),
                style = CoordinateTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}
