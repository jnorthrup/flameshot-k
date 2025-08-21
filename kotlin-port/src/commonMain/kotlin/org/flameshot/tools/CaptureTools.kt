// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.tools

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * GREEN PHASE: Provide a UI-compatible abstract CaptureTool class so Compose UI
 * can interact with tools via properties like `icon`, `description`, and
 * `createObject` returning `ToolObject`.
 */
abstract class CaptureTool {
    abstract val type: ToolType
    abstract val name: String

    // UI related properties expected by Compose code
    open val icon: ImageVector = EmptyImageVector
    open val description: String = name
    open val isDrawingTool: Boolean = false
    open val isAction: Boolean = false

    // Hook to create a drawable ToolObject (used by CaptureWidget)
    open fun createObject(
        startPoint: Offset,
        endPoint: Offset,
        color: Color,
        strokeWidth: Float
    ): ToolObject? = null

    // General behavior
    open fun isSelectable(): Boolean = true
    open fun isValid(): Boolean = true
    open fun useMousePreview(): Boolean = false

    // Configuration
    open fun getColor(): UInt = 0u
    open fun setColor(color: UInt) {}
    open fun getStrokeWidth(): Int = 1
    open fun setStrokeWidth(width: Int) {}
}

/**
 * Base implementation for drawing tools
 */
abstract class DrawingTool : CaptureTool() {
    private var toolColor: UInt = 0xFF0000u // Default red
    private var toolStrokeWidth: Int = 5

    override val isDrawingTool: Boolean = true
    override fun useMousePreview(): Boolean = true

    override fun getColor(): UInt = toolColor
    override fun setColor(color: UInt) { this.toolColor = color }
    override fun getStrokeWidth(): Int = toolStrokeWidth
    override fun setStrokeWidth(width: Int) { this.toolStrokeWidth = width }
}

/**
 * Base implementation for action tools
 */
abstract class ActionTool : CaptureTool() {
    override val isAction: Boolean = true
    override fun isSelectable(): Boolean = false
    override fun useMousePreview(): Boolean = false
}

// GREEN PHASE: Minimal implementations for each tool type used by tests/UI

class PencilTool : DrawingTool() {
    override val type = ToolType.PENCIL
    override val name = "Pencil"

    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float): ToolObject? {
        return PencilStrokeObject(startPoint, endPoint, color, strokeWidth)
    }
}

class ArrowTool : DrawingTool() {
    override val type = ToolType.ARROW
    override val name = "Arrow"

    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float): ToolObject? {
        return ArrowObject(startPoint, endPoint, color, strokeWidth)
    }
}

class RectangleTool : DrawingTool() {
    override val type = ToolType.RECTANGLE
    override val name = "Rectangle"
}

class CircleTool : DrawingTool() {
    override val type = ToolType.CIRCLE
    override val name = "Circle"
}

class TextTool : DrawingTool() {
    override val type = ToolType.TEXT
    override val name = "Text"
}

class CopyTool : ActionTool() {
    override val type = ToolType.COPY
    override val name = "Copy"
}

class SaveTool : ActionTool() {
    override val type = ToolType.SAVE
    override val name = "Save"
}