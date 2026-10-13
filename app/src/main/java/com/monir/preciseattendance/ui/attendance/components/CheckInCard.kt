package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monir.preciseattendance.R
import com.monir.preciseattendance.domain.model.AttendanceWindow
import com.monir.preciseattendance.ui.attendance.CheckInMode
import com.monir.preciseattendance.ui.attendance.formatEpochMillis
import com.monir.preciseattendance.ui.attendance.formatLocalTime
import com.monir.preciseattendance.ui.theme.attendanceColors
import java.time.ZoneId

private const val TINT_ALPHA = 0.55f

/**
 * The bottom block. Locked: a dashed outline and a closed lock. Ready: a tinted solid outline,
 * an open lock and a solid button. Marked: a success tint and the time attendance was recorded.
 * [lockedReason] is read out as the button's state when it is disabled, so TalkBack says why.
 */
@Composable
fun CheckInCard(
    mode: CheckInMode,
    window: AttendanceWindow,
    markedAtMillis: Long?,
    isMarking: Boolean,
    lockedReason: String?,
    onMarkAttendance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = checkInStyle(mode)
    val border by animateColorAsState(style.border, label = "checkInBorder")
    val background by animateColorAsState(style.background, label = "checkInBackground")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(background)
            .dashedBorder(color = border, shape = MaterialTheme.shapes.large, dashed = mode == CheckInMode.Locked),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CheckInIcon(mode)
            if (mode == CheckInMode.Marked) MarkedMessage(markedAtMillis)
            MarkAttendanceButton(
                enabled = mode == CheckInMode.Ready && !isMarking,
                isMarking = isMarking,
                lockedReason = lockedReason,
                onClick = onMarkAttendance,
            )
            WindowCaption(window)
        }
    }
}

private class CheckInStyle(val border: Color, val background: Color)

@Composable
private fun checkInStyle(mode: CheckInMode): CheckInStyle {
    val scheme = MaterialTheme.colorScheme
    val extra = MaterialTheme.attendanceColors
    return when (mode) {
        CheckInMode.Locked -> CheckInStyle(scheme.outline, Color.Transparent)
        CheckInMode.Ready -> CheckInStyle(scheme.primary, scheme.primaryContainer.copy(alpha = TINT_ALPHA))
        CheckInMode.Marked -> CheckInStyle(extra.success, extra.successContainer.copy(alpha = TINT_ALPHA))
    }
}

@Composable
private fun CheckInIcon(mode: CheckInMode) {
    Crossfade(targetState = mode, label = "checkInIcon") { current ->
        val (icon, description, tint) = iconFor(current)
        Icon(imageVector = icon, contentDescription = description, tint = tint, modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun iconFor(mode: CheckInMode): Triple<ImageVector, String, Color> = when (mode) {
    CheckInMode.Locked ->
        Triple(AttendanceIcons.Lock, stringResource(R.string.check_in_locked), MaterialTheme.colorScheme.onSurfaceVariant)
    CheckInMode.Ready ->
        Triple(AttendanceIcons.LockOpen, stringResource(R.string.check_in_unlocked), MaterialTheme.colorScheme.primary)
    CheckInMode.Marked ->
        Triple(AttendanceIcons.CheckCircle, stringResource(R.string.check_in_marked_today), MaterialTheme.attendanceColors.success)
}

@Composable
private fun MarkedMessage(markedAtMillis: Long?) {
    val locale = LocalConfiguration.current.locales[0]
    val zone = remember { ZoneId.systemDefault() }
    val text = if (markedAtMillis != null) {
        stringResource(R.string.check_in_marked_at, formatEpochMillis(markedAtMillis, zone, locale))
    } else {
        stringResource(R.string.check_in_marked_today)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.attendanceColors.onSuccessContainer,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun MarkAttendanceButton(
    enabled: Boolean,
    isMarking: Boolean,
    lockedReason: String?,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { if (!enabled && lockedReason != null) stateDescription = lockedReason }
            .testTag(AttendanceTestTags.MARK_ATTENDANCE_BUTTON),
    ) {
        if (isMarking) {
            val markingDescription = stringResource(R.string.check_in_in_progress)
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp).semantics { contentDescription = markingDescription },
                strokeWidth = 2.dp,
            )
        }
        Text(
            text = stringResource(R.string.mark_attendance),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = if (isMarking) 8.dp else 0.dp),
        )
    }
}

@Composable
private fun WindowCaption(window: AttendanceWindow) {
    val locale = LocalConfiguration.current.locales[0]
    val caption = stringResource(
        R.string.check_in_window_caption,
        formatLocalTime(window.start, locale),
        formatLocalTime(window.end, locale),
    ).uppercase(locale)
    Text(
        text = caption,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
