package com.example.testapp.presentation.screen.file

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.testapp.R
import com.example.testapp.uicommon.design.AppEmptyStateInline
import java.io.File

/** Quiz file extensions the in-app browser will show and the importer can parse. */
val QUIZ_IMPORT_EXTENSIONS: Set<String> = setOf("json", "sqlite", "db", "xls", "xlsx", "txt", "docx")

/**
 * Whether the app can read shared storage directly.
 *
 * On API 30+ this requires All Files Access ([Environment.isExternalStorageManager]); on older
 * releases the legacy [Manifest.permission.READ_EXTERNAL_STORAGE] runtime grant is enough.
 */
fun hasStorageAccess(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }

private fun listQuizEntries(dir: File): List<File> {
    val children = dir.listFiles() ?: return emptyList()
    val dirs = children
        .filter { it.isDirectory && !it.name.startsWith(".") && it.canRead() }
        .sortedBy { it.name.lowercase() }
    val files = children
        .filter { it.isFile && it.extension.lowercase() in QUIZ_IMPORT_EXTENSIONS }
        .sortedBy { it.name.lowercase() }
    return dirs + files
}

/**
 * Minimal in-app file browser rooted at the shared external-storage directory. Lets the user
 * navigate folders and multi-select quiz files, bypassing the system document picker entirely.
 */
@Composable
fun QuizFileBrowserDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<File>) -> Unit,
) {
    val root = remember { Environment.getExternalStorageDirectory() }
    var currentDir by remember { mutableStateOf(root) }
    val selected: SnapshotStateList<File> = remember { mutableListOf<File>().toMutableStateList() }
    val entries by remember(currentDir) { mutableStateOf(listQuizEntries(currentDir)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.file_browser_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                )
                Text(
                    text = currentDir.absolutePath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                HorizontalDivider()

                if (currentDir.absolutePath != root.absolutePath) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentDir = currentDir.parentFile ?: root }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.file_browser_up))
                    }
                    HorizontalDivider()
                }

                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (entries.isEmpty()) {
                        item {
                            AppEmptyStateInline(
                                message = stringResource(R.string.file_browser_empty)
                            )
                        }
                    }
                    items(entries, key = { it.absolutePath }) { entry ->
                        val isDir = entry.isDirectory
                        val checked = selected.any { it.absolutePath == entry.absolutePath }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isDir) {
                                        currentDir = entry
                                    } else {
                                        if (checked) {
                                            selected.removeAll { it.absolutePath == entry.absolutePath }
                                        } else {
                                            selected.add(entry)
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (isDir) Icons.Filled.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = if (isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = entry.name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (!isDir) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            if (selected.none { it.absolutePath == entry.absolutePath }) {
                                                selected.add(entry)
                                            }
                                        } else {
                                            selected.removeAll { it.absolutePath == entry.absolutePath }
                                        }
                                    },
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.file_browser_cancel))
                    }
                    TextButton(
                        onClick = { onConfirm(selected.toList()) },
                        enabled = selected.isNotEmpty(),
                    ) {
                        Text(stringResource(R.string.file_browser_import_selected, selected.size))
                    }
                }
            }
        }
    }
}
