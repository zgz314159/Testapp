package com.example.testapp.uicommon.util

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun MirroredIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified
) {
    val layoutDir = LocalLayoutDirection.current
    val flip = layoutDir == LayoutDirection.Rtl
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = if (flip) modifier.graphicsLayer(scaleX = -1f) else modifier,
        tint = tint
    )
}
