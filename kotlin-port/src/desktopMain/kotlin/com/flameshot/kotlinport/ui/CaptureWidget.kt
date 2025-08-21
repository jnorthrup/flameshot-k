package com.flameshot.kotlinport.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun CaptureWidget(
    modifier: Modifier = Modifier,
    onCaptureDone: (/* TODO: Screenshot type */ Any, /* TODO: Rect type */ Any) -> Unit = { _, _ -> },
    onCancelled: () -> Unit = {}
) {
    var selection by remember { mutableStateOf(Rectangle()) }
    var isSelecting by remember { mutableStateOf(false) }
    var currentTool by remember { mutableStateOf<CaptureTool?>(null) }
    val toolObjects = remember { mutableStateListOf<ToolObject>() }

    var drawColor by remember { mutableStateOf(androidx.compose.ui.graphics.Color.Red) }
    var strokeWidth by remember { mutableStateOf(5f) }

    Box(modifier = modifier.fillMaxSize()) {
        // Placeholder for ScreenshotCanvas
        DrawingCanvas(
            toolObjects = toolObjects,
            currentPath = null,
            modifier = Modifier.fillMaxSize()
        )

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

// Minimal placeholder types to keep this compile-safe until full models are implemented
class Rectangle(var left: Float = 0f, var top: Float = 0f, var right: Float = 0f, var bottom: Float = 0f) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    fun bottomRight(): Offset = Offset(right, bottom)
}

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
