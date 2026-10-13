package com.monir.preciseattendance.domain.geo

import com.monir.preciseattendance.domain.model.ProximityResult
import com.monir.preciseattendance.fakes.testFix
import com.monir.preciseattendance.fakes.testOffice
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class ProximityEvaluatorTest {

    private fun evaluatorAt(distanceMeters: Double) =
        ProximityEvaluator { _, _ -> distanceMeters }

    @Test
    fun noOffice_isNoOfficeSet_evenWithAFix() {
        val result = evaluatorAt(0.0).evaluate(office = null, fix = testFix())
        assertEquals(ProximityResult.NoOfficeSet, result)
    }

    @Test
    fun officeButNoFix_isAcquiringFix() {
        val result = evaluatorAt(0.0).evaluate(office = testOffice(), fix = null)
        assertEquals(ProximityResult.AcquiringFix, result)
    }

    @Test
    fun exactlyFiftyMetres_isInRange() {
        val result = evaluatorAt(50.0).evaluate(testOffice(), testFix())
        assertEquals(ProximityResult.InRange(50.0), result)
    }

    @Test
    fun fiftyPointOneMetres_isOutOfRange() {
        val result = evaluatorAt(50.1).evaluate(testOffice(), testFix())
        assertEquals(ProximityResult.OutOfRange(50.1), result)
    }

    @Test
    fun zeroMetres_isInRange() {
        val result = evaluatorAt(0.0).evaluate(testOffice(), testFix())
        assertEquals(ProximityResult.InRange(0.0), result)
    }

    @Test
    fun accuracyExactlyAtTheLimit_isStillTrusted() {
        val result = evaluatorAt(10.0).evaluate(testOffice(), testFix(accuracyMeters = 30.0))
        assertEquals(ProximityResult.InRange(10.0), result)
    }

    @Test
    fun accuracyJustWorseThanTheLimit_isWeakSignal() {
        val result = evaluatorAt(10.0).evaluate(testOffice(), testFix(accuracyMeters = 30.1))
        assertEquals(ProximityResult.WeakSignal(30.1), result)
    }

    @Test
    fun poorAccuracy_isWeakSignal_evenWhenTheFixLooksInsideTheRadius() {
        val result = evaluatorAt(0.0).evaluate(testOffice(), testFix(accuracyMeters = 500.0))
        assertEquals(ProximityResult.WeakSignal(500.0), result)
    }

    @Test
    fun poorAccuracy_isWeakSignal_evenWhenTheFixLooksOutsideTheRadius() {
        val result = evaluatorAt(900.0).evaluate(testOffice(), testFix(accuracyMeters = 500.0))
        assertEquals(ProximityResult.WeakSignal(500.0), result)
    }

    @Test
    fun nanAccuracy_isWeakSignal_notInRange() {
        val result = evaluatorAt(0.0).evaluate(testOffice(), testFix(accuracyMeters = Double.NaN))
        assertEquals(ProximityResult.WeakSignal(Double.NaN), result)
    }

    @Test
    fun weakSignal_skipsTheDistanceCalculation() {
        val calculator = mockk<DistanceCalculator>()
        every { calculator.metersBetween(any(), any()) } returns 0.0

        ProximityEvaluator(calculator).evaluate(testOffice(), testFix(accuracyMeters = 100.0))

        verify(exactly = 0) { calculator.metersBetween(any(), any()) }
    }

    @Test
    fun measuresFromTheOfficePointToTheFix() {
        val calculator = mockk<DistanceCalculator>()
        every { calculator.metersBetween(any(), any()) } returns 12.0
        val office = testOffice()
        val fix = testFix()

        ProximityEvaluator(calculator).evaluate(office, fix)

        verify(exactly = 1) { calculator.metersBetween(office.point, fix.point) }
    }
}
