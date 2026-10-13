package com.monir.preciseattendance.domain.model

sealed interface AttendanceStatus {
    data object NotMarked : AttendanceStatus
    data class Marked(val markedAtEpochMillis: Long) : AttendanceStatus
}
