import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    kotlin("multiplatform") version "1.9.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
}

group = "org.flameshot"
version = "13.1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

kotlin {
    // macOS targets
    macosX64("macosX64") {
        binaries {
            executable {
                entryPoint = "org.flameshot.main"
            }
        }
    }
    macosArm64("macosArm64") {
        binaries {
            executable {
                entryPoint = "org.flameshot.main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val macosMain by creating {
            dependsOn(commonMain)
        }
        
        val macosX64Main by getting {
            dependsOn(macosMain)
        }
        
        val macosArm64Main by getting {
            dependsOn(macosMain)
        }
        
        val macosTest by creating {
            dependsOn(commonTest)
        }
        
        val macosX64Test by getting {
            dependsOn(macosTest)
        }
        
        val macosArm64Test by getting {
            dependsOn(macosTest)
        }
    }
}