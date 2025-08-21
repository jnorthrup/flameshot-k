// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot

import org.flameshot.core.CaptureRequest
import org.flameshot.core.Flameshot

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
    
    // For tests and CLI runs we don't block; native GUI apps would run their runloop instead.
    println("Flameshot Kotlin startup complete (non-blocking for test/CLI runs)")
}

private fun handleCaptureMode(baseRequest: CaptureRequest, parser: SimpleArgsParser, flameshot: Flameshot) {
    var request = applyCommonOptions(baseRequest, parser)
    
    when (baseRequest.mode) {
        CaptureRequest.CaptureMode.FULLSCREEN_MODE -> flameshot.full(request)
        CaptureRequest.CaptureMode.GRAPHICAL_MODE -> flameshot.gui(request)
        CaptureRequest.CaptureMode.SCREEN_MODE -> flameshot.screen(request, 0)
    }
}

// applyCommonOptions moved to commonMain `CliOptions.kt`

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