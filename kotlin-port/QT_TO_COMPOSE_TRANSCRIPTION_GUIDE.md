# Qt C++ to Kotlin Compose Multiplatform Transcription Guide

## 1. Core Qt → Compose Mappings

### Widget Hierarchy Translation

```kotlin
// Qt: QWidget inheritance hierarchy
class CaptureWidget : public QWidget {
    Q_OBJECT
    // ...
};

// Compose: Composable function
@Composable
fun CaptureWidget(
    captureRequest: CaptureRequest,
    modifier: Modifier = Modifier,
    onCaptureDone: (Screenshot) -> Unit = {},
    onCancelled: () -> Unit = {}
) {
    // State management replaces Qt's member variables
    var selection by remember { mutableStateOf(Rectangle()) }
    var currentTool by remember { mutableStateOf<CaptureTool?>(null) }
    var toolObjects by remember { mutableStateOf(listOf<ToolObject>()) }
    
    // Compose UI hierarchy
    Box(modifier = modifier.fillMaxSize()) {
        ScreenshotCanvas(
            screenshot = screenshot,
            selection = selection,
            toolObjects = toolObjects,
            currentTool = currentTool,
            modifier = Modifier.fillMaxSize()
        )
        
        ToolPanel(
            tools = availableTools,
            currentTool = currentTool,
            onToolSelected = { currentTool = it },
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
```

### QPainter/QPaintDevice → Skiko Canvas

```kotlin
// Qt: QPainter drawing
void PencilTool::process(QPainter& painter, const QPixmap& pixmap) {
    painter.setPen(QPen(m_color, m_thickness));
    painter.drawPath(m_path);
}

// Compose with Skiko:
@Composable
fun DrawingCanvas(
    toolObjects: List<ToolObject>,
    currentPath: Path?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Direct Skiko access for complex drawing
        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas as org.jetbrains.skia.Canvas
            
            toolObjects.forEach { toolObject ->
                when (toolObject) {
                    is PencilObject -> {
                        val paint = Paint().apply {
                            color = toolObject.color.toArgb()
                            strokeWidth = toolObject.thickness.toFloat()
                            style = PaintingStyle.Stroke
                        }
                        nativeCanvas.drawPath(toolObject.path.toSkiaPath(), paint.asFrameworkPaint())
                    }
                    is ArrowObject -> drawArrow(nativeCanvas, toolObject)
                    is TextObject -> drawText(nativeCanvas, toolObject)
                }
            }
        }
    }
}

// Extension to convert Compose Path to Skia Path
fun Path.toSkiaPath(): org.jetbrains.skia.Path {
    val skiaPath = org.jetbrains.skia.Path()
    // Convert path operations
    return skiaPath
}
```

### Qt Layouts → Compose Layout System

```kotlin
// Qt: QVBoxLayout, QHBoxLayout, QGridLayout
auto* layout = new QVBoxLayout(this);
layout->addWidget(button1);
layout->addWidget(button2);
layout->addSpacing(10);

// Compose: Column, Row, Box
@Composable
fun ToolPanel(
    tools: List<CaptureTool>,
    currentTool: CaptureTool?,
    onToolSelected: (CaptureTool) -> Unit
) {
    Column(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tools.forEach { tool ->
            ToolButton(
                tool = tool,
                isSelected = tool == currentTool,
                onClick = { onToolSelected(tool) }
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Additional controls
        ColorPicker(onColorSelected = { /* ... */ })
        SizeSlider(onSizeChanged = { /* ... */ })
    }
}
```

## 2. Architecture Translations

### Qt Signal/Slot → Kotlin StateFlow/Callbacks

