# Flameshot Qt C++ to Kotlin Compose Multiplatform Transcription Guide

## Overview
This guide provides comprehensive mappings and patterns for translating the Flameshot Qt C++ application to Kotlin Compose Multiplatform.

## Architecture Translation

### 1. Core Application Structure

#### Qt Application Model → KMP Application
```kotlin
// Qt: QApplication with event loop
// KMP equivalent:
object FlameshotApp {
    @Composable
    fun App() {
        val coroutineScope = rememberCoroutineScope()
        val appState = remember { FlameshotAppState() }
        
        CompositionLocalProvider(
            LocalAppState provides appState,
            LocalCoroutineScope provides coroutineScope
        ) {
            FlameshotTheme {
                MainWindow()
            }
        }
    }
}
```

### 2. Signal-Slot Mechanism → Kotlin Coroutines & State

#### Qt Signals/Slots
```cpp
// Qt Pattern
connect(flameshot, &Flameshot::captureTaken, [&](const QPixmap&) { 
    // handle capture 
});
```

#### Kotlin Compose Pattern
```kotlin
// Using SharedFlow for events
class FlameshotViewModel : ViewModel() {
    private val _captureTaken = MutableSharedFlow<Screenshot>()
    val captureTaken: SharedFlow<Screenshot> = _captureTaken.asSharedFlow()
    
    // Using State for UI state
    var captureState by mutableStateOf<CaptureState>(CaptureState.Idle)
        private set
    
    fun captureScreen() {
        viewModelScope.launch {
            captureState = CaptureState.Capturing
            val screenshot = performCapture()
            _captureTaken.emit(screenshot)
            captureState = CaptureState.Completed(screenshot)
        }
    }
}
```

### 3. Widget Hierarchy → Composable Functions

#### Main Window Translation
```kotlin
@Composable
fun MainWindow() {
    Scaffold(
        topBar = { FlameshotMenuBar() },
        content = { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (LocalAppState.current.mode) {
                    AppMode.Capture -> CaptureScreen()
                    AppMode.Config -> ConfigurationScreen()
                    AppMode.Launcher -> LauncherScreen()
                }
            }
        }
    )
}
```

### 4. Qt Widgets → Compose Components Mapping

| Qt Widget | Compose Equivalent | Notes |
|-----------|-------------------|-------|
| QMainWindow | Scaffold | Use with TopAppBar for menus |
| QWidget | Box/Surface | Base container |
| QPushButton | Button | With onClick lambda |
| QLabel | Text | For static text |
| QLineEdit | TextField | With value state |
| QComboBox | DropdownMenu | With expanded state |
| QCheckBox | Checkbox | With checked state |
| QSlider | Slider | With value state |
| QSpinBox | TextField + validation | Custom implementation |
| QColorDialog | Custom ColorPicker | Build with Canvas |
| QMenuBar | TopAppBar + DropdownMenu | Custom menu structure |
| QToolBar | Row of IconButtons | Horizontal arrangement |
| QDockWidget | NavigationRail/Drawer | Side panels |
| QTabWidget | TabRow + content | Tab navigation |
| QListWidget | LazyColumn | Virtual scrolling |
| QTreeWidget | Custom Tree composable | Recursive structure |
| QTableWidget | LazyVerticalGrid | Grid layout |
| QScrollArea | verticalScroll modifier | Scrollable content |
| QGroupBox | Card | Grouped content |
| QSplitter | Custom with draggable divider | Manual implementation |
| QStatusBar | BottomAppBar | Status information |

### 5. Drawing and Painting → Skiko Canvas

