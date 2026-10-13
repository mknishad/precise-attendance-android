package com.monir.preciseattendance.ui.attendance

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private fun timeFormatter(locale: Locale): DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)

/** A time of day the way the device's locale writes it, for example "9:00 am" or "09:00". */
fun formatLocalTime(time: LocalTime, locale: Locale): String = timeFormatter(locale).format(time)

fun formatEpochMillis(epochMillis: Long, zone: ZoneId, locale: Locale): String =
    timeFormatter(locale).format(Instant.ofEpochMilli(epochMillis).atZone(zone))
