package com.monir.preciseattendance.di

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * A system clock that reads the device's time zone on every call, so a long-lived process still
 * evaluates the attendance window in local time after the user travels or changes zone.
 */
internal object DeviceZoneClock : Clock() {
    override fun getZone(): ZoneId = ZoneId.systemDefault()

    override fun withZone(zone: ZoneId): Clock = system(zone)

    override fun instant(): Instant = Instant.now()
}
