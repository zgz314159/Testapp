package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.uicommon.component.LocalFontFamily

@Composable
fun ExportSourceSelectionDialog(
    title: String,
    fileNames: List<String>,
    selectedFileName: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    fontSize: Float
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)) },
        text = {
            Column {
                fileNames.forEach { fileName ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(fileName) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedFileName == fileName, onClick = { onSelect(fileName) })
                        Text(text = fileName, style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm, enabled = selectedFileName != null) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

