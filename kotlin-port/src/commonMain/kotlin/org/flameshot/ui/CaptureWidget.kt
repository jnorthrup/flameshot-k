// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.flameshot.core.CaptureRequest
import org.flameshot.core.Rectangle
import org.flameshot.tools.*

/**
 * Main capture widget - Compose translation of Qt's CaptureWidget
 * Handles screenshot capture, selection, and tool operations
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CaptureWidget(
    screenshot: ImageBitmap,
    captureRequest: CaptureRequest,
    modifier: Modifier = Modifier,
    onCaptureDone: (ImageBitmap, Rectangle) -> Unit = { _, _ -> },
    onCancelled: () -> Unit = {}
) {
    // Core state management replacing Qt member variables
    var selection by remember { mutableStateOf(Rectangle()) }
    var isSelecting by remember { mutableStateOf(false) }
    var currentTool by remember { mutableStateOf<CaptureTool?>(null) }
    val toolObjects = remember { mutableStateListOf<ToolObject>() }
    val undoStack = remember { UndoStack() }
    
    // Tool configuration state
    var drawColor by remember { mutableStateOf(Color.Red) }
    var strokeWidth by remember { mutableStateOf(5f) }
    
    // Mouse/touch position tracking
    var currentMousePos by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStart by remember { mutableStateOf(Offset.Zero) }
    
    // Active tool path for real-time drawing
    var activePath by remember { mutableStateOf<Path?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .onKeyEvent { keyEvent ->
                handleKeyEvent(
                    keyEvent = keyEvent,
                    onEscape = onCancelled,
                    onUndo = { undoStack.undo() },
                    onRedo = { undoStack.redo() },
                    onDelete = { 
                        if (currentTool != null) {
                            currentTool = null
                        } else if (toolObjects.isNotEmpty()) {
                            toolObjects.removeLast()
                        }
                    },
                    onSelectAll = {
                        selection = Rectangle(
                            0, 0,
                            screenshot.width,
                            screenshot.height
                        )
                    }
                )
            }
    ) {
        // Main screenshot and drawing canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragStart = offset
                            
                            if (currentTool == null && !isSelecting) {
                                // Start selection
                                isSelecting = true
                                selection = Rectangle(
                                    offset.x.toInt(),
                                    offset.y.toInt(),
                                    0, 0
                                )
                            } else if (currentTool is PencilTool) {
                                // Start drawing
                                activePath = Path().apply {
                                    moveTo(offset.x, offset.y)
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            currentMousePos = change.position
                            
                            if (isSelecting) {
                                // Update selection rectangle
                                val minX = minOf(dragStart.x, currentMousePos.x).toInt()
                                val minY = minOf(dragStart.y, currentMousePos.y).toInt()
                                val maxX = maxOf(dragStart.x, currentMousePos.x).toInt()
                                val maxY = maxOf(dragStart.y, currentMousePos.y).toInt()
                                
                                selection = Rectangle(
                                    minX, minY,
                                    maxX - minX,
                                    maxY - minY
                                )
                            } else if (currentTool is PencilTool && activePath != null) {
                                // Continue drawing
                                activePath?.lineTo(currentMousePos.x, currentMousePos.y)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            
                            if (isSelecting) {
                                isSelecting = false
                                if (captureRequest.tasks.contains(CaptureRequest.ExportTask.ACCEPT_ON_SELECT)) {
                                    onCaptureDone(screenshot, selection)
                                }
                            } else if (currentTool != null && activePath != null) {
                                // Commit the tool object
                                val toolObject = when (currentTool) {
                                    is PencilTool -> PencilObject(
                                        path = activePath!!,
                                        color = drawColor,
                                        strokeWidth = strokeWidth
                                    )
                                    is ArrowTool -> ArrowObject(
                                        start = dragStart,
                                        end = currentMousePos,
                                        color = drawColor,
                                        strokeWidth = strokeWidth
                                    )
                                    else -> null
                                }
                                
                                toolObject?.let {
                                    toolObjects.add(it)
                                    undoStack.push(AddToolObjectAction(it, toolObjects))
                                }
                                
                                activePath = null
                            }
                        }
                    )
                }
        ) {
            // Draw screenshot
            drawImage(
                image = screenshot,
                topLeft = Offset.Zero
            )
            
            // Draw selection overlay
            if (selection.width > 0 && selection.height > 0) {
                drawSelectionOverlay(selection, screenshot)
            }
            
            // Draw all committed tool objects
            toolObjects.forEach { it.draw(this) }
            
            // Draw active tool preview
            activePath?.let { path ->
                drawPath(
                    path = path,
                    color = drawColor,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            
            // Draw current tool preview (e.g., arrow while dragging)
            if (isDragging && currentTool is ArrowTool) {
                drawArrowPreview(dragStart, currentMousePos, drawColor, strokeWidth)
            }
        }
        
        // Tool panel - animated visibility
        AnimatedVisibility(
            visible = !isSelecting && selection.width > 0,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ToolPanel(
                currentTool = currentTool,
                drawColor = drawColor,
                strokeWidth = strokeWidth,
                onToolSelected = { tool ->
                    handleToolSelection(
                        tool = tool,
                        screenshot = screenshot,
                        selection = selection,
                        undoStack = undoStack,
                        onCaptureDone = onCaptureDone,
                        onCancelled = onCancelled,
                        onToolChanged = { currentTool = it }
                    )
                },
                onColorChanged = { drawColor = it },
                onStrokeWidthChanged = { strokeWidth = it }
            )
        }
        
        // Side panel for additional options
        AnimatedVisibility(
            visible = !isSelecting && selection.width > 0,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            SidePanel(
                onCopy = { copyToClipboard(screenshot, selection) },
                onSave = { saveScreenshot(screenshot, selection) },
                onUpload = { uploadScreenshot(screenshot, selection) }
            )
        }
        
        // Magnifier for precision selection
        if (isSelecting || isDragging) {
            Magnifier(
                screenshot = screenshot,
                position = currentMousePos,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
        
        // Coordinates display
        if (isSelecting || selection.width > 0) {
            SelectionInfo(
                selection = selection,
                currentPos = currentMousePos,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * Handle keyboard events - replaces Qt's keyPressEvent
 */
