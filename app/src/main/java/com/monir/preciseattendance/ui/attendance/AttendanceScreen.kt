package com.monir.preciseattendance.ui.attendance

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.monir.preciseattendance.ui.attendance.components.rememberMapAvailable
import com.monir.preciseattendance.ui.location.LocationSettingsResolver
import com.monir.preciseattendance.ui.location.rememberLocationPermissionRequester
import com.monir.preciseattendance.ui.location.rememberLocationSettingsResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Wires the ViewModel, permission and settings helpers, and events to the stateless content. */
@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionRequester = rememberLocationPermissionRequester { viewModel.onIntent(AttendanceIntent.PermissionResult(it)) }
    val settingsResolver = rememberLocationSettingsResolver { viewModel.onIntent(AttendanceIntent.LocationSettingsResolved(it)) }

    CollectEvents(viewModel, settingsResolver, snackbarHostState)

    AttendanceContent(
        state = state,
        isMapAvailable = rememberMapAvailable(),
        snackbarHostState = snackbarHostState,
        actions = AttendanceActions(
            onRequestPermission = permissionRequester::request,
            onOpenSettings = { viewModel.onIntent(AttendanceIntent.OpenSettingsClicked) },
            onEnableLocation = settingsResolver::resolve,
            onSetOfficeLocation = { viewModel.onIntent(AttendanceIntent.SetOfficeLocationClicked) },
            onMarkAttendance = { viewModel.onIntent(AttendanceIntent.MarkAttendanceClicked) },
        ),
    )
}

/** Handles events only while the screen is started, so none is lost or acted on in the background. */
@Composable
private fun CollectEvents(
    viewModel: AttendanceViewModel,
    settingsResolver: LocationSettingsResolver,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val currentResolver by rememberUpdatedState(settingsResolver)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                handleAttendanceEvent(event, context, currentResolver) { scope.showSnackbar(snackbarHostState, it) }
            }
        }
    }
}

/** Replaces any snackbar already showing, and does not block the event loop while it is visible. */
private fun CoroutineScope.showSnackbar(host: SnackbarHostState, message: String) {
    launch {
        host.currentSnackbarData?.dismiss()
        host.showSnackbar(message = message, duration = SnackbarDuration.Short)
    }
}
