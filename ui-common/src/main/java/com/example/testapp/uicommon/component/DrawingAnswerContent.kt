package com.example.testapp.uicommon.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.testapp.uicommon.component.LocalFontFamily
import java.io.File

private val DRAWING_IMAGES_REGEX = Regex("\\[DRAWING_IMAGES:([^\\]]+)]")
private val DRAWING_TABLES_REGEX = Regex("\\[DRAWING_TABLE:([^\\]]+)]")
private const val TABLE_CELL_SEP = "\u001F"
private const val TABLE_ROW_SEP = "\u001E"

fun extractDrawingImagePaths(answer: String): List<String> {
    val match = DRAWING_IMAGES_REGEX.find(answer) ?: return emptyList()
    return match.groupValues[1].split(",").map { it.trim() }.filter { it.isNotBlank() }
}

fun extractDrawingTables(answer: String): List<List<List<String>>> {
    val tables = mutableListOf<List<List<String>>>()
    DRAWING_TABLES_REGEX.findAll(answer).forEach { match ->
        val tableData = match.groupValues[1]
        val rows = tableData.split(TABLE_ROW_SEP).map { row ->
            row.split(TABLE_CELL_SEP).map { it.trim() }
        }.filter { it.any { cell -> cell.isNotBlank() } }
        if (rows.isNotEmpty()) {
            val maxCols = rows.maxOf { it.size }
            val padded = rows.map { row ->
                if (row.size < maxCols) row + List(maxCols - row.size) { "" } else row
            }
            tables.add(padded)
        }
    }
    return tables
}

fun stripDrawingTags(answer: String): String {
    return answer
        .replace(DRAWING_IMAGES_REGEX, "")
        .replace(DRAWING_TABLES_REGEX, "")
        .trim()
}

@Composable
fun DrawingAnswerImages(
    answer: String,
    modifier: Modifier = Modifier
) {
    val imagePaths = remember(answer) { extractDrawingImagePaths(answer) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    if (imagePaths.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        imagePaths.forEachIndexed { index, path ->
            val file = File(path)
            if (file.exists()) {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(file)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedImagePath = path },
                    contentScale = ContentScale.FillWidth
                )
                if (index < imagePaths.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    selectedImagePath?.let { path ->
        ZoomableImageViewer(
            imagePath = path,
            onDismiss = { selectedImagePath = null }
        )
    }
}

@Composable
fun TextResponseAnswerContent(
    answer: String,
    questionFontSize: Float,
    modifier: Modifier = Modifier
) {
    val imagePaths = remember(answer) { extractDrawingImagePaths(answer) }
    val tables = remember(answer) { extractDrawingTables(answer) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    val hasImages = imagePaths.isNotEmpty()
    val hasTables = tables.isNotEmpty()

    if (!hasImages && !hasTables) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (hasTables) {
            tables.forEachIndexed { tableIndex, table ->
                AnswerTable(
                    rows = table,
                    questionFontSize = questionFontSize
                )
                if (tableIndex < tables.size - 1 || hasImages) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (hasImages) {
            imagePaths.forEachIndexed { index, path ->
                val file = File(path)
                if (file.exists()) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(file)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedImagePath = path },
                        contentScale = ContentScale.FillWidth
                    )
                    if (index < imagePaths.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    selectedImagePath?.let { path ->
        ZoomableImageViewer(
            imagePath = path,
            onDismiss = { selectedImagePath = null }
        )
    }
}

@Composable
private fun AnswerTable(
    rows: List<List<String>>,
    questionFontSize: Float,
    modifier: Modifier = Modifier
) {
    if (rows.isEmpty()) return
    val columnCount = rows.maxOf { it.size }.coerceAtLeast(1)
    val borderColor = MaterialTheme.colorScheme.outline
    val cellFontSize = (questionFontSize - 2f).coerceAtLeast(12f).sp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor)
    ) {
        rows.forEachIndexed { rowIndex, cells ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                for (colIndex in 0 until columnCount) {
                    val cellText = cells.getOrNull(colIndex).orEmpty()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .drawBehind {
                                val strokePx = 1.dp.toPx()
                                drawLine(
                                    color = borderColor,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokePx
                                )
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokePx
                                )
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cellText,
                            fontSize = cellFontSize,
                            fontFamily = LocalFontFamily.current,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StemImagesSection(
    imagePaths: List<String>,
    modifier: Modifier = Modifier
) {
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imagePaths.forEachIndexed { index, path ->
            val file = remember(path) { File(path) }
            if (file.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(file)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedImagePath = path },
                    contentScale = ContentScale.FillWidth
                )
                if (index < imagePaths.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    selectedImagePath?.let { path ->
        ZoomableImageViewer(
            imagePath = path,
            onDismiss = { selectedImagePath = null }
        )
    }
}
