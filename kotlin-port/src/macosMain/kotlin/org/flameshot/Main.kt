// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot

import org.flameshot.core.CaptureRequest
import org.flameshot.core.Flameshot
import kotlinx.cinterop.*
import kotlinx.cli.*

/**
 * Main entry point for Flameshot Kotlin/Native on macOS
 */
fun main(args: Array<String>) {
    println("Flameshot Kotlin - macOS Port v13.1.0")
    
    val parser = ArgParser("flameshot-kotlin")
    
    val gui by parser.option(ArgType.Boolean, shortName = "g", description = "Capture GUI mode").default(false)
    val fullscreen by parser.option(ArgType.Boolean, shortName = "f", description = "Capture fullscreen").default(false)
    val screen by parser.option(ArgType.Int, shortName = "s", description = "Capture specific screen number")
    val delay by parser.option(ArgType.Int, shortName = "d", description = "Delay in seconds before capture").default(0)
    val save by parser.option(ArgType.String, shortName = "p", description = "Save path for screenshot")
    val copy by parser.option(ArgType.Boolean, shortName = "c", description = "Copy to clipboard").default(false)
    val config by parser.option(ArgType.Boolean, description = "Open configuration").default(false)
    val launcher by parser.option(ArgType.Boolean, description = "Open launcher").default(false)
    val version by parser.option(ArgType.Boolean, shortName = "v", description = "Show version").default(false)
    
    try {
        parser.parse(args)
    } catch (e: Exception) {
        println("Error parsing arguments: ${e.message}")
        return
    }
    
    val flameshot = Flameshot.instance()
    
    when {
        version -> {
            println("Flameshot Kotlin version: ${flameshot.getVersion()}")
            return
        }
        
        config -> {
            flameshot.config()
            return
        }
        
        launcher -> {
            flameshot.launcher()
            return
        }
        
        fullscreen -> {
            var request = CaptureRequest.FULLSCREEN_MODE.copy(delay = delay.toUInt())
            
            if (!save.isNullOrEmpty()) {
                request = request.addSaveTask(save)
            }
            if (copy) {
                request = request.addTask(CaptureRequest.ExportTask.COPY)
            }
            
            flameshot.full(request)
        }
        
        screen != null -> {
            var request = CaptureRequest.SCREEN_MODE.copy(delay = delay.toUInt())
            
            if (!save.isNullOrEmpty()) {
                request = request.addSaveTask(save)
            }
            if (copy) {
                request = request.addTask(CaptureRequest.ExportTask.COPY)
            }
            
            flameshot.screen(request, screen)
        }
        
        gui -> {
            var request = CaptureRequest.GRAPHICAL_MODE.copy(delay = delay.toUInt())
            
            if (!save.isNullOrEmpty()) {
                request = request.addSaveTask(save)
            }
            if (copy) {
                request = request.addTask(CaptureRequest.ExportTask.COPY)
            }
            
            flameshot.gui(request)
        }
        
        else -> {
            // Default to GUI mode if no specific mode is requested
            println("Starting Flameshot in GUI mode...")
            println("Use --help to see available options")
            flameshot.gui()
        }
    }
    
    // Keep the application running for GUI interactions
    println("Flameshot Kotlin is running. Press Ctrl+C to exit.")
    
    // Simple event loop - in a real app this would be more sophisticated
    try {
        while (true) {
            Thread.sleep(100)
        }
    } catch (e: InterruptedException) {
        println("Application interrupted, exiting...")
    }
}