private fun handleKeyEvent(
    keyEvent: KeyEvent,
    onEscape: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onDelete: () -> Unit,
    onSelectAll: () -> Unit
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false
    
    return when {
        keyEvent.key == Key.Escape -> {
            onEscape()
            true
        }
        keyEvent.key == Key.Z && keyEvent.isCtrlPressed -> {
            if (keyEvent.isShiftPressed) onRedo() else onUndo()
            true
        }
        keyEvent.key == Key.Delete || keyEvent.key == Key.Backspace -> {
            onDelete()
            true
        }
        keyEvent.key == Key.A && keyEvent.isCtrlPressed -> {
            onSelectAll()
            true
        }
        else -> false
    }
}

/**
 * Draw selection overlay with darkened areas outside selection
 */
private fun DrawScope.drawSelectionOverlay(
    selection: Rectangle,
    screenshot: ImageBitmap
) {
    val selectionRect = Rect(
        selection.x.toFloat(),
        selection.y.toFloat(),
        (selection.x + selection.width).toFloat(),
        (selection.y + selection.height).toFloat()
    )
    
    // Darken area outside selection
    val path = Path().apply {
        addRect(Rect(0f, 0f, size.width, size.height))
        op(
            Path().apply { addRect(selectionRect) },
            this,
            PathOperation.Difference
        )
    }
    
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = 0.5f)
    )
    
    // Draw selection border
    drawRect(
        color = Color.White,
        topLeft = Offset(selectionRect.left, selectionRect.top),
        size = androidx.compose.ui.geometry.Size(selectionRect.width, selectionRect.height),
        style = Stroke(width = 2f)
    )
    
    // Draw resize handles
    val handleSize = 8f
    val handles = listOf(
        Offset(selectionRect.left, selectionRect.top),
        Offset(selectionRect.right, selectionRect.top),
        Offset(selectionRect.left, selectionRect.bottom),
        Offset(selectionRect.right, selectionRect.bottom),
        Offset(selectionRect.center.x, selectionRect.top),
        Offset(selectionRect.center.x, selectionRect.bottom),
        Offset(selectionRect.left, selectionRect.center.y),
        Offset(selectionRect.right, selectionRect.center.y)
    )
    
    handles.forEach { handle ->
        drawCircle(
            color = Color.White,
            radius = handleSize / 2,
            center = handle
        )
        drawCircle(
            color = Color.Blue,
            radius = handleSize / 2 - 1,
            center = handle
        )
    }
}

/**
 * Draw arrow preview while dragging
 */
private fun DrawScope.drawArrowPreview(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float
) {
    ArrowObject(start, end, color, strokeWidth).draw(this)
}

/**
 * Handle tool selection and actions
 */
private fun handleToolSelection(
    tool: CaptureTool,
    screenshot: ImageBitmap,
    selection: Rectangle,
    undoStack: UndoStack,
    onCaptureDone: (ImageBitmap, Rectangle) -> Unit,
    onCancelled: () -> Unit,
    onToolChanged: (CaptureTool?) -> Unit
) {
    when (tool.type) {
        ToolType.SAVE -> saveScreenshot(screenshot, selection)
        ToolType.COPY -> copyToClipboard(screenshot, selection)
        ToolType.UNDO -> undoStack.undo()
        ToolType.REDO -> undoStack.redo()
        ToolType.EXIT -> onCancelled()
        ToolType.ACCEPT -> onCaptureDone(screenshot, selection)
        else -> onToolChanged(tool)
    }
}

// Platform-specific implementations are declared in PlatformActions.kt