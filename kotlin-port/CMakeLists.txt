# Kotlin port integration for Flameshot

if(ENABLE_KOTLIN_PORT AND APPLE)
    message(STATUS "Configuring Kotlin/Native port for macOS")
    
    # Find Kotlin/Native compiler
    find_program(KOTLINC_NATIVE kotlinc-native)
    
    if(KOTLINC_NATIVE)
        message(STATUS "Found Kotlin/Native compiler: ${KOTLINC_NATIVE}")
        
        # Define Kotlin source files
        set(KOTLIN_COMMON_SOURCES
            ${CMAKE_CURRENT_SOURCE_DIR}/kotlin-port/src/commonMain/kotlin/org/flameshot/core/CaptureRequest.kt
            ${CMAKE_CURRENT_SOURCE_DIR}/kotlin-port/src/commonMain/kotlin/org/flameshot/core/Flameshot.kt
            ${CMAKE_CURRENT_SOURCE_DIR}/kotlin-port/src/commonMain/kotlin/org/flameshot/config/ConfigHandler.kt
        )
        
        set(KOTLIN_MACOS_SOURCES
            ${CMAKE_CURRENT_SOURCE_DIR}/kotlin-port/src/macosMain/kotlin/org/flameshot/Main.kt
            ${CMAKE_CURRENT_SOURCE_DIR}/kotlin-port/src/macosMain/kotlin/org/flameshot/platform/MacOSFlameshot.kt
            ${CMAKE_CURRENT_SOURCE_DIR}/kotlin-port/src/macosMain/kotlin/org/flameshot/platform/MacOSConfig.kt
        )
        
        # Create Kotlin build target
        add_custom_target(flameshot-kotlin
            COMMAND ${KOTLINC_NATIVE}
                -target macos_x64
                -produce executable  
                -output ${CMAKE_BINARY_DIR}/flameshot-kotlin
                -entry org.flameshot.main
                ${KOTLIN_COMMON_SOURCES}
                ${KOTLIN_MACOS_SOURCES}
            COMMENT "Building Flameshot Kotlin/Native port"
            DEPENDS ${KOTLIN_COMMON_SOURCES} ${KOTLIN_MACOS_SOURCES}
        )
        
        # Install the Kotlin binary
        install(PROGRAMS ${CMAKE_BINARY_DIR}/flameshot-kotlin
                DESTINATION bin
                COMPONENT kotlin-port)
        
    else()
        message(WARNING "Kotlin/Native compiler not found - skipping Kotlin port")
        message(STATUS "To install Kotlin/Native:")
        message(STATUS "  brew install kotlin")
        message(STATUS "  or download from https://kotlinlang.org/")
    endif()
    
elseif(ENABLE_KOTLIN_PORT)
    message(STATUS "Kotlin port currently only supports macOS")
endif()