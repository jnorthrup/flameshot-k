import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    kotlin("multiplatform") version "2.2.10"
    // Required for Kotlin 2.x + Compose Multiplatform (per migration guide)
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
    id("org.jetbrains.compose") version "1.8.2"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
}

group = "org.flameshot"
version = "13.1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    // Add JVM target so common tests can run on the JVM during TDD iterations
    // Configure JVM toolchain at the kotlin extension level (affects all JVM targets).
    jvmToolchain((System.getenv("JDK_VERSION") ?: "21").toInt())

    jvm("jvm") {
        // JVM target configured via kotlin.jvmToolchain(...)
    }

    // Ensure Kotlin compilation targets JVM 11 for Compose compatibility
    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
        // Use the newer compilerOptions DSL required by Kotlin 2.x
        this.compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    // macOS Kotlin/Native targets for Apple Silicon and Intel macOS
    macosArm64("macosArm64")
    macosX64("macosX64")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
                // Compose multiplatform UI and Material3 used by the kotlin UI sources
                implementation(compose.ui)
                implementation(compose.material3)
            }
        }

        val jvmMain by getting {
            dependencies {
                // Enable Compose Desktop for packaging and UI runtime on JVM
                implementation(compose.desktop.currentOs)
            }
        }

        // macOS native source set (shared by both arm64 and x64 targets)
        val macosMain by creating {
            dependsOn(commonMain)
            dependencies {
                // native-specific dependencies can be added here later
            }
        }

        // wire macos target source sets to the created macosMain
        val macosArm64Main by getting { dependsOn(macosMain) }
        val macosX64Main by getting { dependsOn(macosMain) }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmTest by getting {
            dependencies {
                // Use JUnit Jupiter on the JVM test source set
                implementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
            }
        }
    }
}

// Simple packaging task: assemble a fat JAR for desktop consumption when compose desktop is used
tasks.register<Jar>("desktopFatJar") {
    archiveBaseName.set("flameshot-kotlin-desktop")
    archiveVersion.set(project.version.toString())
    from(sourceSets.getByName("jvmMain").output)
    dependsOn("jvmJar")
    // include runtime dependencies
    val runtimeClasspath = configurations.getByName("jvmRuntimeClasspath")
    from({ runtimeClasspath.filter { it.name.endsWith(".jar") }.map { zipTree(it) } })
    manifest {
        attributes(mapOf("Main-Class" to "org.flameshot.ui.SimpleLauncher"))
    }
}


// Configure Ben Manes dependency updates report
// Configure the existing dependencyUpdates task instead of registering a duplicate
tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    checkForGradleUpdate = true
    outputFormatter = "plain"
    outputDir = "${buildDir}/dependencyUpdates"
    reportfileName = "report"

    // Reject candidate versions that are non-stable when current is stable
    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA")
        val isStable = stableKeyword.any { version.uppercase().contains(it) } || "^[0-9,.v-]+$".toRegex().matches(version)
        return !isStable
    }

    rejectVersionIf {
        val candidate = candidate.version
        isNonStable(candidate) && !isNonStable(currentVersion)
    }
}
