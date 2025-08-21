package org.flameshot.ui

import androidx.compose.ui.graphics.ImageBitmap
import org.flameshot.core.Rectangle

expect fun copyToClipboard(screenshot: ImageBitmap, selection: Rectangle)
expect fun saveScreenshot(screenshot: ImageBitmap, selection: Rectangle)
expect fun uploadScreenshot(screenshot: ImageBitmap, selection: Rectangle)