```kotlin
// Qt: Signals and slots
signals:
    void colorChanged(const QColor& c);
    void toolSizeChanged(int size);
    
private slots:
    void setDrawColor(const QColor& c);
    void onToolSizeChanged(int size);

// Kotlin: StateFlow and callbacks
class CaptureViewModel {
    private val _drawColor = MutableStateFlow(Color.Red)
    val drawColor: StateFlow<Color> = _drawColor.asStateFlow()
    
    private val _toolSize = MutableStateFlow(5)
    val toolSize: StateFlow<Int> = _toolSize.asStateFlow()
    
    fun setDrawColor(color: Color) {
        _drawColor.value = color
    }
    
    fun setToolSize(size: Int) {
        _toolSize.value = size.coerceIn(1, 50)
    }
}

@Composable
fun CaptureScreen(viewModel: CaptureViewModel = remember { CaptureViewModel() }) {
    val drawColor by viewModel.drawColor.collectAsState()
    val toolSize by viewModel.toolSize.collectAsState()
    
    // UI that reacts to state changes
    DrawingCanvas(
        color = drawColor,
        strokeWidth = toolSize.toFloat()
    )
}
```

### QObject Properties → Compose State

```kotlin
// Qt: Q_PROPERTY with getter/setter
Q_PROPERTY(QColor color READ color WRITE setColor NOTIFY colorChanged)

// Compose: MutableState with derived states
@Composable
fun ColoredWidget() {
    var color by remember { mutableStateOf(Color.Red) }
    
    // Derived state for expensive computations
    val complementaryColor by remember(color) {
        derivedStateOf { 
            Color(
                red = 1f - color.red,
                green = 1f - color.green,
                blue = 1f - color.blue
            )
        }
    }
    
    Box(
        modifier = Modifier
            .background(color)
            .clickable { 
                // Change color on click
                color = Color(
                    Random.nextFloat(),
                    Random.nextFloat(),
                    Random.nextFloat()
                )
            }
    )
}
```

### Qt Event System → Compose Event Handling

```kotlin
// Qt: Mouse and keyboard events
void CaptureWidget::mousePressEvent(QMouseEvent* e) {
    if (e->button() == Qt::LeftButton) {
        m_startPos = e->pos();
    }
}

void CaptureWidget::keyPressEvent(QKeyEvent* e) {
    if (e->key() == Qt::Key_Escape) {
        close();
    }
}

// Compose: Modifier system for events
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InteractiveCanvas(
    onSelectionStart: (Offset) -> Unit,
    onSelectionUpdate: (Offset) -> Unit,
    onSelectionEnd: (Offset) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        onSelectionStart(offset)
                    },
                    onDrag = { _, offset ->
                        onSelectionUpdate(offset)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSelectionEnd(Offset.Zero)
                    }
                )
            }
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyDown -> {
                        // Handle escape
                        true
                    }
                    keyEvent.key == Key.Z && keyEvent.isCtrlPressed -> {
                        // Handle undo
                        true
                    }
                    else -> false
                }
            }
    ) {
        // Canvas content
    }
}
```

### QTimer → Coroutines

```kotlin
// Qt: QTimer for periodic updates
QTimer* timer = new QTimer(this);
connect(timer, &QTimer::timeout, this, &Widget::update);
timer->start(1000);

// Kotlin: Coroutines with Flow
@Composable
fun TimedWidget() {
    var counter by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000)
            counter++
        }
    }
    
    // Or using Flow for more complex timing
    LaunchedEffect(Unit) {
        tickerFlow(1000)
            .collect {
                counter++
            }
    }
}

fun tickerFlow(periodMillis: Long): Flow<Unit> = flow {
    while (currentCoroutineContext().isActive) {
        emit(Unit)
        delay(periodMillis)
    }
}
```

## 3. Key Flameshot Components Translation

### CaptureWidget Implementation

