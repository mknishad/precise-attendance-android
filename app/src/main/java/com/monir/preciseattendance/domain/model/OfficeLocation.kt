package com.monir.preciseattendance.domain.model

data class OfficeLocation(
    val point: GeoPoint,
    val savedAtMillis: Long,
)
