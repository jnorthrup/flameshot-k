// Small UI compatibility shims used by the Compose UI layer
package org.flameshot.tools

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

internal val EmptyImageVector: ImageVector = ImageVector.Builder(
    defaultWidth = 24.0.dp,
    defaultHeight = 24.0.dp,
    viewportWidth = 24.0f,
    viewportHeight = 24.0f
).build()