```kotlin
// Complete CaptureWidget translation
@Composable
fun CaptureWidget(
    screenshot: ImageBitmap,
    captureRequest: CaptureRequest,
    modifier: Modifier = Modifier,
    onCaptureDone: (ImageBitmap, Rectangle) -> Unit = { _, _ -> },
    onCancelled: () -> Unit = {}
) {
    // State management
    var selection by remember { mutableStateOf(Rectangle()) }
    var isSelecting by remember { mutableStateOf(false) }
    var currentTool by remember { mutableStateOf<CaptureTool?>(null) }
    val toolObjects = remember { mutableStateListOf<ToolObject>() }
    val undoStack = remember { UndoStack() }
    
    // Tool configuration
    var drawColor by remember { mutableStateOf(Color.Red) }
    var strokeWidth by remember { mutableStateOf(5f) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Main screenshot canvas
        ScreenshotCanvas(
            screenshot = screenshot,
            selection = selection,
            isSelecting = isSelecting,
            toolObjects = toolObjects,
            currentTool = currentTool,
            drawColor = drawColor,
            strokeWidth = strokeWidth,
            onSelectionChange = { newSelection ->
                selection = newSelection
                if (captureRequest.tasks.contains(ExportTask.ACCEPT_ON_SELECT)) {
                    onCaptureDone(screenshot, selection)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Tool panel
        AnimatedVisibility(
            visible = !isSelecting && selection.width > 0,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally()
        ) {
            ToolPanel(
                currentTool = currentTool,
                drawColor = drawColor,
                strokeWidth = strokeWidth,
                onToolSelected = { tool ->
                    currentTool = tool
                    when (tool.type) {
                        ToolType.SAVE -> handleSave(screenshot, selection)
                        ToolType.COPY -> handleCopy(screenshot, selection)
                        ToolType.UNDO -> undoStack.undo()
                        ToolType.REDO -> undoStack.redo()
                        ToolType.EXIT -> onCancelled()
                        else -> { /* Drawing tool selected */ }
                    }
                },
                onColorChanged = { drawColor = it },
                onStrokeWidthChanged = { strokeWidth = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // Magnifier for precision selection
        if (isSelecting) {
            Magnifier(
                screenshot = screenshot,
                position = selection.bottomRight(),
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}
```

### CaptureTool System

```kotlin
// Tool abstraction
sealed class CaptureTool {
    abstract val type: ToolType
    abstract val icon: ImageVector
    abstract val name: String
    abstract val description: String
    
    abstract fun createObject(
        startPoint: Offset,
        endPoint: Offset,
        color: Color,
        strokeWidth: Float
    ): ToolObject
}

// Tool implementations
class PencilTool : CaptureTool() {
    override val type = ToolType.PENCIL
    override val icon = Icons.Default.Edit
    override val name = "Pencil"
    override val description = "Draw freely"
    
    private val path = Path()
    
    override fun createObject(
        startPoint: Offset,
        endPoint: Offset,
        color: Color,
        strokeWidth: Float
    ): ToolObject = PencilObject(path.copy(), color, strokeWidth)
    
    fun addPoint(point: Offset) {
        if (path.isEmpty) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }
}

class ArrowTool : CaptureTool() {
    override val type = ToolType.ARROW
    override val icon = Icons.Default.ArrowForward
    override val name = "Arrow"
    override val description = "Draw an arrow"
    
    override fun createObject(
        startPoint: Offset,
        endPoint: Offset,
        color: Color,
        strokeWidth: Float
    ): ToolObject = ArrowObject(startPoint, endPoint, color, strokeWidth)
}

// Tool objects for rendering
sealed class ToolObject {
    abstract fun draw(canvas: DrawScope)
}

data class PencilObject(
    val path: Path,
    val color: Color,
    val strokeWidth: Float
) : ToolObject() {
    override fun draw(canvas: DrawScope) {
        canvas.drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

data class ArrowObject(
    val start: Offset,
    val end: Offset,
    val color: Color,
    val strokeWidth: Float
) : ToolObject() {
    override fun draw(canvas: DrawScope) {
        // Draw arrow line
        canvas.drawLine(
            start = start,
            end = end,
            color = color,
            strokeWidth = strokeWidth
        )
        
        // Draw arrowhead
        val angle = atan2(end.y - start.y, end.x - start.x)
        val arrowLength = 20f
        val arrowAngle = PI.toFloat() / 6
        
        canvas.drawLine(
            start = end,
            end = Offset(
                end.x - arrowLength * cos(angle - arrowAngle),
                end.y - arrowLength * sin(angle - arrowAngle)
            ),
            color = color,
            strokeWidth = strokeWidth
        )
        
        canvas.drawLine(
            start = end,
            end = Offset(
                end.x - arrowLength * cos(angle + arrowAngle),
                end.y - arrowLength * sin(angle + arrowAngle)
            ),
            color = color,
            strokeWidth = strokeWidth
        )
    }
}
```

