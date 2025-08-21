// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.ui

import org.flameshot.core.Rectangle
import org.flameshot.tools.CaptureTool
import org.flameshot.tools.ToolType
import org.flameshot.tools.PencilTool
import org.flameshot.tools.ArrowTool
import org.flameshot.tools.RectangleTool
import org.flameshot.tools.CopyTool
import org.flameshot.tools.SaveTool

/**
 * GREEN PHASE: Minimal UI component implementations to make tests pass
 */

/**
 * Main capture widget for screenshot capture and editing
 */
class CaptureWidget {
    private var capturing = false
    private var selection = Rectangle()
    
    fun isCapturing(): Boolean = capturing
    
    fun getSelection(): Rectangle = selection
    
    fun startCapture() {
        capturing = true
    }
    
    fun setSelection(newSelection: Rectangle) {
        selection = newSelection
    }
    
    fun acceptSelection(): Boolean {
        capturing = false
        return true
    }
    
    fun cancelCapture() {
        capturing = false
        selection = Rectangle()
    }
}

/**
 * Tool panel for managing drawing tools and settings
 */
class ToolPanel {
    private var visible = true
    private var selectedTool: CaptureTool? = null
    private var selectedColor: UInt = 0xFF0000u // Default red
    private var strokeWidth: Int = 5
    
    private val availableTools = listOf(
        PencilTool(),
        ArrowTool(),
        RectangleTool(),
        CopyTool(),
        SaveTool()
    )
    
    fun getAvailableTools(): List<CaptureTool> = availableTools
    
    fun selectTool(toolType: ToolType) {
        selectedTool = availableTools.find { it.type == toolType }
    }
    
    fun getSelectedTool(): CaptureTool? = selectedTool
    
    fun isVisible(): Boolean = visible
    
    fun show() {
        visible = true
    }
    
    fun hide() {
        visible = false
    }
    
    fun getSelectedColor(): UInt = selectedColor
    
    fun setSelectedColor(color: UInt) {
        selectedColor = color
    }
    
    fun getStrokeWidth(): Int = strokeWidth
    
    fun setStrokeWidth(width: Int) {
        strokeWidth = width
    }
}

/**
 * Color picker component
 */
class ColorPicker {
    private var currentColor: UInt = 0xFF0000u
    
    private val presetColors = listOf(
        0xFF0000u, // Red
        0x00FF00u, // Green
        0x0000FFu, // Blue
        0xFFFF00u, // Yellow
        0xFF00FFu, // Magenta
        0x00FFFFu, // Cyan
        0x000000u, // Black
        0xFFFFFFu, // White
        0x808080u  // Gray
    )
    
    fun getCurrentColor(): UInt = currentColor
    
    fun selectColor(color: UInt) {
        currentColor = color
    }
    
    fun getPresetColors(): List<UInt> = presetColors
    
    fun setHSV(hue: Float, saturation: Float, value: Float) {
        // GREEN PHASE: Simplified HSV to RGB conversion
        // This is a minimal implementation for testing
        currentColor = when {
            hue < 60 -> 0xFF0000u // Red range
            hue < 120 -> 0x00FF00u // Green range
            hue < 180 -> 0x00FFFFu // Cyan range
            hue < 240 -> 0x0000FFu // Blue range
            hue < 300 -> 0xFF00FFu // Magenta range
            else -> 0xFFFF00u // Yellow range
        }
    }
}