#### Qt QPainter Translation
```kotlin
// Qt: QPainter for custom drawing
// Compose + Skiko equivalent:

@Composable
fun DrawingCanvas(
    tools: List<DrawingTool>,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf<Path?>(null) }
    val paths = remember { mutableStateListOf<DrawingPath>() }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPath = Path().apply {
                            moveTo(offset.x, offset.y)
                        }
                    },
                    onDrag = { _, dragAmount ->
                        currentPath?.let { path ->
                            path.lineTo(
                                path.lastPoint.x + dragAmount.x,
                                path.lastPoint.y + dragAmount.y
                            )
                        }
                    },
                    onDragEnd = {
                        currentPath?.let { paths.add(DrawingPath(it, currentTool)) }
                        currentPath = null
                    }
                )
            }
    ) {
        // Draw all completed paths
        paths.forEach { drawingPath ->
            drawPath(
                path = drawingPath.path,
                color = drawingPath.tool.color,
                style = Stroke(width = drawingPath.tool.width.dp.toPx())
            )
        }
        
        // Draw current path being drawn
        currentPath?.let { path ->
            drawPath(
                path = path,
                color = currentTool.color,
                style = Stroke(width = currentTool.width.dp.toPx())
            )
        }
    }
}
```

### 6. Screenshot Capture → Platform-Specific Implementation

#### Common Interface
```kotlin
// Common code
expect class ScreenshotCapture {
    suspend fun captureScreen(screenIndex: Int = -1): Screenshot
    suspend fun captureRegion(x: Int, y: Int, width: Int, height: Int): Screenshot
    fun getScreenCount(): Int
    fun getScreenGeometry(index: Int): Rectangle
}

// Platform-specific implementations
// macOS (using native APIs via cinterop)
actual class ScreenshotCapture {
    actual suspend fun captureScreen(screenIndex: Int): Screenshot {
        return withContext(Dispatchers.IO) {
            // Use CGWindowListCreateImage for macOS
            val image = CGWindowListCreateImage(
                CGRectNull,
                kCGWindowListOptionOnScreenOnly,
                kCGNullWindowID,
                kCGWindowImageDefault
            )
            Screenshot(convertCGImageToByteArray(image))
        }
    }
}

// JVM (using Robot API)
actual class ScreenshotCapture {
    actual suspend fun captureScreen(screenIndex: Int): Screenshot {
        return withContext(Dispatchers.IO) {
            val robot = Robot()
            val screenDevice = if (screenIndex >= 0) {
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .screenDevices[screenIndex]
            } else {
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .defaultScreenDevice
            }
            val bounds = screenDevice.defaultConfiguration.bounds
            val bufferedImage = robot.createScreenCapture(bounds)
            Screenshot(bufferedImageToByteArray(bufferedImage))
        }
    }
}
```

### 7. Tool System Translation

#### Base Tool Interface
```kotlin
interface CaptureTool {
    val type: ToolType
    val icon: DrawableResource
    val name: String
    val cursor: CursorType
    
    fun isValid(): Boolean
    fun isSelectable(): Boolean
    fun useMousePreview(): Boolean
    
    // Drawing operations
    fun onDrawStart(point: Offset, context: DrawContext)
    fun onDrawMove(point: Offset, context: DrawContext)
    fun onDrawEnd(point: Offset, context: DrawContext)
    
    // Rendering
    fun render(canvas: DrawScope, context: DrawContext)
}

// Example: Arrow Tool Implementation
class ArrowTool : CaptureTool {
    override val type = ToolType.ARROW
    override val icon = Res.drawable.arrow_icon
    override val name = "Arrow"
    
    private var startPoint: Offset? = null
    private var endPoint: Offset? = null
    
    override fun onDrawStart(point: Offset, context: DrawContext) {
        startPoint = point
        endPoint = point
    }
    
    override fun onDrawMove(point: Offset, context: DrawContext) {
        endPoint = point
    }
    
    override fun render(canvas: DrawScope, context: DrawContext) {
        val start = startPoint ?: return
        val end = endPoint ?: return
        
        canvas.drawArrow(
            start = start,
            end = end,
            color = context.drawColor,
            strokeWidth = context.strokeWidth
        )
    }
}
```

### 8. Configuration Management

