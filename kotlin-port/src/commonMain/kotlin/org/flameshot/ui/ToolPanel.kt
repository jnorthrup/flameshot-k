// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package org.flameshot.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.flameshot.tools.*

/**
 * Tool panel composable - replaces Qt's button handler and tool button widgets
 */
@Composable
fun ToolPanel(
    currentTool: CaptureTool?,
    drawColor: Color,
    strokeWidth: Float,
    onToolSelected: (CaptureTool) -> Unit,
    onColorChanged: (Color) -> Unit,
    onStrokeWidthChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Available tools
    val tools = remember {
        listOf(
            SelectionTool(),
            PencilTool(),
            ArrowTool(),
            RectangleTool(),
            CircleTool(),
            TextTool(),
            MarkerTool(),
            PixelateTool(),
            // Action tools
            UndoTool(),
            RedoTool(),
            CopyTool(),
            SaveTool(),
            AcceptTool(),
            CancelTool()
        )
    }
    
    Surface(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = Color.Black.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tool buttons grid
            ToolButtonGrid(
                tools = tools.filter { it.isDrawingTool || it.isAction },
                currentTool = currentTool,
                onToolSelected = onToolSelected
            )
            
            Divider(color = Color.White.copy(alpha = 0.2f))
            
            // Color and size controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color picker
                ColorPickerButton(
                    currentColor = drawColor,
                    onColorSelected = onColorChanged
                )
                
                // Stroke width slider
                StrokeWidthControl(
                    strokeWidth = strokeWidth,
                    onStrokeWidthChanged = onStrokeWidthChanged
                )
            }
        }
    }
}

/**
 * Grid of tool buttons
 */
@Composable
private fun ToolButtonGrid(
    tools: List<CaptureTool>,
    currentTool: CaptureTool?,
    onToolSelected: (CaptureTool) -> Unit
) {
    val columns = 7
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        tools.chunked(columns).forEach { rowTools ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowTools.forEach { tool ->
                    ToolButton(
                        tool = tool,
                        isSelected = tool == currentTool,
                        onClick = { onToolSelected(tool) }
                    )
                }
                // Fill empty spaces in the last row
                repeat(columns - rowTools.size) {
                    Spacer(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

/**
 * Individual tool button
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToolButton(
    tool: CaptureTool,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            Color.Blue.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        }
    )
    
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tool.description)
            }
        },
        state = rememberTooltipState()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color.Blue else Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.name,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Color picker button
 */
@Composable
private fun ColorPickerButton(
    currentColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    
    Box {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(currentColor)
                .border(2.dp, Color.White, CircleShape)
                .clickable { showPicker = !showPicker }
        )
        
        if (showPicker) {
            ColorPickerPopup(
                currentColor,
                {
                    onColorSelected(it)
                    showPicker = false
                },
                { showPicker = false }
            )
        }
    }
}

/**
 * Stroke width control
 */
@Composable
private fun StrokeWidthControl(
    strokeWidth: Float,
    onStrokeWidthChanged: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Decrease button
        IconButton(
            onClick = { 
                onStrokeWidthChanged((strokeWidth - 1).coerceAtLeast(1f))
            },
            modifier = Modifier.size(24.dp)
        ) {
            Text("-", color = Color.White)
        }
        
        // Visual indicator
        Canvas(modifier = Modifier.size(40.dp, 20.dp)) {
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                strokeWidth = strokeWidth
            )
        }
        
        // Increase button
        IconButton(
            onClick = {
                onStrokeWidthChanged((strokeWidth + 1).coerceAtMost(50f))
            },
            modifier = Modifier.size(24.dp)
        ) {
            Text("+", color = Color.White)
        }
        
        // Numeric display
        Text(
            text = "${strokeWidth.toInt()}px",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Side panel for additional actions
 */
@Composable
fun SidePanel(
    onCopy: () -> Unit,
    onSave: () -> Unit,
    onUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = Color.Black.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SidePanelButton(
                text = "Copy",
                onClick = onCopy
            )
            SidePanelButton(
                text = "Save",
                onClick = onSave
            )
            SidePanelButton(
                text = "Upload",
                onClick = onUpload
            )
        }
    }
}

@Composable
private fun SidePanelButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue.copy(alpha = 0.5f)
        )
    ) {
        Text(text, color = Color.White)
    }
}

