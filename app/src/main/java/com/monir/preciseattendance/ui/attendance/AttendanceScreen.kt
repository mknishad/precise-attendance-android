package com.monir.preciseattendance.ui.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monir.preciseattendance.R
import com.monir.preciseattendance.ui.theme.AttendanceTheme

@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { _ ->
            // TODO: handle one-off effects (permission request, settings intents, messages)
        }
    }

    AttendanceContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun AttendanceContent(
    state: AttendanceUiState,
    onIntent: (AttendanceIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.attendance_title),
                style = MaterialTheme.typography.titleLarge,
            )
            // TODO: build the screen from the design: office context card, distance indicator,
            //  status chip, permission / GPS prompts, and the locked-state Mark Attendance panel.
            Button(
                onClick = { onIntent(AttendanceIntent.SetOfficeLocationClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.set_office_location))
            }
            Button(
                onClick = { onIntent(AttendanceIntent.MarkAttendanceClicked) },
                enabled = false, // TODO: enable only when state.locationStatus is InRange
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.mark_attendance))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceContentPreview() {
    AttendanceTheme {
        AttendanceContent(state = AttendanceUiState(), onIntent = {})
    }
}
