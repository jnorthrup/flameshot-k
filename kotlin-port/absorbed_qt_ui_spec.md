This file summarizes the Qt UI specifications absorbed into the Kotlin port.

Included items:
- CaptureWidget composable with selection, tool state, and tool panel placement.
- DrawingCanvas stub mapping QPainter to Skiko drawing via Compose Canvas.
- ToolPanel composable replicating layout and controls.
- CaptureViewModel using StateFlow for color and tool size.

Notes:
- Detailed path conversion and native Skia drawing helpers to be implemented.
- Event handling uses pointerInput and onKeyEvent in Compose; global hotkeys planned.
- This absorption preserves the functional contracts from the Qt code and maps them to idiomatic Compose patterns.
