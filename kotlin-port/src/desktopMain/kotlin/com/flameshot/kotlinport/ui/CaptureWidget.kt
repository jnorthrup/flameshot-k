package com.flameshot.kotlinport.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun CaptureWidget(
    modifier: Modifier = Modifier,
    onCaptureDone: (screenshot: ImageBitmap, rect: Rect) -> Unit = { _, _ -> },
    onCancelled: () -> Unit = {}
) {
    var selection by remember { mutableStateOf(Rectangle()) }
    var isSelecting by remember { mutableStateOf(false) }
    var currentTool by remember { mutableStateOf<CaptureTool?>(null) }
    val toolObjects = remember { mutableStateListOf<ToolObject>() }

    var drawColor by remember { mutableStateOf(androidx.compose.ui.graphics.Color.Red) }
    var strokeWidth by remember { mutableStateOf(5f) }

    // Track a drag start offset for selection
    var dragStart by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isSelecting = true
                        dragStart = offset
                        selection.left = offset.x
                        selection.top = offset.y
                        selection.right = offset.x
                        selection.bottom = offset.y
                    },
                    onDrag = { change, _ ->
                        val pos = change.position
                        selection.right = pos.x
                        selection.bottom = pos.y
                    },
                    onDragEnd = {
                        isSelecting = false
                        dragStart = null
                        // In this minimal implementation we can't create a real screenshot.
                        // Provide a tiny placeholder ImageBitmap and the selected rect to the callback.
                        val placeholder = ImageBitmap(1, 1)
                        val rect = Rect(selection.left, selection.top, selection.right, selection.bottom)
                        onCaptureDone(placeholder, rect)
                    },
                    onDragCancel = {
                        isSelecting = false
                        dragStart = null
                    }
                )
            }
    ) {
        // Placeholder for ScreenshotCanvas
        DrawingCanvas(
            toolObjects = toolObjects,
            currentPath = null,
            modifier = Modifier.fillMaxSize()
        )

        // Visual selection overlay while selecting
        if (isSelecting) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    topLeft = Offset(selection.left, selection.top),
                    size = androidx.compose.ui.geometry.Size(selection.width, selection.height),
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        // Tool panel stub
        ToolPanel(
            tools = listOf(),
            currentTool = currentTool,
            onToolSelected = { tool -> currentTool = tool },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        )
    }
}

// ...existing code...

sealed class CaptureTool {
    object Pencil : CaptureTool()
    object Arrow : CaptureTool()
    object Text : CaptureTool()
    object Save : CaptureTool()
    object Copy : CaptureTool()
    object Undo : CaptureTool()
    object Redo : CaptureTool()
    object Exit : CaptureTool()
}

sealed class ToolObject
