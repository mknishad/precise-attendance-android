package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.monir.preciseattendance.R
import com.monir.preciseattendance.domain.model.OfficeLocation

/** Step 1: show where the office is and let the user save the current position as the office. */
@Composable
fun OfficeStepCard(
    office: OfficeLocation?,
    isSettingOffice: Boolean,
    isMapAvailable: Boolean,
    onSetOfficeLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AttendanceCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            //StepHeader()
            OfficeMap(office = office, isMapAvailable = isMapAvailable)
            Text(
                text = stringResource(R.string.step_office_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SetOfficeButton(isSettingOffice = isSettingOffice, onClick = onSetOfficeLocation)
        }
    }
}

@Composable
private fun StepHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.step_office_context),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        // Decorative "current step" marker.
        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
    }
}

@Composable
private fun SetOfficeButton(isSettingOffice: Boolean, onClick: () -> Unit) {
    val accent = if (isSettingOffice) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary
    OutlinedButton(
        onClick = onClick,
        enabled = !isSettingOffice,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(width = 1.5.dp, color = accent),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag(AttendanceTestTags.SET_OFFICE_BUTTON),
    ) {
        if (isSettingOffice) {
            val savingDescription = stringResource(R.string.set_office_in_progress)
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp).semantics { contentDescription = savingDescription },
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = AttendanceIcons.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = stringResource(R.string.set_office_location),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
