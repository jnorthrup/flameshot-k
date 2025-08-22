// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot

/**
 * Simple argument parser for basic CLI functionality
 */
class SimpleArgsParser(private val args: Array<String>) {
    private var index = 0
    
    fun hasNext(): Boolean = index < args.size

    fun next(): String = if (hasNext()) {
        args[index++]
    } else {
        throw NoSuchElementException("No more arguments")
    }
    
    fun peek(): String? = if (hasNext()) args[index] else null
    
    fun hasOption(option: String): Boolean {
        for (a in args) {
            // If this is a long option (starts with "--"), match case-insensitively.
            if (option.startsWith("--")) {
                if (a.equals(option, ignoreCase = true)) return true
                if (a.startsWith(option + "=", ignoreCase = true)) return true
            } else {
                if (a == option) return true
                if (a.startsWith(option + "=", ignoreCase = false)) return true
            }
            // support combined short flags like "-abc" matching "-a", "-b", "-c"
            if (option.length == 2 && option.startsWith("-") && a.startsWith("-") && !a.startsWith("--") ) {
                // examine characters after the leading '-' up to an optional '='
                val body = a.substring(1).substringBefore('=')
                if (body.any { it == option[1] }) return true
            }
        }
        return false
    }
    
    fun getOptionValue(option: String): String? {
        // Scan arguments in order so we return the first occurrence whether it's
        // in the form "--opt value" or "--opt=value".
        for (i in args.indices) {
            val a = args[i]
            // Long options should match case-insensitively
            if (option.startsWith("--")) {
                if (a.equals(option, ignoreCase = true)) {
                    return if (i + 1 < args.size) args[i + 1] else null
                }
                if (a.startsWith(option + "=", ignoreCase = true)) {
                    return a.substringAfter('=')
                }
            } else {
                if (a == option) {
                    return if (i + 1 < args.size) args[i + 1] else null
                }
                // support --opt=value and -o=value style (case-sensitive for short options)
                if (a.startsWith(option + "=", ignoreCase = false)) {
                    return a.substringAfter('=')
                }
            }
            // support short-option attached values like "-ovalue"
            if (option.length == 2 && option.startsWith("-") && a.startsWith(option) && !a.startsWith("--")) {
                // if arg is exactly the option (handled above) we won't be here;
                // otherwise treat the remainder as the attached value
                if (a.length > option.length) return a.substring(option.length)
            }
        }
        return null
    }
    
    fun getIntOptionValue(option: String, default: Int? = null): Int? {
        return getOptionValue(option)?.toIntOrNull() ?: default
    }

    // Convenience overload when a non-null default is provided so callers don't
    // need to handle nullable Ints.
    fun getIntOptionValue(option: String, default: Int): Int {
        return getOptionValue(option)?.toIntOrNull() ?: default
    }
}
