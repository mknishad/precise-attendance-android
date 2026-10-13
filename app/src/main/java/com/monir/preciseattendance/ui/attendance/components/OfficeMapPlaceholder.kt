package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

private val GRID_SPACING = 28.dp

/** Stands in for the map when there is no API key: a soft gradient, a faint grid and a pin. */
@Composable
fun OfficeMapPlaceholder(modifier: Modifier = Modifier) {
    val gradient = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primaryContainer),
    )
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Box(modifier.background(gradient)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = GRID_SPACING.toPx()
            var x = step
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                x += step
            }
            var y = step
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                y += step
            }
        }
        Icon(
            imageVector = AttendanceIcons.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = PIN_OFFSET)
                .size(40.dp),
        )
    }
}

/** Pushes the pin below the coordinate pill that floats over the middle of the map. */
internal val PIN_OFFSET = 56.dp