#### Qt QSettings → Kotlin Serialization + Platform Storage
```kotlin
@Serializable
data class FlameshotConfig(
    val general: GeneralConfig = GeneralConfig(),
    val shortcuts: ShortcutConfig = ShortcutConfig(),
    val ui: UIConfig = UIConfig(),
    val tools: ToolConfig = ToolConfig()
)

@Serializable
data class GeneralConfig(
    val showTrayIcon: Boolean = true,
    val startupLaunch: Boolean = false,
    val showDesktopNotification: Boolean = true,
    val savePath: String = "",
    val filenamePattern: String = "%F_%T",
    val copyPathAfterSave: Boolean = false
)

// Platform-specific storage
expect class ConfigStorage {
    fun load(): FlameshotConfig
    fun save(config: FlameshotConfig)
}

// macOS implementation using UserDefaults
actual class ConfigStorage {
    actual fun load(): FlameshotConfig {
        val defaults = NSUserDefaults.standardUserDefaults
        val jsonString = defaults.stringForKey("flameshot_config") ?: return FlameshotConfig()
        return Json.decodeFromString(jsonString)
    }
    
    actual fun save(config: FlameshotConfig) {
        val defaults = NSUserDefaults.standardUserDefaults
        val jsonString = Json.encodeToString(config)
        defaults.setObject(jsonString, forKey = "flameshot_config")
        defaults.synchronize()
    }
}
```

### 9. Event Handling Translation

#### Qt Events → Compose Modifiers
```kotlin
@Composable
fun CaptureWidget(
    onCapture: (Rectangle) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectionStart by remember { mutableStateOf<Offset?>(null) }
    var selectionEnd by remember { mutableStateOf<Offset?>(null) }
    var isSelecting by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selectionStart = offset
                        selectionEnd = offset
                        isSelecting = true
                    },
                    onDrag = { _, _ ->
                        selectionEnd = currentPointer
                    },
                    onDragEnd = {
                        isSelecting = false
                        val start = selectionStart ?: return@detectDragGestures
                        val end = selectionEnd ?: return@detectDragGestures
                        onCapture(Rectangle.fromPoints(start, end))
                    }
                )
            }
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.Escape -> {
                        onCancel()
                        true
                    }
                    keyEvent.key == Key.Enter -> {
                        onAccept()
                        true
                    }
                    else -> false
                }
            }
    ) {
        // Selection overlay
        if (isSelecting) {
            SelectionOverlay(
                start = selectionStart,
                end = selectionEnd
            )
        }
    }
}
```

### 10. System Tray Integration

```kotlin
// Platform-specific system tray
expect class SystemTray {
    fun show()
    fun hide()
    fun setMenu(items: List<TrayMenuItem>)
    fun showNotification(title: String, message: String)
}

// Common tray menu structure
data class TrayMenuItem(
    val label: String,
    val icon: DrawableResource? = null,
    val shortcut: KeyboardShortcut? = null,
    val action: () -> Unit
)

// Usage in Compose
@Composable
fun FlameshotTray() {
    val tray = remember { SystemTray() }
    
    LaunchedEffect(Unit) {
        tray.setMenu(
            listOf(
                TrayMenuItem("Capture", Res.drawable.capture) { 
                    FlameshotApp.instance.gui()
                },
                TrayMenuItem("Launcher", Res.drawable.launcher) {
                    FlameshotApp.instance.launcher()
                },
                TrayMenuItem("Configuration", Res.drawable.config) {
                    FlameshotApp.instance.config()
                },
                TrayMenuItem("Quit", Res.drawable.quit) {
                    exitApplication()
                }
            )
        )
        tray.show()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            tray.hide()
        }
    }
}
```

### 11. File Operations

```kotlin
// Common file operations interface
expect class FileOperations {
    suspend fun saveImage(screenshot: Screenshot, path: String): Boolean
    suspend fun loadImage(path: String): Screenshot?
    fun showSaveDialog(defaultName: String): String?
    fun showOpenDialog(): String?
    fun openInExternalApp(path: String)
}

// Usage in Compose
@Composable
fun SaveButton(screenshot: Screenshot) {
    val fileOps = LocalFileOperations.current
    val coroutineScope = rememberCoroutineScope()
    
    Button(
        onClick = {
            coroutineScope.launch {
                val path = fileOps.showSaveDialog("screenshot.png")
                if (path != null) {
                    val saved = fileOps.saveImage(screenshot, path)
                    if (saved) {
                        showNotification("Screenshot saved to $path")
                    }
                }
            }
        }
    ) {
        Text("Save")
    }
}
```

### 12. Undo/Redo System

