package com.monir.preciseattendance.fakes

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/** A clock the test can move, to cover time passing during a suspending call. */
class MutableTestClock(
    var now: Instant,
    private val zone: ZoneId = TEST_ZONE,
) : Clock() {
    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableTestClock(now, zone)

    override fun instant(): Instant = now
}
