// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.tools

/**
 * GREEN PHASE: Minimal implementation to make tests pass
 * Enumeration of all available tools in Flameshot
 */
enum class ToolType(val id: Int) {
    // Drawing tools (0-9)
    PENCIL(0),
    ARROW(1),
    RECTANGLE(2),
    TEXT(3),
    CIRCLE(4),
    MARKER(5),
    PIXELATE(6),
    SELECTION(7),
    
    // Action tools (10-19)
    COPY(10),
    SAVE(11),
    
    // Edit tools (20-29)
    UNDO(20),
    REDO(21),
    
    // Accept/Cancel/Upload
    ACCEPT(30),
    CANCEL(31),
    UPLOAD(32),
    
    // Exit tools (30+)
    EXIT(30);
    
    companion object {
        fun values(): Array<ToolType> = enumValues()
        
        fun fromId(id: Int): ToolType? = values().find { it.id == id }
    }
}