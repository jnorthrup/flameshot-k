// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot

import org.flameshot.core.CaptureRequest
import org.flameshot.core.Flameshot

/**
 * Simple argument parser for basic CLI functionality
 */
class SimpleArgsParser(private val args: Array<String>) {
    private var index = 0
    
    fun hasNext(): Boolean = index < args.size
    
    fun next(): String = args[index++]
    
    fun peek(): String? = if (hasNext()) args[index] else null
    
    fun hasOption(option: String): Boolean = args.contains(option)
    
    fun getOptionValue(option: String): String? {
        val optionIndex = args.indexOf(option)
        return if (optionIndex != -1 && optionIndex + 1 < args.size) {
            args[optionIndex + 1]
        } else null
    }
    
    fun getIntOptionValue(option: String, default: Int = 0): Int {
        return getOptionValue(option)?.toIntOrNull() ?: default
    }
}

/**
 * Main entry point for Flameshot Kotlin/Native on macOS
 */
fun main(args: Array<String>) {
    println("Flameshot Kotlin - macOS Port v13.1.0")
    
    val parser = SimpleArgsParser(args)
    
    // Check for help
    if (parser.hasOption("--help") || parser.hasOption("-h")) {
        showHelp()
        return
    }
    
    val flameshot = Flameshot.instance()
    
    when {
        parser.hasOption("--version") || parser.hasOption("-v") -> {
            println("Flameshot Kotlin version: ${flameshot.getVersion()}")
            return
        }
        
        parser.hasOption("--config") -> {
            flameshot.config()
            return
        }
        
        parser.hasOption("--launcher") -> {
            flameshot.launcher()
            return
        }
        
        parser.hasOption("--fullscreen") || parser.hasOption("-f") -> {
            handleCaptureMode(CaptureRequest.FULLSCREEN_MODE, parser, flameshot)
        }
        
        parser.hasOption("--screen") || parser.hasOption("-s") -> {
            val screenNumber = parser.getIntOptionValue("-s") ?: parser.getIntOptionValue("--screen") ?: 0
            var request = CaptureRequest.SCREEN_MODE
            request = applyCommonOptions(request, parser)
            flameshot.screen(request, screenNumber)
        }
        
        parser.hasOption("--gui") || parser.hasOption("-g") -> {
            handleCaptureMode(CaptureRequest.GRAPHICAL_MODE, parser, flameshot)
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

private fun handleCaptureMode(baseRequest: CaptureRequest, parser: SimpleArgsParser, flameshot: Flameshot) {
    var request = applyCommonOptions(baseRequest, parser)
    
    when (baseRequest.mode) {
        CaptureRequest.CaptureMode.FULLSCREEN_MODE -> flameshot.full(request)
        CaptureRequest.CaptureMode.GRAPHICAL_MODE -> flameshot.gui(request)
        CaptureRequest.CaptureMode.SCREEN_MODE -> flameshot.screen(request, 0)
    }
}

private fun applyCommonOptions(request: CaptureRequest, parser: SimpleArgsParser): CaptureRequest {
    var result = request
    
    // Apply delay
    val delay = parser.getIntOptionValue("--delay") ?: parser.getIntOptionValue("-d")
    if (delay != null) {
        result = result.copy(delay = delay.toUInt())
    }
    
    // Apply save path
    val savePath = parser.getOptionValue("--path") ?: parser.getOptionValue("-p")
    if (savePath != null) {
        result = result.addSaveTask(savePath)
    }
    
    // Apply copy to clipboard
    if (parser.hasOption("--copy") || parser.hasOption("-c")) {
        result = result.addTask(CaptureRequest.ExportTask.COPY)
    }
    
    return result
}

private fun showHelp() {
    println("""
        Flameshot Kotlin - Screenshot Tool
        
        USAGE:
            flameshot-kotlin [OPTIONS]
            
        OPTIONS:
            -g, --gui              Capture in GUI mode (default)
            -f, --fullscreen       Capture fullscreen
            -s, --screen <number>  Capture specific screen number
            -d, --delay <seconds>  Delay before capture
            -p, --path <path>      Save path for screenshot
            -c, --copy             Copy screenshot to clipboard
            --config               Open configuration
            --launcher             Open launcher  
            -v, --version          Show version
            -h, --help             Show this help
            
        EXAMPLES:
            flameshot-kotlin -g                    # GUI capture mode
            flameshot-kotlin -f -c                # Fullscreen, copy to clipboard  
            flameshot-kotlin -s 0 -p ~/shot.png   # Capture screen 0, save to file
            flameshot-kotlin -f -d 3              # Fullscreen with 3s delay
    """.trimIndent())
}