### ConfigHandler Translation

```kotlin
// Configuration management with expect/actual pattern
expect class ConfigHandler {
    fun getString(key: String, default: String = ""): String
    fun setString(key: String, value: String)
    fun getInt(key: String, default: Int = 0): Int
    fun setInt(key: String, value: Int)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)
    fun getColor(key: String, default: Color = Color.Red): Color
    fun setColor(key: String, value: Color)
}

// Common configuration keys
object ConfigKeys {
    const val DRAW_COLOR = "drawColor"
    const val STROKE_WIDTH = "strokeWidth"
    const val SAVE_PATH = "savePath"
    const val SHOW_HELP = "showHelp"
    const val COPY_ON_SELECTION = "copyOnSelection"
    const val CLOSE_AFTER_SAVE = "closeAfterSave"
}

// ViewModel using config
class ConfigViewModel(private val config: ConfigHandler) {
    val drawColor = mutableStateOf(
        config.getColor(ConfigKeys.DRAW_COLOR, Color.Red)
    )
    
    val strokeWidth = mutableStateOf(
        config.getInt(ConfigKeys.STROKE_WIDTH, 5)
    )
    
    fun saveConfig() {
        config.setColor(ConfigKeys.DRAW_COLOR, drawColor.value)
        config.setInt(ConfigKeys.STROKE_WIDTH, strokeWidth.value)
    }
}
```

### ColorPicker Widget

```kotlin
@Composable
fun ColorPicker(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Color indicator button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(currentColor)
                .border(2.dp, Color.White, CircleShape)
                .clickable { showPicker = !showPicker }
        )
        
        // Color picker popup
        if (showPicker) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, 50),
                onDismissRequest = { showPicker = false }
            ) {
                ColorPickerDialog(
                    initialColor = currentColor,
                    onColorSelected = { color ->
                        onColorSelected(color)
                        showPicker = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var value by remember { mutableStateOf(1f) }
    
    Column(
        modifier = Modifier
            .background(Color.DarkGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Predefined colors grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.size(240.dp, 60.dp)
        ) {
            val presetColors = listOf(
                Color.Red, Color.Green, Color.Blue,
                Color.Yellow, Color.Cyan, Color.Magenta,
                Color.Black, Color.White, Color.Gray,
                // Add more preset colors
            )
            
            items(presetColors) { color ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(2.dp)
                        .background(color, CircleShape)
                        .clickable { onColorSelected(color) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // HSV sliders
        Text("Hue", color = Color.White)
        Slider(
            value = hue,
            onValueChange = { hue = it },
            valueRange = 0f..360f
        )
        
        Text("Saturation", color = Color.White)
        Slider(
            value = saturation,
            onValueChange = { saturation = it },
            valueRange = 0f..1f
        )
        
        Text("Value", color = Color.White)
        Slider(
            value = value,
            onValueChange = { value = it },
            valueRange = 0f..1f
        )
        
        // Preview and select button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.hsv(hue, saturation, value),
                        RoundedCornerShape(4.dp)
                    )
            )
            
            Button(onClick = {
                onColorSelected(Color.hsv(hue, saturation, value))
            }) {
                Text("Select")
            }
        }
    }
}
```

## 4. Platform-Specific Implementations

