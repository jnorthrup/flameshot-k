// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Configuration handler for Flameshot settings
 */
@Serializable
data class FlameshotConfig(
    val savePath: String = "",
    val saveAsFileExtension: String = "png",
    val jpegQuality: Int = 75,
    val uiColor: String = "#663399",
    val contrastUiColor: String = "#ffffff", 
    val contrastOpacity: Int = 190,
    val copyPathAfterSave: Boolean = false,
    val savePathFixed: Boolean = false,
    val shortcuts: Map<String, String> = defaultShortcuts()
) {
    companion object {
        private fun defaultShortcuts() = mapOf(
            "TAKE_SCREENSHOT" to "Cmd+Shift+3",
            "SCREENSHOT_HISTORY" to "Cmd+Shift+4"
        )
    }
}

/**
 * Configuration manager that handles reading/writing settings
 */
object ConfigHandler {
    private var currentConfig = FlameshotConfig()
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    fun getConfig(): FlameshotConfig = currentConfig
    
    fun updateConfig(update: (FlameshotConfig) -> FlameshotConfig) {
        currentConfig = update(currentConfig)
        saveConfig()
    }
    
    fun savePath(): String = currentConfig.savePath
    
    fun setSavePath(path: String) {
        updateConfig { it.copy(savePath = path) }
    }
    
    fun saveAsFileExtension(): String = currentConfig.saveAsFileExtension
    
    fun jpegQuality(): Int = currentConfig.jpegQuality
    
    fun uiColor(): String = currentConfig.uiColor
    
    fun setUiColor(color: String) {
        updateConfig { it.copy(uiColor = color) }
    }
    
    fun contrastUiColor(): String = currentConfig.contrastUiColor
    
    fun setContrastUiColor(color: String) {
        updateConfig { it.copy(contrastUiColor = color) }
    }
    
    fun contrastOpacity(): Int = currentConfig.contrastOpacity
    
    fun copyPathAfterSave(): Boolean = currentConfig.copyPathAfterSave
    
    fun savePathFixed(): Boolean = currentConfig.savePathFixed
    
    fun shortcut(key: String): String {
        return currentConfig.shortcuts[key] ?: ""
    }
    
    fun setShortcut(key: String, value: String) {
        updateConfig { 
            it.copy(shortcuts = it.shortcuts + (key to value))
        }
    }
    
    private fun saveConfig() {
        // Platform-specific implementation will handle actual file I/O
        platformSaveConfig(json.encodeToString(currentConfig))
    }
    
    fun loadConfig() {
        val configString = platformLoadConfig()
        if (configString.isNotEmpty()) {
            try {
                currentConfig = json.decodeFromString(configString)
            } catch (e: Exception) {
                println("Failed to load config: ${e.message}, using defaults")
            }
        }
    }
}

/**
 * Platform-specific config save/load functions
 */
expect fun platformSaveConfig(configJson: String)
expect fun platformLoadConfig(): String