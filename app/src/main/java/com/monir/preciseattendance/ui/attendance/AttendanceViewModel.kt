package com.monir.preciseattendance.ui.attendance

import androidx.lifecycle.ViewModel
import com.monir.preciseattendance.domain.usecase.MarkAttendance
import com.monir.preciseattendance.domain.usecase.ObserveDistanceToOffice
import com.monir.preciseattendance.domain.usecase.SetOfficeLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val observeDistanceToOffice: ObserveDistanceToOffice,
    private val setOfficeLocation: SetOfficeLocation,
    private val markAttendance: MarkAttendance,
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceUiState())
    val state: StateFlow<AttendanceUiState> = _state.asStateFlow()

    private val _events = Channel<AttendanceUiEvent>(Channel.BUFFERED)
    val events: Flow<AttendanceUiEvent> = _events.receiveAsFlow()

    fun onIntent(intent: AttendanceIntent) {
        when (intent) {
            AttendanceIntent.SetOfficeLocationClicked -> {
                // TODO: call setOfficeLocation() and reduce the result into _state
            }
            AttendanceIntent.MarkAttendanceClicked -> {
                // TODO: call markAttendance() and reduce the result into _state
            }
            AttendanceIntent.OpenAppSettingsClicked -> {
                // TODO: send AttendanceUiEvent.OpenAppSettings
            }
            AttendanceIntent.OpenLocationSettingsClicked -> {
                // TODO: send AttendanceUiEvent.OpenLocationSettings
            }
            is AttendanceIntent.PermissionResult -> {
                // TODO: map granted / permanentlyDenied onto LocationStatus and start observing distance
            }
        }
    }
}
