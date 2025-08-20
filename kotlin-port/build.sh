#!/bin/bash

echo "Building Flameshot Kotlin port..."

# Simple build script for macOS
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "Detected macOS - building native binary"
    
    # Check if Kotlin/Native is available
    if ! command -v kotlinc-native &> /dev/null; then
        echo "Error: Kotlin/Native not found. Please install Kotlin/Native compiler."
        echo "You can install it via:"
        echo "  brew install kotlin" 
        echo "  or download from https://kotlinlang.org/docs/native-get-started.html"
        exit 1
    fi
    
    echo "Compiling Kotlin sources..."
    
    # Create output directory
    mkdir -p build/bin
    
    # Compile for macOS
    kotlinc-native \
        -target macos_x64 \
        -produce executable \
        -output build/bin/flameshot-kotlin \
        -entry org.flameshot.main \
        src/commonMain/kotlin/org/flameshot/core/*.kt \
        src/commonMain/kotlin/org/flameshot/config/*.kt \
        src/macosMain/kotlin/org/flameshot/*.kt \
        src/macosMain/kotlin/org/flameshot/platform/*.kt
        
    if [ $? -eq 0 ]; then
        echo "Build successful! Executable created at: build/bin/flameshot-kotlin"
        echo ""
        echo "Usage examples:"
        echo "  ./build/bin/flameshot-kotlin --help"
        echo "  ./build/bin/flameshot-kotlin -g    # GUI mode"
        echo "  ./build/bin/flameshot-kotlin -f    # Fullscreen"
    else
        echo "Build failed!"
        exit 1
    fi
else
    echo "Error: This build script currently only supports macOS"
    echo "Please use the Gradle build for other platforms"
    exit 1
fi