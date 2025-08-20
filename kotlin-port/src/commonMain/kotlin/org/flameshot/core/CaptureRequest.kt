// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.core

/**
 * Represents a screenshot capture request with mode and export options
 */
data class CaptureRequest(
    val mode: CaptureMode = CaptureMode.GRAPHICAL_MODE,
    val delay: UInt = 0u,
    val data: Any? = null,
    val tasks: Set<ExportTask> = emptySet(),
    private var staticId: UInt? = null,
    private var savePath: String = "",
    private var pinWindowGeometry: Rectangle = Rectangle(),
    private var initialSelection: Rectangle = Rectangle()
) {
    enum class CaptureMode {
        FULLSCREEN_MODE,
        GRAPHICAL_MODE,
        SCREEN_MODE
    }

    enum class ExportTask(val value: Int) {
        NO_TASK(0),
        COPY(1),
        SAVE(2),
        PRINT_RAW(4),
        PRINT_GEOMETRY(8),
        PIN(16),
        UPLOAD(32),
        ACCEPT_ON_SELECT(64)
    }

    val id: UInt
        get() = staticId ?: generateId()

    val path: String
        get() = savePath

    private fun generateId(): UInt {
        // Simple ID generation - in real implementation would be more robust
        return kotlin.random.Random.nextInt().toUInt()
    }

    fun setStaticId(id: UInt): CaptureRequest {
        return copy(staticId = id)
    }

    fun addTask(task: ExportTask): CaptureRequest {
        return copy(tasks = tasks + task)
    }

    fun removeTask(task: ExportTask): CaptureRequest {
        return copy(tasks = tasks - task)
    }

    fun addSaveTask(path: String = ""): CaptureRequest {
        return copy(tasks = tasks + ExportTask.SAVE, savePath = path)
    }

    fun addPinTask(pinGeometry: Rectangle): CaptureRequest {
        return copy(tasks = tasks + ExportTask.PIN, pinWindowGeometry = pinGeometry)
    }

    fun setInitialSelection(selection: Rectangle): CaptureRequest {
        return copy(initialSelection = selection)
    }

    companion object {
        val GRAPHICAL_MODE = CaptureRequest(CaptureMode.GRAPHICAL_MODE)
        val FULLSCREEN_MODE = CaptureRequest(CaptureMode.FULLSCREEN_MODE)
        val SCREEN_MODE = CaptureRequest(CaptureMode.SCREEN_MODE)
    }
}

/**
 * Simple rectangle data class - platform implementations will provide native rectangle types
 */
data class Rectangle(
    val x: Int = 0,
    val y: Int = 0, 
    val width: Int = 0,
    val height: Int = 0
)