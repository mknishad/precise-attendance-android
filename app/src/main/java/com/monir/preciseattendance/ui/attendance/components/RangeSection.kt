package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monir.preciseattendance.R
import com.monir.preciseattendance.ui.attendance.AttendanceUiState
import com.monir.preciseattendance.ui.attendance.checkInMode
import com.monir.preciseattendance.ui.attendance.helperText
import com.monir.preciseattendance.ui.attendance.rangeIndicator
import com.monir.preciseattendance.ui.attendance.supportingText

/** The distance ring, status chip, helper text and check-in card, for when location works. */
@Composable
fun RangeSection(
    state: AttendanceUiState,
    onMarkAttendance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    val indicator = state.rangeIndicator()
    val supporting = state.supportingText(resources)
    val helper = state.helperText(resources)
    val chipLabel = stringResource(indicator.chipLabel)
    val ringDescription = if (supporting != null) {
        stringResource(R.string.ring_description, supporting, chipLabel)
    } else {
        chipLabel
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DistanceRing(
            indicator = indicator,
            progress = state.distanceProgress,
            distanceMeters = state.displayDistanceMeters,
            description = ringDescription,
        )
        StatusChip(indicator)
        RangeCaption(supporting = supporting, helper = helper)
        CheckInCard(
            mode = state.checkInMode(),
            window = state.attendanceWindow,
            markedAtMillis = state.markedAtMillis,
            isMarking = state.isMarkingAttendance,
            lockedReason = helper,
            onMarkAttendance = onMarkAttendance,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun RangeCaption(supporting: String?, helper: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (supporting != null) {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = helper,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}
