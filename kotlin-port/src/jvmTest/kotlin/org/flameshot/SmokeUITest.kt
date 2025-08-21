package org.flameshot

import kotlin.test.Test

class SmokeUITest {
    @Test
    fun smokeBuildRuns() {
        // Minimal smoke: instantiate core class and call a pure function to ensure JVM target compiles and runs
        val args = arrayOf("--version")
        // Ensure SimpleLauncher can be loaded (non-UI entry)
        SimpleLauncher.main(args)
    }
}
