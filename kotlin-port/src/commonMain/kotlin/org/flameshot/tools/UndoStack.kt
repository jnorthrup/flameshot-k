// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.tools

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Undo/Redo stack implementation - replaces QUndoStack
 */
class UndoStack {
    private val stack = mutableListOf<UndoableAction>()
    private var currentIndex = -1
    
    fun push(action: UndoableAction) {
        // Remove any actions after current index (redo history)
        while (stack.size > currentIndex + 1) {
            stack.removeAt(stack.size - 1)
        }
        
        stack.add(action)
        currentIndex++
        
        // Limit stack size to prevent excessive memory usage
        if (stack.size > MAX_UNDO_LEVELS) {
            stack.removeAt(0)
            currentIndex--
        }
    }
    
    fun undo() {
        if (canUndo()) {
            stack[currentIndex].undo()
            currentIndex--
        }
    }
    
    fun redo() {
        if (canRedo()) {
            currentIndex++
            stack[currentIndex].redo()
        }
    }
    
    fun canUndo(): Boolean = currentIndex >= 0
    
    fun canRedo(): Boolean = currentIndex < stack.size - 1
    
    fun clear() {
        stack.clear()
        currentIndex = -1
    }
    
    companion object {
        private const val MAX_UNDO_LEVELS = 50
    }
}

/**
 * Base interface for undoable actions
 */
interface UndoableAction {
    fun undo()
    fun redo()
    val description: String
}

/**
 * Action for adding a tool object
 */
class AddToolObjectAction(
    private val toolObject: ToolObject,
    private val toolObjects: SnapshotStateList<ToolObject>
) : UndoableAction {
    
    override fun undo() {
        toolObjects.remove(toolObject)
    }
    
    override fun redo() {
        toolObjects.add(toolObject)
    }
    
    override val description: String = "Add drawing"
}

/**
 * Action for removing a tool object
 */
class RemoveToolObjectAction(
    private val toolObject: ToolObject,
    private val toolObjects: SnapshotStateList<ToolObject>,
    private val index: Int
) : UndoableAction {
    
    override fun undo() {
        toolObjects.add(index, toolObject)
    }
    
    override fun redo() {
        toolObjects.removeAt(index)
    }
    
    override val description: String = "Remove drawing"
}

/**
 * Action for moving a tool object
 */
class MoveToolObjectAction(
    private val toolObject: ToolObject,
    private val oldPosition: androidx.compose.ui.geometry.Offset,
    private val newPosition: androidx.compose.ui.geometry.Offset
) : UndoableAction {
    
    override fun undo() {
        val delta = oldPosition - newPosition
        toolObject.move(delta)
    }
    
    override fun redo() {
        val delta = newPosition - oldPosition
        toolObject.move(delta)
    }
    
    override val description: String = "Move drawing"
}

/**
 * Action for modifying tool object properties
 */
class ModifyToolObjectAction(
    private val oldObject: ToolObject,
    private val newObject: ToolObject,
    private val toolObjects: SnapshotStateList<ToolObject>,
    private val index: Int
) : UndoableAction {
    
    override fun undo() {
        toolObjects[index] = oldObject
    }
    
    override fun redo() {
        toolObjects[index] = newObject
    }
    
    override val description: String = "Modify drawing"
}

/**
 * Composite action for grouping multiple actions
 */
class CompositeAction(
    private val actions: List<UndoableAction>,
    override val description: String
) : UndoableAction {
    
    override fun undo() {
        // Undo in reverse order
        actions.asReversed().forEach { it.undo() }
    }
    
    override fun redo() {
        actions.forEach { it.redo() }
    }
}