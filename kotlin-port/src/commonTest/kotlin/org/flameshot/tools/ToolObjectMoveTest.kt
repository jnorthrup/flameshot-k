package org.flameshot.tools

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ToolObjectMoveTest {
    @Test
    fun testArrowObjectMove() {
        val arrow = ArrowObject(
            start = Offset(1f, 2f),
            end = Offset(3f, 4f),
            color = Color.Black,
            strokeWidth = 2f
        )

        arrow.move(Offset(5f, 6f))

        assertEquals(Offset(6f, 8f), arrow.start)
        assertEquals(Offset(8f, 10f), arrow.end)
    }

    @Test
    fun testSelectionObjectMove() {
        val sel = SelectionObject(
            start = Offset(0f, 0f),
            end = Offset(10f, 20f),
            color = Color.Red,
            strokeWidth = 1f
        )

        sel.move(Offset(3f, 4f))

        assertEquals(Offset(3f, 4f), sel.start)
        assertEquals(Offset(13f, 24f), sel.end)
    }

    @Test
    fun testPencilStrokeObjectMove() {
        val ps = PencilStrokeObject(
            start = Offset(2f, 3f),
            end = Offset(4f, 6f),
            color = Color.Green,
            strokeWidth = 1.5f
        )

        ps.move(Offset(-1f, -2f))

        assertEquals(Offset(1f, 1f), ps.start)
        assertEquals(Offset(3f, 4f), ps.end)
    }
}
