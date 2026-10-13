package com.monir.preciseattendance.ui.attendance.components

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A rounded border that is dashed when [dashed] is true and solid otherwise. */
fun Modifier.dashedBorder(
    color: Color,
    shape: CornerBasedShape,
    dashed: Boolean,
    strokeWidth: Dp = 2.dp,
): Modifier = drawBehind {
    val width = strokeWidth.toPx()
    val dashes = if (dashed) PathEffect.dashPathEffect(floatArrayOf(DASH_ON.toPx(), DASH_OFF.toPx())) else null
    drawRoundRect(
        color = color,
        topLeft = Offset(width / 2, width / 2),
        size = Size(size.width - width, size.height - width),
        cornerRadius = CornerRadius(shape.topStart.toPx(size, this)),
        style = Stroke(width = width, pathEffect = dashes),
    )
}

private val DASH_ON = 12.dp
private val DASH_OFF = 8.dp
