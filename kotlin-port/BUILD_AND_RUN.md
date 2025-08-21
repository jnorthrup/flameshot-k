# Build and run (kotlin-port)

This file explains how to build the Compose desktop artifact and run the produced fat JAR locally.

Prerequisites

- JDK 17+ installed (or set JDK_VERSION environment variable used by the Gradle toolchain in `build.gradle.kts`).
- On Linux CI or headless environments, install Xvfb for UI tests if needed.

Build the desktop artifact (fat JAR)

```bash
cd kotlin-port
chmod +x ../gradlew
../gradlew clean :kotlin-port:desktopFatJar
```

The fat JAR will be produced under `kotlin-port/build/libs/` and will be named like `flameshot-kotlin-desktop-<version>.jar`.

Run the desktop JAR

```bash
java -jar kotlin-port/build/libs/flameshot-kotlin-desktop-13.1.0.jar
```

If your environment is headless (CI), run UI tests under Xvfb:

```bash
# Debian/Ubuntu example
sudo apt-get update && sudo apt-get install -y xvfb
xvfb-run -s "-screen 0 1280x1024x24" ../gradlew :kotlin-port:connectedCheck
```

Notes

- The Compose for Desktop runtime will be included in the fat JAR via the `desktopFatJar` task where possible. If you prefer platform-native distributions (DMG, AppImage), configure the `org.jetbrains.compose.desktop.application` plugin section in `build.gradle.kts`.
