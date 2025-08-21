package org.flameshot.tools

import kotlin.test.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

class CaptureToolsTest {

    @Test
    fun arrowCreateObject_returnsArrowObject() {
        val tool = ArrowTool()
        val start = Offset(1f, 2f)
        val end = Offset(10f, 20f)
        val color = Color.Red
        val stroke = 3f

        val obj = tool.createObject(start, end, color, stroke)

        assertNotNull(obj, "ArrowTool.createObject should not return null")
        assertTrue(obj is ArrowObject, "Expected ArrowObject from ArrowTool")

        obj as ArrowObject
        assertEquals(start, obj.start)
        assertEquals(end, obj.end)
        assertEquals(color, obj.color)
        assertEquals(stroke, obj.strokeWidth)
    }

    @Test
    fun drawingTool_color_and_stroke_defaults_and_setters() {
        val pencil = PencilTool()

        // defaults from DrawingTool implementation
        assertEquals(0xFF0000u, pencil.getColor())
        assertEquals(5, pencil.getStrokeWidth())

        // mutate and verify
        pencil.setColor(0x00FF00u)
        pencil.setStrokeWidth(8)
        assertEquals(0x00FF00u, pencil.getColor())
        assertEquals(8, pencil.getStrokeWidth())
    }

    @Test
    fun pencilCreateObject_returnsPencilObject() {
        val tool = PencilTool()
        val start = Offset(1f, 2f)
        val end = Offset(10f, 20f)
        val color = Color.Green
        val stroke = 4f

        val obj = tool.createObject(start, end, color, stroke)

    assertNotNull(obj, "PencilTool.createObject should not return null")
    assertTrue(obj is org.flameshot.tools.PencilStrokeObject, "Expected PencilStrokeObject from PencilTool")

    val strokeObj = obj as org.flameshot.tools.PencilStrokeObject
    assertEquals(color, strokeObj.color)
    assertEquals(stroke, strokeObj.strokeWidth)
    }

    @Test
    fun actionTool_flags() {
        val copy = CopyTool()
        assertTrue(copy.isAction)
        assertFalse(copy.isSelectable(), "ActionTool-derived tools should not be selectable")
        assertFalse(copy.useMousePreview(), "Action tools should not use mouse preview")
    }

    @Test
    fun selectionTool_createObject_should_return_non_null() {
        // RED PHASE: this test is expected to fail until SelectionTool is implemented
    val sel = org.flameshot.ui.SelectionTool()
    val obj = sel.createObject(Offset(0f,0f), Offset(10f,10f), Color.Black, 1f)
        assertNotNull(obj, "SelectionTool.createObject should return a ToolObject (RED test)")
    }
}
