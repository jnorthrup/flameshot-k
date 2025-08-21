// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.tools

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import kotlin.math.hypot

/**
 * Minimal ToolObject compatibility used by the Compose UI and the Undo stack.
 * This is a small, well-scoped adapter to allow the simplified tools model
 * to interoperate with UI code that expects drawable/movable objects.
 */
interface ToolObject {
    fun draw(drawScope: DrawScope)
    fun move(delta: Offset)
}

data class PencilObject(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    private var offset: Offset = Offset.Zero
) : ToolObject {
    override fun draw(drawScope: DrawScope) {
        val p = Path().apply { addPath(path, offset) }
        drawScope.drawPath(
            path = p,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }

    override fun move(delta: Offset) {
        offset = Offset(offset.x + delta.x, offset.y + delta.y)
    }
}

data class ArrowObject(
    var start: Offset,
    var end: Offset,
    val color: Color,
    val strokeWidth: Float
) : ToolObject {
    override fun draw(drawScope: DrawScope) {
        drawScope.drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = strokeWidth
        )

        // Simple arrow head
        val dx = end.x - start.x
        val dy = end.y - start.y
        val length = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        if (length > 0f) {
            val nx = dx / length
            val ny = dy / length
            val size = 10f
            val left = Offset(
                end.x - nx * size - ny * (size / 2),
                end.y - ny * size + nx * (size / 2)
            )
            val right = Offset(
                end.x - nx * size + ny * (size / 2),
                end.y - ny * size - nx * (size / 2)
            )
            drawScope.drawLine(color = color, start = end, end = left, strokeWidth = strokeWidth)
            drawScope.drawLine(color = color, start = end, end = right, strokeWidth = strokeWidth)
        }
    }

    override fun move(delta: Offset) {
        start = Offset(start.x + delta.x, start.y + delta.y)
        end = Offset(end.x + delta.x, end.y + delta.y)
    }
}

data class SelectionObject(
    var start: Offset,
    var end: Offset,
    val color: Color,
    val strokeWidth: Float
) : ToolObject {
    override fun draw(drawScope: DrawScope) {
        val left = minOf(start.x, end.x)
        val top = minOf(start.y, end.y)
        val right = maxOf(start.x, end.x)
        val bottom = maxOf(start.y, end.y)
        drawScope.drawRect(
            color = color,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            style = Stroke(width = strokeWidth)
        )
    }

    override fun move(delta: Offset) {
        start = Offset(start.x + delta.x, start.y + delta.y)
        end = Offset(end.x + delta.x, end.y + delta.y)
    }
}

/**
 * Lightweight pencil stroke used for JVM tests where full Path/Skia
 * may not be available. This stores a simple two-point stroke and
 * draws a straight line between them.
 */
data class PencilStrokeObject(
    var start: Offset,
    var end: Offset,
    val color: Color,
    val strokeWidth: Float
) : ToolObject {
    override fun draw(drawScope: DrawScope) {
        drawScope.drawLine(color = color, start = start, end = end, strokeWidth = strokeWidth)
    }

    override fun move(delta: Offset) {
        start = Offset(start.x + delta.x, start.y + delta.y)
        end = Offset(end.x + delta.x, end.y + delta.y)
    }
}