/**
 * Magnifier widget for precision selection
 */
@Composable
fun Magnifier(
    screenshot: ImageBitmap,
    position: Offset,
    modifier: Modifier = Modifier,
    zoomFactor: Float = 4f
) {
    Surface(
        modifier = modifier.size(150.dp),
        shape = CircleShape,
        border = BorderStroke(2.dp, Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw magnified portion of screenshot
            val sourceRect = Rect(
                position.x - 75f / zoomFactor,
                position.y - 75f / zoomFactor,
                position.x + 75f / zoomFactor,
                position.y + 75f / zoomFactor
            )
            
            // Clip to circle
            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 2
            )
            
            // Draw magnified image (platform-specific implementation needed)
            // This would require actual image manipulation
            
            // Draw crosshair
            val centerX = size.width / 2
            val centerY = size.height / 2
            drawLine(
                color = Color.Red,
                start = androidx.compose.ui.geometry.Offset(0f, centerY),
                end = androidx.compose.ui.geometry.Offset(size.width, centerY),
                strokeWidth = 1f
            )
            drawLine(
                color = Color.Red,
                start = androidx.compose.ui.geometry.Offset(centerX, 0f),
                end = androidx.compose.ui.geometry.Offset(centerX, size.height),
                strokeWidth = 1f
            )
        }
    }
}

/**
 * Selection info display
 */
@Composable
fun SelectionInfo(
    selection: org.flameshot.core.Rectangle,
    currentPos: Offset,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "Selection: ${selection.width} × ${selection.height}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Position: (${currentPos.x.toInt()}, ${currentPos.y.toInt()})",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Tool implementations for UI actions
class SelectionTool : CaptureTool() {
    override val type = ToolType.SELECTION
    override val icon = EmptyImageVector
    override val name = "Selection"
    override val description = "Select area"
    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float) =
        org.flameshot.tools.SelectionObject(startPoint, endPoint, color, strokeWidth)
}

class MarkerTool : CaptureTool() {
    override val type = ToolType.MARKER
    override val icon = EmptyImageVector
    override val name = "Marker"
    override val description = "Highlight with marker"
    override val isDrawingTool = true
    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float) = null
}

class PixelateTool : CaptureTool() {
    override val type = ToolType.PIXELATE
    override val icon = EmptyImageVector
    override val name = "Pixelate"
    override val description = "Pixelate area"
    override val isDrawingTool = true
    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float) = null
}

class UndoTool : CaptureTool() {
    override val type = ToolType.UNDO
    override val icon = EmptyImageVector
    override val name = "Undo"
    override val description = "Undo last action"
    override val isAction = true
    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float) = null
}

class RedoTool : CaptureTool() {
    override val type = ToolType.REDO
    override val icon = EmptyImageVector
    override val name = "Redo"
    override val description = "Redo last action"
    override val isAction = true
    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float) = null
}

class CopyTool : CaptureTool() {
    override val type = ToolType.COPY
    override val icon = EmptyImageVector
    override val name = "Copy"
    override val description = "Copy to clipboard"
    override val isAction = true
    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float) = null
}

class SaveTool : CaptureTool() {
    override val type = ToolType.SAVE
    override val icon = EmptyImageVector
    override val name = "Save"
    override val description = "Save screenshot"
    override val isAction = true
    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float) = null
}

class AcceptTool : CaptureTool() {
    override val type = ToolType.ACCEPT
    override val icon = EmptyImageVector
    override val name = "Accept"
    override val description = "Accept selection"
    override val isAction = true
    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float) = null
}

class CancelTool : CaptureTool() {
    override val type = ToolType.CANCEL
    override val icon = EmptyImageVector
    override val name = "Cancel"
    override val description = "Cancel capture"
    override val isAction = true
    override fun createObject(startPoint: Offset, endPoint: Offset, color: Color, strokeWidth: Float) = null
}

// EmptyImageVector is provided by org.flameshot.tools.UiCompat