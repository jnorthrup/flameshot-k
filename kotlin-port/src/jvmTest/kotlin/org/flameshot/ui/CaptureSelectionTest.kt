package org.flameshot.ui

import androidx.compose.ui.geometry.Rect
import com.flameshot.kotlinport.ui.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals

// TDD: start with a failing spec for Rectangle normalization and size
class CaptureSelectionTest {
    @Test
    fun selectionWidthHeightAndNormalizedRect() {
        // Create a Rectangle with inverted coordinates (dragging from bottom-right to top-left)
        val left = 200f
        val top = 150f
        val right = 100f
        val bottom = 50f

        val rect = Rectangle(left, top, right, bottom)

        // Expect width/height to be computed as right-left and bottom-top (may be negative before normalization)
        assertEquals(right - left, rect.width)
        assertEquals(bottom - top, rect.height)

        val normalized = Rect(
            left = minOf(rect.left, rect.right),
            top = minOf(rect.top, rect.bottom),
            right = maxOf(rect.left, rect.right),
            bottom = maxOf(rect.top, rect.bottom)
        )

        // Normalized should give positive width/height
        assertEquals(100f, normalized.width)
        assertEquals(100f, normalized.height)
    }
}
