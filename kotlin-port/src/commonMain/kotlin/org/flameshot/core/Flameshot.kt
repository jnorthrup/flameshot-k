// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Main Flameshot application class - Kotlin common implementation
 */
class Flameshot private constructor() {
    
    enum class Origin {
        CLI,
        DAEMON
    }

    // Capture-related events
    private val _captureTaken = MutableSharedFlow<Screenshot>()
    val captureTaken: SharedFlow<Screenshot> = _captureTaken.asSharedFlow()
    
    private val _captureFailed = MutableSharedFlow<Unit>()
    val captureFailed: SharedFlow<Unit> = _captureFailed.asSharedFlow()

    // Instance state
    private var haveExternalWidget: Boolean = false
    
    // Window references - platform-specific implementations will handle actual windows
    private var captureWindow: Any? = null
    private var infoWindow: Any? = null 
    private var launcherWindow: Any? = null
    private var configWindow: Any? = null

    /**
     * Launch GUI capture mode
     */
    fun gui(req: CaptureRequest = CaptureRequest.GRAPHICAL_MODE): Any? {
        return platformGui(req)
    }

    /**
     * Capture specific screen
     */
    fun screen(req: CaptureRequest, screenNumber: Int = -1) {
        platformScreen(req, screenNumber)
    }

    /**
     * Capture full screen
     */
    fun full(req: CaptureRequest) {
        platformFull(req)
    }

    /**
     * Show launcher window
     */
    fun launcher() {
        platformLauncher()
    }

    /**
     * Show configuration window
     */
    fun config() {
        if (!resolveAnyConfigErrors()) {
            return
        }
        platformConfig()
    }

    /**
     * Show info window
     */
    fun info() {
        platformInfo()
    }

    /**
     * Open save path directory
     */
    fun openSavePath() {
        platformOpenSavePath()
    }

    /**
     * Get application version
     */
    fun getVersion(): String = "13.1.0"

    /**
     * Request a capture with the given request
     */
    fun requestCapture(request: CaptureRequest) {
        platformRequestCapture(request)
    }

    /**
     * Export a captured screenshot
     */
    fun exportCapture(screenshot: Screenshot, selection: Rectangle, req: CaptureRequest) {
        platformExportCapture(screenshot, selection, req)
    }

    /**
     * Set whether we have external widget
     */
    fun setExternalWidget(b: Boolean) {
        haveExternalWidget = b
    }

    /**
     * Check if we have external widget
     */
    fun haveExternalWidget(): Boolean = haveExternalWidget

    /**
     * Resolve configuration errors - platform specific
     */
    private fun resolveAnyConfigErrors(): Boolean {
        return platformResolveConfigErrors()
    }

    // Platform-specific functions to be implemented by each platform
    protected open fun platformGui(req: CaptureRequest): Any? = null
    protected open fun platformScreen(req: CaptureRequest, screenNumber: Int) {}
    protected open fun platformFull(req: CaptureRequest) {}
    protected open fun platformLauncher() {}
    protected open fun platformConfig() {}
    protected open fun platformInfo() {}
    protected open fun platformOpenSavePath() {}
    protected open fun platformRequestCapture(request: CaptureRequest) {}
    protected open fun platformExportCapture(screenshot: Screenshot, selection: Rectangle, req: CaptureRequest) {}
    protected open fun platformResolveConfigErrors(): Boolean = true

    companion object {
        @Volatile
        private var INSTANCE: Flameshot? = null
        
        private var _origin: Origin = Origin.DAEMON

        fun instance(): Flameshot {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: createInstance().also { INSTANCE = it }
            }
        }

        private fun createInstance(): Flameshot {
            return platformCreateInstance()
        }

        fun setOrigin(origin: Origin) {
            _origin = origin
        }

        fun origin(): Origin = _origin
    }
}

/**
 * Platform-specific factory function - to be implemented by each platform
 */
expect fun platformCreateInstance(): Flameshot

/**
 * Screenshot data class - platform implementations will provide native image types
 */
expect class Screenshot {
    fun save(path: String): Boolean
    fun copyToClipboard()
}