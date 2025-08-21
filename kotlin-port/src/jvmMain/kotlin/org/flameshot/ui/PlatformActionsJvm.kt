package org.flameshot.ui

import androidx.compose.ui.graphics.ImageBitmap
import org.flameshot.core.Rectangle

actual fun copyToClipboard(screenshot: ImageBitmap, selection: Rectangle) {
    // No-op for JVM unit tests
}

actual fun saveScreenshot(screenshot: ImageBitmap, selection: Rectangle) {
    // No-op for JVM unit tests
}

actual fun uploadScreenshot(screenshot: ImageBitmap, selection: Rectangle) {
    // No-op for JVM unit tests
}
