package com.flameshot.kotlinport.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DrawingCanvas(
    toolObjects: List<ToolObject>,
    currentPath: androidx.compose.ui.graphics.Path?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Skia-based drawing will be implemented here
    }
}
