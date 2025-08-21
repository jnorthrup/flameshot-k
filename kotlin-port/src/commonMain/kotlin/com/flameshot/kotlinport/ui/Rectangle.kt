package com.flameshot.kotlinport.ui

import androidx.compose.ui.geometry.Offset

// Shared Rectangle used by the UI capture components. Kept simple for now.
class Rectangle(var left: Float = 0f, var top: Float = 0f, var right: Float = 0f, var bottom: Float = 0f) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    fun bottomRight(): Offset = Offset(right, bottom)
}
