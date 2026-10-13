package com.monir.preciseattendance.ui.attendance

import java.util.Locale

/**
 * A coordinate for display only: four decimal places (about 11 m), always with a dot. The stored
 * value is never rounded.
 */
fun formatCoordinate(degrees: Double): String {
    val text = String.format(Locale.ROOT, "%.4f", degrees)
    return if (text == "-0.0000") "0.0000" else text
}
