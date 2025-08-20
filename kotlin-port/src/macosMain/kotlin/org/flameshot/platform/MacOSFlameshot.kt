// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.platform

import org.flameshot.core.*
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.AppKit.*
import platform.CoreGraphics.*

/**
 * macOS-specific Flameshot implementation
 */
class MacOSFlameshot : Flameshot() {

    // macOS-specific hotkey references
    private var hotkeyScreenshotCapture: Any? = null
    private var hotkeyScreenshotHistory: Any? = null

    override fun platformGui(req: CaptureRequest): Any? {
        // Create and show capture widget for macOS
        println("Creating macOS capture GUI for request: $req")
        // TODO: Implement native macOS capture window
        return null
    }

    override fun platformScreen(req: CaptureRequest, screenNumber: Int) {
        println("Capturing screen $screenNumber on macOS with request: $req")
        captureScreen(screenNumber, req)
    }

    override fun platformFull(req: CaptureRequest) {
        println("Capturing full screen on macOS with request: $req")
        captureFullScreen(req)
    }

    override fun platformLauncher() {
        println("Showing launcher on macOS")
        // TODO: Implement launcher window
    }

    override fun platformConfig() {
        println("Showing configuration on macOS")
        // TODO: Implement config window
    }

    override fun platformInfo() {
        println("Showing info window on macOS")
        // TODO: Implement info window
    }

    override fun platformOpenSavePath() {
        println("Opening save path on macOS")
        // Open Finder to the save path
        val savePath = NSHomeDirectory() + "/Pictures" // Default save location
        NSWorkspace.sharedWorkspace.openURL(NSURL.fileURLWithPath(savePath))
    }

    override fun platformRequestCapture(request: CaptureRequest) {
        println("Requesting capture on macOS: $request")
        when (request.mode) {
            CaptureRequest.CaptureMode.FULLSCREEN_MODE -> platformFull(request)
            CaptureRequest.CaptureMode.GRAPHICAL_MODE -> platformGui(request)
            CaptureRequest.CaptureMode.SCREEN_MODE -> platformScreen(request, 0)
        }
    }

    override fun platformExportCapture(screenshot: Screenshot, selection: Rectangle, req: CaptureRequest) {
        println("Exporting capture on macOS")
        
        req.tasks.forEach { task ->
            when (task) {
                CaptureRequest.ExportTask.SAVE -> {
                    val path = req.path.ifEmpty { generateDefaultPath() }
                    screenshot.save(path)
                }
                CaptureRequest.ExportTask.COPY -> {
                    screenshot.copyToClipboard()
                }
                else -> println("Task $task not yet implemented on macOS")
            }
        }
    }

    private fun captureScreen(screenNumber: Int, req: CaptureRequest) {
        // Use macOS CGDisplayCreateImage API
        val displays = CGGetActiveDisplayList(32u, null, null)
        println("Capturing screen using macOS CoreGraphics APIs")
        
        // TODO: Implement actual screen capture using CGDisplayCreateImage
        // For now, just simulate the capture
        simulateCapture(req)
    }

    private fun captureFullScreen(req: CaptureRequest) {
        println("Capturing full screen using macOS APIs")
        // TODO: Implement full screen capture
        simulateCapture(req)
    }

    private fun simulateCapture(req: CaptureRequest) {
        // Simulate a successful capture for now
        val mockScreenshot = MacOSScreenshot(byteArrayOf()) // Empty screenshot for now
        // Emit capture taken event
        println("Simulated capture completed")
    }

    private fun generateDefaultPath(): String {
        val homeDir = NSHomeDirectory()
        val timestamp = NSDate().timeIntervalSince1970.toLong()
        return "$homeDir/Pictures/flameshot_$timestamp.png"
    }
}

/**
 * macOS-specific Screenshot implementation
 */
actual class Screenshot(private val imageData: ByteArray) {
    
    actual fun save(path: String): Boolean {
        return try {
            val data = NSData.dataWithBytes(imageData.refTo(0), imageData.size.toULong())
            data.writeToFile(path, true)
        } catch (e: Exception) {
            println("Failed to save screenshot: ${e.message}")
            false
        }
    }

    actual fun copyToClipboard() {
        try {
            // Copy to macOS pasteboard
            val pasteboard = NSPasteboard.generalPasteboard
            pasteboard.clearContents()
            
            val data = NSData.dataWithBytes(imageData.refTo(0), imageData.size.toULong())
            pasteboard.setData(data, NSPasteboardTypePNG)
            
            println("Screenshot copied to clipboard")
        } catch (e: Exception) {
            println("Failed to copy screenshot to clipboard: ${e.message}")
        }
    }
}

// Type alias for the actual implementation
typealias MacOSScreenshot = Screenshot

/**
 * Platform factory function for macOS
 */
actual fun platformCreateInstance(): Flameshot {
    return MacOSFlameshot()
}