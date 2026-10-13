package com.monir.preciseattendance.ui.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.monir.preciseattendance.R
import com.monir.preciseattendance.ui.location.LocationSettingsResolver
import com.monir.preciseattendance.ui.location.rememberLocationPermissionRequester
import com.monir.preciseattendance.ui.location.rememberLocationSettingsResolver
import com.monir.preciseattendance.ui.theme.AttendanceTheme

/** Placeholder screen: renders the state as plain text so every state can be exercised on a device. */
@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionRequester = rememberLocationPermissionRequester { viewModel.onIntent(AttendanceIntent.PermissionResult(it)) }
    val settingsResolver = rememberLocationSettingsResolver { viewModel.onIntent(AttendanceIntent.LocationSettingsResolved(it)) }

    CollectEvents(viewModel, settingsResolver)

    AttendanceContent(
        state = state,
        actions = AttendanceActions(
            onRequestPermission = permissionRequester::request,
            onOpenSettings = { viewModel.onIntent(AttendanceIntent.OpenSettingsClicked) },
            onSetOfficeLocation = { viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked) },
            onMarkAttendance = { viewModel.onIntent(AttendanceIntent.MarkAttendanceClicked) },
        ),
    )
}

/** Handles events only while the screen is started, so none is lost or acted on in the background. */
@Composable
private fun CollectEvents(viewModel: AttendanceViewModel, settingsResolver: LocationSettingsResolver) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentResolver by rememberUpdatedState(settingsResolver)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { handleAttendanceEvent(it, context, currentResolver) }
        }
    }
}

@Composable
fun AttendanceContent(
    state: AttendanceUiState,
    actions: AttendanceActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(R.string.attendance_title), style = MaterialTheme.typography.titleLarge)
            StatusText(state.statusMessage)
            PermissionButton(state.permissionState, actions)
            Button(
                onClick = actions.onSetOfficeLocation,
                enabled = !state.isSettingOffice,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.set_office_location)) }
            Button(
                onClick = actions.onMarkAttendance,
                enabled = state.isMarkAttendanceEnabled && !state.isMarkingAttendance,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.mark_attendance)) }
            state.dumpLines().forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun StatusText(message: StatusMessage) {
    Text(text = LocalResources.current.statusText(message), style = MaterialTheme.typography.bodyLarge)
}

/** Offers the right next step for the permission state, or nothing once it is granted. */
@Composable
private fun PermissionButton(permission: LocationPermissionState, actions: AttendanceActions) {
    val (label, onClick) = when (permission) {
        LocationPermissionState.Granted -> return
        LocationPermissionState.NotRequested,
        LocationPermissionState.Denied,
        -> R.string.action_allow_location to actions.onRequestPermission
        LocationPermissionState.ApproximateOnly -> R.string.action_allow_precise_location to actions.onRequestPermission
        LocationPermissionState.PermanentlyDenied -> R.string.action_open_app_settings to actions.onOpenSettings
    }
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(stringResource(label)) }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceContentPreview() {
    AttendanceTheme {
        AttendanceContent(state = AttendanceUiState(), actions = AttendanceActions())
    }
}