### Screen Capture (expect/actual)

```kotlin
// Common interface
expect class ScreenGrabber {
    suspend fun captureScreen(screenIndex: Int = -1): ImageBitmap?
    suspend fun captureRegion(region: Rectangle): ImageBitmap?
    fun getScreenCount(): Int
    fun getScreenGeometry(index: Int): Rectangle
}

// macOS implementation using native APIs
actual class ScreenGrabber {
    actual suspend fun captureScreen(screenIndex: Int): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            // Use CoreGraphics for screen capture
            val displayId = if (screenIndex >= 0) {
                getDisplayIdForScreen(screenIndex)
            } else {
                CGMainDisplayID()
            }
            
            val image = CGDisplayCreateImage(displayId)
            image?.toImageBitmap()
        }
    }
    
    actual suspend fun captureRegion(region: Rectangle): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            val rect = CGRect(
                x = region.x.toDouble(),
                y = region.y.toDouble(),
                width = region.width.toDouble(),
                height = region.height.toDouble()
            )
            val image = CGWindowListCreateImage(
                rect,
                kCGWindowListOptionOnScreenOnly,
                kCGNullWindowID,
                kCGWindowImageDefault
            )
            image?.toImageBitmap()
        }
    }
    
    // Platform-specific helper
    private external fun getDisplayIdForScreen(index: Int): UInt
}

// JVM implementation using Robot
actual class ScreenGrabber {
    private val robot = Robot()
    
    actual suspend fun captureScreen(screenIndex: Int): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            val env = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val screens = env.screenDevices
            
            val screen = if (screenIndex in screens.indices) {
                screens[screenIndex]
            } else {
                env.defaultScreenDevice
            }
            
            val bounds = screen.defaultConfiguration.bounds
            val bufferedImage = robot.createScreenCapture(bounds)
            bufferedImage.toComposeImageBitmap()
        }
    }
}
```

### System Tray Integration

```kotlin
// Expect declaration
expect class SystemTray {
    fun show()
    fun hide()
    fun setMenu(items: List<TrayMenuItem>)
    fun showNotification(title: String, message: String)
}

data class TrayMenuItem(
    val label: String,
    val action: () -> Unit,
    val shortcut: KeyboardShortcut? = null
)

// Platform implementation (JVM example)
actual class SystemTray {
    private val tray = SystemTray.getSystemTray()
    private var trayIcon: TrayIcon? = null
    
    actual fun show() {
        if (!SystemTray.isSupported()) return
        
        val icon = loadTrayIcon()
        trayIcon = TrayIcon(icon, "Flameshot").apply {
            isImageAutoSize = true
            tray.add(this)
        }
    }
    
    actual fun setMenu(items: List<TrayMenuItem>) {
        val popup = PopupMenu()
        items.forEach { item ->
            val menuItem = MenuItem(item.label).apply {
                addActionListener { item.action() }
            }
            popup.add(menuItem)
        }
        trayIcon?.popupMenu = popup
    }
}
```

### Global Hotkeys

```kotlin
// Common interface
expect class GlobalHotkey {
    fun register(
        key: Key,
        modifiers: Set<KeyModifier>,
        action: () -> Unit
    ): HotkeyHandle
    
    fun unregister(handle: HotkeyHandle)
}

data class HotkeyHandle(val id: String)

// Platform-specific registration
actual class GlobalHotkey {
    actual fun register(
        key: Key,
        modifiers: Set<KeyModifier>,
        action: () -> Unit
    ): HotkeyHandle {
        // Platform-specific hotkey registration
        // macOS: Use NSEvent.addGlobalMonitorForEvents
        // JVM: Use JNativeHook or similar library
        // Windows: RegisterHotKey API
        
        val id = UUID.randomUUID().toString()
        registerNativeHotkey(key, modifiers, id, action)
        return HotkeyHandle(id)
    }
    
    private external fun registerNativeHotkey(
        key: Key,
        modifiers: Set<KeyModifier>,
        id: String,
        callback: () -> Unit
    )
}
```

