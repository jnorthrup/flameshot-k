package com.flameshot.kotlinport.ui

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CaptureViewModel {
    private val _drawColor = MutableStateFlow(Color.Red)
    val drawColor: StateFlow<Color> = _drawColor.asStateFlow()

    private val _toolSize = MutableStateFlow(5)
    val toolSize: StateFlow<Int> = _toolSize.asStateFlow()

    fun setDrawColor(color: Color) {
        _drawColor.value = color
    }

    fun setToolSize(size: Int) {
        _toolSize.value = size.coerceIn(1, 50)
    }
}
