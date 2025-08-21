// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * RED PHASE: These tests will FAIL initially - that's the point!
 * We write failing tests first, then implement the minimal code to make them pass.
 */
class CaptureToolTest {
    
    @Test
    fun testPencilToolCreation() {
        // RED: This will fail because PencilTool doesn't exist yet
        val pencilTool = PencilTool()
        
        assertEquals(ToolType.PENCIL, pencilTool.type)
        assertEquals("Pencil", pencilTool.name)
        assertTrue(pencilTool.isSelectable())
    }
    
    @Test
    fun testArrowToolCreation() {
        // RED: This will fail because ArrowTool doesn't exist yet
        val arrowTool = ArrowTool()
        
        assertEquals(ToolType.ARROW, arrowTool.type)
        assertEquals("Arrow", arrowTool.name)
        assertTrue(arrowTool.isSelectable())
    }
    
    @Test
    fun testRectangleToolCreation() {
        // RED: This will fail because RectangleTool doesn't exist yet
        val rectTool = RectangleTool()
        
        assertEquals(ToolType.RECTANGLE, rectTool.type)
        assertEquals("Rectangle", rectTool.name)
        assertTrue(rectTool.isSelectable())
    }
    
    @Test
    fun testCopyToolCreation() {
        // RED: This will fail because CopyTool doesn't exist yet
        val copyTool = CopyTool()
        
        assertEquals(ToolType.COPY, copyTool.type)
        assertEquals("Copy", copyTool.name)
        assertFalse(copyTool.isSelectable()) // Action tools shouldn't be selectable for drawing
    }
    
    @Test
    fun testSaveToolCreation() {
        // RED: This will fail because SaveTool doesn't exist yet
        val saveTool = SaveTool()
        
        assertEquals(ToolType.SAVE, saveTool.type)
        assertEquals("Save", saveTool.name)
        assertFalse(saveTool.isSelectable()) // Action tools shouldn't be selectable for drawing
    }
    
    @Test
    fun testToolColorAndStrokeWidth() {
        // RED: This will fail because tool configuration doesn't exist yet
        val pencilTool = PencilTool()
        
        // Default values
        assertNotNull(pencilTool.getColor())
        assertTrue(pencilTool.getStrokeWidth() > 0)
        
        // Configuration
        pencilTool.setColor(0xFF0000u) // Red
        pencilTool.setStrokeWidth(10)
        
        assertEquals(0xFF0000u, pencilTool.getColor())
        assertEquals(10, pencilTool.getStrokeWidth())
    }
    
    @Test
    fun testToolMousePreview() {
        // RED: This will fail because mouse preview doesn't exist yet
        val pencilTool = PencilTool()
        val arrowTool = ArrowTool()
        val copyTool = CopyTool()
        
        assertTrue(pencilTool.useMousePreview())
        assertTrue(arrowTool.useMousePreview())
        assertFalse(copyTool.useMousePreview()) // Action tools don't need preview
    }
}

class ToolTypeTest {
    
    @Test
    fun testToolTypeEnumValues() {
        // RED: This will fail because ToolType enum doesn't exist yet
        val allTypes = ToolType.values()
        
        assertTrue(allTypes.contains(ToolType.PENCIL))
        assertTrue(allTypes.contains(ToolType.ARROW))
        assertTrue(allTypes.contains(ToolType.RECTANGLE))
        assertTrue(allTypes.contains(ToolType.TEXT))
        assertTrue(allTypes.contains(ToolType.COPY))
        assertTrue(allTypes.contains(ToolType.SAVE))
        assertTrue(allTypes.contains(ToolType.UNDO))
        assertTrue(allTypes.contains(ToolType.REDO))
        assertTrue(allTypes.contains(ToolType.EXIT))
    }
    
    @Test
    fun testToolTypeIds() {
        // RED: This will fail because ToolType IDs don't exist yet
        assertEquals(0, ToolType.PENCIL.id)
        assertEquals(1, ToolType.ARROW.id)
        assertEquals(2, ToolType.RECTANGLE.id)
        assertEquals(3, ToolType.TEXT.id)
        assertEquals(10, ToolType.COPY.id)
        assertEquals(11, ToolType.SAVE.id)
        assertEquals(20, ToolType.UNDO.id)
        assertEquals(21, ToolType.REDO.id)
        assertEquals(30, ToolType.EXIT.id)
    }
}