## 5. Build Configuration Updates

```kotlin
// Update build.gradle.kts for Compose Multiplatform with Skiko

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation("org.jetbrains.skiko:skiko:0.7.77")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.skiko:skiko-awt:0.7.77")
                // For system tray on JVM
                implementation("com.dorkbox:SystemTray:4.4")
                // For global hotkeys on JVM
                implementation("com.github.kwhat:jnativehook:2.2.2")
            }
        }
        
        val macosMain by getting {
            dependencies {
                // macOS specific dependencies
                implementation("org.jetbrains.skiko:skiko-macos-x64:0.7.77")
                implementation("org.jetbrains.skiko:skiko-macos-arm64:0.7.77")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.flameshot.MainKt"
        
        nativeDistributions {
            targetFormats(Dmg, Pkg)
            packageName = "Flameshot"
            packageVersion = "13.1.0"
            
            macOS {
                bundleID = "org.flameshot.flameshot"
                iconFile.set(project.file("src/macosMain/resources/flameshot.icns"))
                
                // Info.plist customization
                infoPlist {
                    extraKeysRawXml = """
                        <key>LSUIElement</key>
                        <true/>
                        <key>NSHighResolutionCapable</key>
                        <true/>
                    """.trimIndent()
                }
            }
        }
    }
}
```

## 6. Migration Strategy

### Phase 1: Core Architecture
1. Set up Compose Multiplatform project structure ✓
2. Implement basic window management
3. Create screenshot capture infrastructure
4. Establish platform abstraction layer

### Phase 2: UI Components
1. Translate CaptureWidget to Compose
2. Implement tool system and drawing tools
3. Create color picker and tool panels
4. Add selection and magnifier widgets

### Phase 3: Platform Integration
1. Implement screen capture for each platform
2. Add system tray support
3. Integrate global hotkeys
4. Handle file operations and clipboard

### Phase 4: Advanced Features
1. Implement configuration system
2. Add undo/redo functionality
3. Create upload integrations
4. Implement pin window feature

### Phase 5: Polish and Optimization
1. Optimize rendering performance
2. Add animations and transitions
3. Implement proper error handling
4. Complete keyboard shortcuts

## Key Considerations

1. **State Management**: Use ViewModel pattern with StateFlow for complex state
2. **Performance**: Leverage Skiko for efficient drawing operations
3. **Platform Differences**: Use expect/actual for platform-specific code
4. **Resource Management**: Properly dispose of native resources
5. **Testing**: Write unit tests for business logic, UI tests for components

## Example Main Entry Point

```kotlin
// Main.kt
fun main() = application {
    val flameshot = remember { Flameshot.instance() }
    val config = remember { ConfigHandler() }
    
    // Set up global hotkeys
    val hotkeyManager = GlobalHotkey()
    DisposableEffect(Unit) {
        val handle = hotkeyManager.register(
            Key.Print,
            setOf(KeyModifier.Ctrl),
            action = { flameshot.gui() }
        )
        
        onDispose {
            hotkeyManager.unregister(handle)
        }
    }
    
    // System tray
    val tray = SystemTray()
    LaunchedEffect(Unit) {
        tray.show()
        tray.setMenu(
            listOf(
                TrayMenuItem("Capture", { flameshot.gui() }),
                TrayMenuItem("Config", { flameshot.config() }),
                TrayMenuItem("Quit", { exitApplication() })
            )
        )
    }
    
    // Main window (hidden by default)
    Window(
        onCloseRequest = ::exitApplication,
        visible = false,
        title = "Flameshot"
    ) {
        // Empty window, app runs in tray
    }
}
```

This guide provides a comprehensive framework for translating Flameshot from Qt C++ to Kotlin Compose Multiplatform, maintaining functionality while leveraging modern Kotlin features and Compose's declarative UI paradigm.