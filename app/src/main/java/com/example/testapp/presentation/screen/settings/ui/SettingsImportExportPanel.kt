package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.uicommon.component.LocalFontFamily

@Composable
fun SettingsImportExportPanel(
    fontSize: Float,
    quizFileNamesSize: Int,
    wrongBookFileNamesSize: Int,
    favoriteFileNamesSize: Int,
    onImportQuiz: () -> Unit,
    onImportLocal: () -> Unit,
    onExportQuiz: () -> Unit,
    onImportWrong: () -> Unit,
    onExportWrong: () -> Unit,
    onImportFavorites: () -> Unit,
    onExportFavorites: () -> Unit
) {
    Button(onClick = onImportQuiz) {
        Text(stringResource(R.string.import_quiz_button), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onImportLocal) {
        Text(stringResource(R.string.import_quiz_local_button), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onExportQuiz) {
        Text(stringResource(R.string.export_quiz_button), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
    }
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onImportWrong) {
        Text(stringResource(R.string.import_wrong_button), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onExportWrong) {
        Text(stringResource(R.string.export_wrong_button), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
    }
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onImportFavorites) {
        Text(stringResource(R.string.import_favorites_button), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onExportFavorites) {
        Text(stringResource(R.string.export_favorites_button), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
    }
}

