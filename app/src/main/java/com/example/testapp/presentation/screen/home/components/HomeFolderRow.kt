package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.geometry.Rect
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.testapp.uicommon.util.MirroredIcon
// arrow-back still filled until autoMirrored library is available
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeFolderRow(
    currentFolder: String?,
    folderNames: List<String>,
    hoverFolder: String?,
    showFolderList: Boolean = true,
    showBackAction: Boolean = true,
    onBackFolder: () -> Unit,
    onFolderClick: (String) -> Unit,
    onFolderLongPress: (String) -> Unit,
    onReportFolderBounds: (String, Rect) -> Unit = { _, _ -> }
) {
    if (currentFolder != null) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackAction) {
                androidx.compose.material3.IconButton(onClick = onBackFolder) {
                    @Suppress("DEPRECATION")
                    MirroredIcon(Icons.Filled.ArrowBack, contentDescription = null)
                }
            }
            Text(
                currentFolder ?: "",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
    }
    if (showFolderList && folderNames.isNotEmpty()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            folderNames.forEach { folder ->
                val isActive = hoverFolder == folder || currentFolder == folder
                Row(
                    modifier = Modifier
                        .background(
                            color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(end = 12.dp)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .onGloballyPositioned { coords -> onReportFolderBounds(folder, coords.boundsInRoot()) }
                        .combinedClickable(
                            onClick = { onFolderClick(folder) },
                            onLongClick = { onFolderLongPress(folder) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isActive) { @Suppress("DEPRECATION") Icons.Filled.ArrowBack } else Icons.Outlined.Folder,
                        contentDescription = folder,
                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        folder,
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}


