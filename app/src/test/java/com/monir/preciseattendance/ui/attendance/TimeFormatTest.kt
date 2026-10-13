package com.monir.preciseattendance.ui.attendance

import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeFormatTest {

    @Test
    fun a24HourLocale_writesTwentyFourHourTime() {
        assertEquals("09:00", formatLocalTime(LocalTime.of(9, 0), Locale.GERMANY))
        assertEquals("22:30", formatLocalTime(LocalTime.of(22, 30), Locale.GERMANY))
    }

    @Test
    fun aTwelveHourLocale_writesAmAndPm() {
        // Newer Java versions put a narrow no-break space before AM/PM, so match any space.
        val text = formatLocalTime(LocalTime.of(10, 30), Locale.US)
        assertTrue(text, Regex("""10:30\p{Zs}AM""").matches(text))
    }

    @Test
    fun epochMillis_areShownInTheGivenZone() {
        val utcNine = 1_760_000_000_000L // 2025-10-09 08:53:20 UTC
        assertEquals("10:53", formatEpochMillis(utcNine, ZoneId.of("Europe/Paris"), Locale.GERMANY))
        assertEquals("08:53", formatEpochMillis(utcNine, ZoneId.of("UTC"), Locale.GERMANY))
    }
}
