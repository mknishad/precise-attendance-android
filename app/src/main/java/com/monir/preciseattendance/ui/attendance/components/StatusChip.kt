package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.monir.preciseattendance.ui.attendance.RangeIndicator

/** A pill with a coloured dot and the range status. Announced politely when it changes. */
@Composable
fun StatusChip(indicator: RangeIndicator, modifier: Modifier = Modifier) {
    val tone = indicator.tone.colors()
    val container by animateColorAsState(tone.container, label = "chipContainer")
    val content by animateColorAsState(tone.onContainer, label = "chipContent")
    val dot by animateColorAsState(tone.accent, label = "chipDot")
    Surface(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        shape = CircleShape,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).background(dot, CircleShape))
            Text(text = stringResource(indicator.chipLabel), style = MaterialTheme.typography.labelMedium)
        }
    }
}
