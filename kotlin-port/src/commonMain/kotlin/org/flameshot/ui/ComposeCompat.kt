// Minimal compile-time shims for Compose types used by the UI files during tests.
// These are intentionally lightweight and non-functional; they only exist to
// allow unit tests to compile without pulling the full Compose desktop runtime.
package org.flameshot.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Size

typealias ImageBitmap = androidx.compose.ui.graphics.ImageBitmap
typealias Offset = androidx.compose.ui.geometry.Offset
typealias Rect = androidx.compose.ui.geometry.Rect

// Minimal Shape stub used for compilation in tests
object CircleShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: androidx.compose.ui.unit.Density) =
        throw NotImplementedError("CircleShape.createOutline is a test-only stub and should not be executed")
}

// Placeholder composable for color picker popup used only in compilation
@Suppress("FunctionName", "UNUSED_PARAMETER")
fun ColorPickerPopup(_currentColor: androidx.compose.ui.graphics.Color, _onColorSelected: (androidx.compose.ui.graphics.Color) -> Unit, _onDismiss: () -> Unit) {
    // No-op stub for tests
}
