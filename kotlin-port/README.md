# Flameshot Kotlin Port

This directory contains a Kotlin Multiplatform port of Flameshot with initial support for macOS.

## Architecture

The Kotlin port follows a clean architecture approach:

- **`src/commonMain/kotlin/`** - Platform-agnostic code shared between all targets
  - Core application logic (`Flameshot` class)
  - Data models (`CaptureRequest`, `Rectangle`)
  - Configuration handling (`ConfigHandler`)

- **`src/macosMain/kotlin/`** - macOS-specific implementations
  - Native screenshot capture using CoreGraphics APIs
  - NSUserDefaults for configuration persistence 
  - macOS-specific UI and system integration

## Features Implemented

### Core Functionality
- ✅ Basic screenshot capture request handling
- ✅ Configuration management with JSON serialization
- ✅ Command-line interface with kotlinx-cli
- ✅ Capture modes: GUI, fullscreen, specific screen
- ✅ Export tasks: save to file, copy to clipboard

### macOS Platform Support
- ✅ macOS-specific Flameshot implementation
- ✅ NSUserDefaults configuration persistence
- ✅ NSPasteboard clipboard integration
- ✅ NSWorkspace file system integration
- 🚧 CoreGraphics screenshot capture (stubbed)
- 🚧 Native macOS UI components (planned)

## Building

### Prerequisites
- Kotlin/Native toolchain (included with Kotlin 1.9.20+)
- macOS 10.15+ (for macOS targets)

### Build Commands
```bash
cd kotlin-port

# Build for macOS x64
./gradlew macosX64Binaries

# Build for macOS ARM64 (Apple Silicon)  
./gradlew macosArm64Binaries

# Run on current macOS architecture
./gradlew macosX64Run    # or macosArm64Run
```

## Usage

The Kotlin port supports the same command-line interface as the original:

```bash
# GUI capture mode (default)
./flameshot-kotlin -g

# Fullscreen capture
./flameshot-kotlin -f

# Capture specific screen
./flameshot-kotlin -s 0

# Save to specific path
./flameshot-kotlin -f -p ~/Desktop/screenshot.png

# Copy to clipboard
./flameshot-kotlin -f -c

# Show configuration
./flameshot-kotlin --config

# Show version
./flameshot-kotlin -v
```

## Project Status

This is a **proof-of-concept** port demonstrating:
1. How Flameshot's core functionality can be implemented in Kotlin Common
2. Platform-specific macOS integration using Kotlin/Native
3. Modern, type-safe configuration management
4. Clean separation of concerns between common and platform code

### Next Steps
1. Complete CoreGraphics screenshot capture implementation
2. Add native macOS UI components (NSWindow, NSView)
3. Implement global hotkey support
4. Add comprehensive error handling
5. Expand to additional platforms (Linux, Windows)

## Comparison with Original

| Feature | Original (C++/Qt) | Kotlin Port |
|---------|------------------|-------------|
| Screenshot capture | ✅ Full implementation | 🚧 Basic structure |
| GUI | ✅ Qt widgets | 🚧 Planned native UI |
| Configuration | ✅ QSettings | ✅ JSON + platform storage |
| Hotkeys | ✅ QHotkey | 🚧 Planned |
| Multi-platform | ✅ Qt framework | ✅ Kotlin Multiplatform |
| Type safety | ⚠️ C++ | ✅ Kotlin strong typing |
| Memory safety | ⚠️ Manual management | ✅ Automatic GC |

## License

Same as original Flameshot: GPL-3.0-or-later