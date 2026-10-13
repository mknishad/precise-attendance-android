package com.monir.preciseattendance.ui.attendance

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.monir.preciseattendance.ui.attendance.components.AttendanceTopBar
import com.monir.preciseattendance.ui.attendance.components.LocationOffCard
import com.monir.preciseattendance.ui.attendance.components.OfficeStepCard
import com.monir.preciseattendance.ui.attendance.components.PermissionCard
import com.monir.preciseattendance.ui.attendance.components.RangeSection

private val CONTENT_MAX_WIDTH = 600.dp

/**
 * The whole screen as a function of [state]. Stateless, so it can be previewed and tested without
 * a ViewModel. Scrolls, so small screens, landscape and large fonts never clip anything.
 */
@Composable
fun AttendanceContent(
    state: AttendanceUiState,
    actions: AttendanceActions,
    modifier: Modifier = Modifier,
    isMapAvailable: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AttendanceTopBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Only the top and sides are padded outside the scroll area, so the content can
                // scroll behind the navigation bar; the bottom inset is added inside it instead.
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                )
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = innerPadding.calculateBottomPadding() + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = CONTENT_MAX_WIDTH).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                OfficeStepCard(
                    office = state.officeLocation,
                    isSettingOffice = state.isSettingOffice,
                    isMapAvailable = isMapAvailable,
                    onSetOfficeLocation = actions.onSetOfficeLocation,
                )
                MainArea(state, actions)
            }
        }
    }
}

@Composable
private fun MainArea(state: AttendanceUiState, actions: AttendanceActions) {
    Crossfade(targetState = state.mainArea(), label = "mainArea") { area ->
        when (area) {
            com.monir.preciseattendance.ui.attendance.MainArea.PermissionNeeded -> PermissionCard(
                permission = state.permissionState,
                onRequestPermission = actions.onRequestPermission,
                onOpenSettings = actions.onOpenSettings,
            )
            com.monir.preciseattendance.ui.attendance.MainArea.LocationOff ->
                LocationOffCard(onEnableLocation = actions.onEnableLocation, modifier = Modifier.fillMaxWidth())
            com.monir.preciseattendance.ui.attendance.MainArea.Range ->
                RangeSection(state = state, onMarkAttendance = actions.onMarkAttendance, modifier = Modifier.fillMaxWidth())
        }
    }
}
