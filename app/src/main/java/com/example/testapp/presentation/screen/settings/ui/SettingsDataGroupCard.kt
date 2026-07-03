package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.R

@Composable
fun SettingsDataGroupCard(
    title: String,
    onImport: () -> Unit,
    onExport: () -> Unit,
    extraActionLabel: String? = null,
    onExtraAction: (() -> Unit)? = null
) {
    SettingsCardGroup {
        Text(
            text = title,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onImport,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.settings_import))
            }
            OutlinedButton(
                onClick = onExport,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.settings_export))
            }
        }
        if (extraActionLabel != null && onExtraAction != null) {
            OutlinedButton(
                onClick = onExtraAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Text(extraActionLabel)
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun SettingsDataManagementSection(
    onImportQuiz: () -> Unit,
    onExportQuiz: () -> Unit,
    onImportWrong: () -> Unit,
    onExportWrong: () -> Unit,
    onImportFavorites: () -> Unit,
    onExportFavorites: () -> Unit
) {
    SettingsDataGroupCard(
        title = stringResource(R.string.settings_data_quiz),
        onImport = onImportQuiz,
        onExport = onExportQuiz
    )
    SettingsDataGroupCard(
        title = stringResource(R.string.settings_data_wrongbook),
        onImport = onImportWrong,
        onExport = onExportWrong
    )
    SettingsDataGroupCard(
        title = stringResource(R.string.settings_data_favorites),
        onImport = onImportFavorites,
        onExport = onExportFavorites
    )
}
