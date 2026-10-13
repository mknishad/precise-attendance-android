package com.monir.preciseattendance.ui.attendance.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monir.preciseattendance.R
import com.monir.preciseattendance.ui.attendance.LocationPermissionState
import com.monir.preciseattendance.ui.attendance.missingMessage
import com.monir.preciseattendance.ui.attendance.statusText

/**
 * Explains what is wrong with location permission and offers the one action that can fix it:
 * ask again, or open app settings once the system will no longer show the dialog. With only
 * approximate location, both are offered because asking again does not always show a dialog.
 */
@Composable
fun PermissionCard(
    permission: LocationPermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = permission.missingMessage() ?: return
    AttendanceCard(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconBadge(
                icon = AttendanceIcons.LocationOn,
                container = MaterialTheme.colorScheme.primaryContainer,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(permission.titleRes()),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = LocalResources.current.statusText(message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            PermissionActions(permission, onRequestPermission, onOpenSettings)
        }
    }
}

@Composable
private fun PermissionActions(
    permission: LocationPermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    when (permission) {
        LocationPermissionState.Granted -> Unit
        LocationPermissionState.NotRequested,
        LocationPermissionState.Denied,
        -> FilledAction(R.string.action_grant_location_access, onRequestPermission)
        LocationPermissionState.PermanentlyDenied -> FilledAction(R.string.action_open_app_settings, onOpenSettings)
        LocationPermissionState.ApproximateOnly -> {
            FilledAction(R.string.action_allow_precise_location, onRequestPermission)
            OutlinedButton(
                onClick = onOpenSettings,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(R.string.action_open_app_settings)) }
        }
    }
}

@Composable
private fun FilledAction(@StringRes label: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) { Text(stringResource(label)) }
}

@StringRes
private fun LocationPermissionState.titleRes(): Int = when (this) {
    LocationPermissionState.PermanentlyDenied -> R.string.permission_title_blocked
    LocationPermissionState.ApproximateOnly -> R.string.permission_title_precise
    LocationPermissionState.Granted,
    LocationPermissionState.NotRequested,
    LocationPermissionState.Denied,
    -> R.string.permission_title_needed
}
