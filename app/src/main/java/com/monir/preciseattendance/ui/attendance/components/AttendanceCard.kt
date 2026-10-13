package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** The white, rounded, softly raised card used for every block on the screen. */
@Composable
fun AttendanceCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        content()
    }
}

/** A round tinted badge holding a decorative icon; the surrounding text carries the meaning. */
@Composable
fun IconBadge(
    icon: ImageVector,
    container: Color,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.size(56.dp), shape = CircleShape, color = container) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        }
    }
}
