package org.flameshot

object SimpleLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        // minimal no-op launcher for JVM smoke tests
        if (args.isNotEmpty() && args[0] == "--version") {
            println("flameshot-kotlin stub version")
        }
    }
}
