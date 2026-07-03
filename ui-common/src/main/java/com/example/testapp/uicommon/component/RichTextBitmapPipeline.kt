package com.example.testapp.uicommon.component

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.max
import ru.noties.jlatexmath.JLatexMathDrawable

internal fun JLatexMathDrawable.toImageBitmap(): ImageBitmap {
    val width = max(1, intrinsicWidth)
    val height = max(1, intrinsicHeight)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))
    return bitmap.asImageBitmap()
}
