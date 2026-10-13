package com.monir.preciseattendance.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monir.preciseattendance.domain.model.AttendanceStatus
import com.monir.preciseattendance.domain.model.AttendanceWindow
import com.monir.preciseattendance.domain.model.LocationError
import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.domain.model.attendanceStatusFor
import com.monir.preciseattendance.domain.repository.AttendanceRepository
import com.monir.preciseattendance.domain.repository.LocationRepository
import com.monir.preciseattendance.domain.repository.OfficeLocationRepository
import com.monir.preciseattendance.domain.usecase.MarkAttendance
import com.monir.preciseattendance.domain.usecase.ObserveProximity
import com.monir.preciseattendance.domain.usecase.SetOfficeLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModel @Inject constructor(
    private val observeProximity: ObserveProximity,
    private val setOfficeLocation: SetOfficeLocation,
    private val markAttendance: MarkAttendance,
    private val locationRepository: LocationRepository,
    officeLocationRepository: OfficeLocationRepository,
    attendanceRepository: AttendanceRepository,
    private val window: AttendanceWindow,
    private val clock: Clock,
) : ViewModel() {

    private val permissionState = MutableStateFlow(LocationPermissionState.NotRequested)
    private val isSettingOffice = MutableStateFlow(false)
    private val isMarkingAttendance = MutableStateFlow(false)

    private val _events = Channel<AttendanceUiEvent>(Channel.BUFFERED)
    val events: Flow<AttendanceUiEvent> = _events.receiveAsFlow()

    private val device = combine(permissionState, locationRepository.isLocationEnabled(), ::DeviceState)
        .distinctUntilChanged()
    private val progress = combine(isSettingOffice, isMarkingAttendance, ::ActionProgress)
    private val schedule = combine(attendanceRepository.observeLastMarkedAtMillis(), ticks(), ::scheduleAt)
    private val office = officeLocationRepository.observeOfficeLocation()

    /**
     * Location updates are collected only while this has a subscriber (the screen is started) and
     * only while [device] says they can work, so the GPS is never running without permission.
     */
    val state: StateFlow<AttendanceUiState> = device
        .flatMapLatest { deviceState ->
            combine(proximityFor(deviceState), office, schedule, progress) { proximity, office, schedule, progress ->
                AttendanceUiState(
                    permissionState = deviceState.permission,
                    isLocationEnabled = deviceState.locationEnabled,
                    officeLocation = office,
                    proximity = proximity,
                    isSettingOffice = progress.settingOffice,
                    isMarkingAttendance = progress.marking,
                    attendanceWindow = window,
                    isWithinWindow = schedule.withinWindow,
                    alreadyMarkedToday = schedule.markedAtMillis != null,
                    markedAtMillis = schedule.markedAtMillis,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AttendanceUiState(attendanceWindow = window),
        )

    fun onIntent(intent: AttendanceIntent) {
        when (intent) {
            is AttendanceIntent.PermissionResult -> onPermissionResult(intent.state)
            AttendanceIntent.SetOfficeLocationClicked -> onSetOfficeLocationClicked()
            AttendanceIntent.MarkAttendanceClicked -> onMarkAttendanceClicked()
            is AttendanceIntent.LocationSettingsResolved -> onLocationSettingsResolved(intent.success)
            AttendanceIntent.OpenSettingsClicked -> onOpenSettingsClicked()
        }
    }

    private fun onPermissionResult(state: LocationPermissionState) {
        permissionState.value = state
    }

    private fun onSetOfficeLocationClicked() {
        val missingPermission = permissionState.value.missingMessage()
        if (missingPermission != null) {
            emit(AttendanceUiEvent.ShowMessage(missingPermission))
            return
        }
        if (!isSettingOffice.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                setOffice()
            } finally {
                isSettingOffice.value = false
            }
        }
    }

    /** Never relies on the UI having enabled the button; [MarkAttendance] re-validates everything. */
    private fun onMarkAttendanceClicked() {
        if (!isMarkingAttendance.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                _events.send(markAttendance().toEvent())
            } finally {
                isMarkingAttendance.value = false
            }
        }
    }

    /** The user is told if they declined; if they accepted, the location-enabled flow updates. */
    private fun onLocationSettingsResolved(success: Boolean) {
        if (!success) emit(AttendanceUiEvent.ShowMessage(StatusMessage.LocationSettingsDeclined))
    }

    private fun onOpenSettingsClicked() = emit(AttendanceUiEvent.OpenAppSettings)

    private suspend fun setOffice() {
        val event = if (locationRepository.isLocationEnabled().first()) {
            setOfficeLocation().toEvent()
        } else {
            AttendanceUiEvent.RequestLocationSettingsResolution
        }
        _events.send(event)
    }

    private fun proximityFor(deviceState: DeviceState): Flow<ProximityResult> = when {
        deviceState.permission != LocationPermissionState.Granted ->
            flowOf(ProximityResult.LocationUnavailable(LocationError.PermissionDenied))
        !deviceState.locationEnabled ->
            flowOf(ProximityResult.LocationUnavailable(LocationError.ServicesDisabled))
        else -> observeProximity()
    }

    /** Ticks now, then every minute and at each window boundary, so time-based state stays current. */
    private fun ticks(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(millisUntilNextTick())
        }
    }

    private fun millisUntilNextTick(): Long =
        window.millisUntilNextChange(LocalTime.now(clock))?.coerceIn(1L, TICK_MILLIS) ?: TICK_MILLIS

    private fun scheduleAt(lastMarkedAtMillis: Long?, tick: Unit): Schedule = Schedule(
        withinWindow = window.isOpen(clock),
        markedAtMillis = (attendanceStatusFor(lastMarkedAtMillis, clock) as? AttendanceStatus.Marked)
            ?.markedAtEpochMillis,
    )

    private fun emit(event: AttendanceUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private data class DeviceState(val permission: LocationPermissionState, val locationEnabled: Boolean)
    private data class ActionProgress(val settingOffice: Boolean, val marking: Boolean)
    private data class Schedule(val withinWindow: Boolean, val markedAtMillis: Long?)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val TICK_MILLIS = 60_000L
    }
}
