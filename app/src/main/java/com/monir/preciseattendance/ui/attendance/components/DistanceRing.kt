package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monir.preciseattendance.R
import com.monir.preciseattendance.ui.attendance.RangeIndicator

private val RING_SIZE = 176.dp
private val RING_STROKE = 14.dp
private const val MAX_RING_SCALE = 1.5f
private const val RING_FILL_ALPHA = 0.5f
private const val ARC_ANIMATION_MILLIS = 700

/**
 * The distance as a ring that fills as the user gets closer. [progress] is the fraction from the
 * UI state (1 at the office, 0 at the far limit), animated here. Acquiring a fix shows a spinning
 * ring instead. The ring grows with the font scale so large text still fits inside it.
 */
@Composable
fun DistanceRing(
    indicator: RangeIndicator,
    progress: Float,
    distanceMeters: Int?,
    description: String,
    modifier: Modifier = Modifier,
) {
    val tone = indicator.tone.colors()
    val size = RING_SIZE * LocalDensity.current.fontScale.coerceIn(1f, MAX_RING_SCALE)
    Box(
        modifier = modifier.size(size).clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(RING_STROKE)
                .background(tone.container.copy(alpha = RING_FILL_ALPHA), CircleShape),
        )
        RingArc(indicator, progress, tone)
        RingText(indicator, distanceMeters, Modifier.padding(RING_STROKE * 1.6f))
    }
}

@Composable
private fun RingArc(indicator: RangeIndicator, progress: Float, tone: ToneColors) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val arc = Modifier.fillMaxSize()
    if (indicator == RangeIndicator.Acquiring) {
        CircularProgressIndicator(
            modifier = arc,
            color = tone.accent,
            trackColor = track,
            strokeWidth = RING_STROKE,
            strokeCap = StrokeCap.Round,
        )
    } else {
        val animated by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(ARC_ANIMATION_MILLIS, easing = FastOutSlowInEasing),
            label = "distanceRingProgress",
        )
        CircularProgressIndicator(
            progress = { animated },
            modifier = arc,
            color = tone.accent,
            trackColor = track,
            strokeWidth = RING_STROKE,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
        )
    }
}

@Composable
private fun RingText(indicator: RangeIndicator, distanceMeters: Int?, modifier: Modifier = Modifier) {
    val value = distanceMeters?.let { stringResource(R.string.ring_distance_value, it) }
        ?: stringResource(R.string.ring_value_unknown)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
        Text(
            text = stringResource(indicator.ringLabel),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
