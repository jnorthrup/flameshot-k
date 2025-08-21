package com.flameshot.kotlinport.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ToolPanel(
    tools: List<CaptureTool>,
    currentTool: CaptureTool?,
    onToolSelected: (CaptureTool) -> Unit,
    onColorChanged: (Color) -> Unit = {},
    onStrokeWidthChanged: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tools.forEach { tool ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToolSelected(tool) }
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = tool.javaClass.simpleName)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ColorPicker and SizeSlider would go here
    }
}
