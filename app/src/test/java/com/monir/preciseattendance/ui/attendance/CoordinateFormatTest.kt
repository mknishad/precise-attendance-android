package com.monir.preciseattendance.ui.attendance

import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CoordinateFormatTest {

    private val originalLocale: Locale = Locale.getDefault()

    @Before
    fun useACommaLocale() = Locale.setDefault(Locale.GERMANY)

    @After
    fun restoreLocale() = Locale.setDefault(originalLocale)

    @Test
    fun showsFourDecimalPlaces_withADot_whateverTheDeviceLocale() {
        assertEquals("40.7128", formatCoordinate(40.7128))
        assertEquals("-74.0060", formatCoordinate(-74.00604321))
    }

    @Test
    fun padsShortValues() {
        assertEquals("12.5000", formatCoordinate(12.5))
        assertEquals("0.0000", formatCoordinate(0.0))
    }

    @Test
    fun doesNotShowNegativeZero() {
        assertEquals("0.0000", formatCoordinate(-0.00001))
    }

    @Test
    fun formattingIsDisplayOnly_theInputIsNotChanged() {
        val stored = -37.81362765432101
        formatCoordinate(stored)
        assertEquals(-37.81362765432101, stored, 0.0)
    }
}