```kotlin
class UndoRedoManager<T> {
    private val undoStack = mutableListOf<T>()
    private val redoStack = mutableListOf<T>()
    private var currentState: T? = null
    
    fun push(state: T) {
        currentState?.let { undoStack.add(it) }
        currentState = state
        redoStack.clear()
    }
    
    fun undo(): T? {
        if (undoStack.isEmpty()) return null
        val previous = undoStack.removeLast()
        currentState?.let { redoStack.add(it) }
        currentState = previous
        return previous
    }
    
    fun redo(): T? {
        if (redoStack.isEmpty()) return null
        val next = redoStack.removeLast()
        currentState?.let { undoStack.add(it) }
        currentState = next
        return next
    }
    
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
}

// Usage in drawing context
@Composable
fun DrawingCanvas() {
    val undoRedoManager = remember { UndoRedoManager<DrawingState>() }
    var drawingState by remember { mutableStateOf(DrawingState()) }
    
    // After each drawing operation
    LaunchedEffect(drawingState) {
        undoRedoManager.push(drawingState.copy())
    }
    
    // Undo/Redo buttons
    Row {
        IconButton(
            onClick = { 
                undoRedoManager.undo()?.let { drawingState = it }
            },
            enabled = undoRedoManager.canUndo
        ) {
            Icon(Icons.Default.Undo, "Undo")
        }
        
        IconButton(
            onClick = {
                undoRedoManager.redo()?.let { drawingState = it }
            },
            enabled = undoRedoManager.canRedo
        ) {
            Icon(Icons.Default.Redo, "Redo")
        }
    }
}
```

### 13. Upload Management

```kotlin
interface ImageUploader {
    suspend fun upload(screenshot: Screenshot): UploadResult
    val name: String
    val icon: DrawableResource
}

sealed class UploadResult {
    data class Success(val url: String, val deleteUrl: String?) : UploadResult()
    data class Error(val message: String) : UploadResult()
}

class ImgurUploader : ImageUploader {
    override val name = "Imgur"
    override val icon = Res.drawable.imgur
    
    override suspend fun upload(screenshot: Screenshot): UploadResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = httpClient.post("https://api.imgur.com/3/image") {
                    headers {
                        append("Authorization", "Client-ID $clientId")
                    }
                    setBody(MultiPartFormDataContent(
                        formData {
                            append("image", screenshot.toBase64())
                            append("type", "base64")
                        }
                    ))
                }
                
                val data = response.body<ImgurResponse>()
                UploadResult.Success(data.link, data.deletehash)
            } catch (e: Exception) {
                UploadResult.Error(e.message ?: "Upload failed")
            }
        }
    }
}
```

### 14. Color Picker Implementation

```kotlin
@Composable
fun ColorPicker(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableStateOf(currentColor.toHsv().hue) }
    var saturation by remember { mutableStateOf(currentColor.toHsv().saturation) }
    var value by remember { mutableStateOf(currentColor.toHsv().value) }
    
    Column(modifier = modifier.padding(16.dp)) {
        // Hue selector
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        hue = (change.position.x / size.width * 360f).coerceIn(0f, 360f)
                    }
                }
        ) {
            // Draw hue gradient
            for (i in 0..360) {
                drawLine(
                    color = Color.hsv(i.toFloat(), 1f, 1f),
                    start = Offset(i * size.width / 360f, 0f),
                    end = Offset(i * size.width / 360f, size.height)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Saturation/Value selector
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        saturation = (offset.x / size.width).coerceIn(0f, 1f)
                        value = (1f - offset.y / size.height).coerceIn(0f, 1f)
                        onColorSelected(Color.hsv(hue, saturation, value))
                    }
                }
        ) {
            // Draw saturation/value gradient
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White,
                        Color.hsv(hue, 1f, 1f)
                    )
                )
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black
                    )
                )
            )
        }
    }
}
```

### 15. Magnifier Widget

```kotlin
@Composable
fun MagnifierWidget(
    screenshot: Screenshot,
    position: Offset,
    magnification: Float = 2f,
    size: Dp = 150.dp,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, Color.White, CircleShape)
    ) {
        val sourceRect = Rect(
            offset = Offset(
                position.x - size.toPx() / magnification / 2,
                position.y - size.toPx() / magnification / 2
            ),
            size = Size(
                size.toPx() / magnification,
                size.toPx() / magnification
            )
        )
        
        // Draw magnified portion
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.scale(magnification, magnification)
            canvas.translate(
                -sourceRect.left,
                -sourceRect.top
            )
            screenshot.draw(canvas.nativeCanvas)
            canvas.restore()
        }
        
        // Draw crosshair
        drawLine(
            color = Color.Red,
            start = Offset(size.toPx() / 2, 0f),
            end = Offset(size.toPx() / 2, size.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.Red,
            start = Offset(0f, size.toPx() / 2),
            end = Offset(size.toPx(), size.toPx() / 2),
            strokeWidth = 1.dp.toPx()
        )
    }
}
```

## Project Structure

```
kotlin-port/
├── src/
│   ├── commonMain/
│   │   └── kotlin/org/flameshot/
│   │       ├── core/
│   │       │   ├── Flameshot.kt
│   │       │   ├── CaptureRequest.kt
│   │       │   └── Screenshot.kt
│   │       ├── ui/
│   │       │   ├── MainWindow.kt
│   │       │   ├── CaptureScreen.kt
│   │       │   ├── ConfigScreen.kt
│   │       │   └── LauncherScreen.kt
│   │       ├── tools/
│   │       │   ├── CaptureTool.kt
│   │       │   ├── ArrowTool.kt
│   │       │   ├── RectangleTool.kt
│   │       │   └── TextTool.kt
│   │       ├── widgets/
│   │       │   ├── ColorPicker.kt
│   │       │   ├── MagnifierWidget.kt
│   │       │   └── SelectionWidget.kt
│   │       └── utils/
│   │           ├── ConfigHandler.kt
│   │           ├── FileOperations.kt
│   │           └── ImageUtils.kt
│   ├── jvmMain/
│   │   └── kotlin/org/flameshot/
│   │       ├── JvmMain.kt
│   │       └── platform/
│   │           ├── JvmScreenCapture.kt
│   │           └── JvmFileOperations.kt
│   └── macosMain/
│       └── kotlin/org/flameshot/
│           ├── MacOSMain.kt
│           └── platform/
│               ├── MacOSScreenCapture.kt
│               └── MacOSFileOperations.kt
└── build.gradle.kts
```

## Implementation Priority

1. **Phase 1: Core Infrastructure**
   - Basic application structure
   - Screenshot capture (platform-specific)
   - Basic UI with capture mode

2. **Phase 2: Drawing Tools**
   - Canvas implementation with Skiko
   - Basic drawing tools (rectangle, arrow, pencil)
   - Color picker

3. **Phase 3: Advanced Features**
   - Text tool with font selection
   - Undo/redo system
   - Configuration persistence

4. **Phase 4: Platform Integration**
   - System tray
   - Global hotkeys
   - File operations

5. **Phase 5: Polish**
   - Animations and transitions
   - Performance optimization
   - Upload services

## Testing Strategy

```kotlin
// Common test for tools
class CaptureToolTest {
    @Test
    fun testArrowToolDrawing() {
        val tool = ArrowTool()
        val context = DrawContext()
        
        tool.onDrawStart(Offset(0f, 0f), context)
        tool.onDrawMove(Offset(100f, 100f), context)
        tool.onDrawEnd(Offset(100f, 100f), context)
        
        assertTrue(tool.isValid())
    }
}
```

## Platform-Specific Considerations

### macOS
- Use CGWindowListCreateImage for screenshot capture
- NSUserDefaults for configuration storage
- NSStatusItem for system tray

### JVM/Desktop
- Robot API for screenshot capture
- Preferences API for configuration storage
- SystemTray API for tray icon

## Performance Optimizations

1. Use `remember` and `derivedStateOf` for expensive computations
2. Implement virtual scrolling for tool lists
3. Use `Canvas` with caching for complex drawings
4. Optimize recomposition with `@Stable` and `@Immutable` annotations
5. Use coroutines with appropriate dispatchers for I/O operations

This guide provides the foundation for translating Flameshot from Qt C++ to Kotlin Compose Multiplatform while maintaining feature parity and improving the codebase with modern Kotlin patterns.