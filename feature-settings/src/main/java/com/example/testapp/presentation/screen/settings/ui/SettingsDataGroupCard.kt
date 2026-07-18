package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.feature.settings.R
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

@Composable
fun SettingsDataManagementSection(
    fontSize: Float,
    onImportQuiz: () -> Unit,
    onExportQuiz: () -> Unit,
    onImportWrong: () -> Unit,
    onExportWrong: () -> Unit,
    onImportFavorites: () -> Unit,
    onExportFavorites: () -> Unit,
) {
    val tokens = AppElevatedActionSheetTokens
    var expandedQuiz by rememberSaveable { mutableStateOf(false) }
    var expandedWrong by rememberSaveable { mutableStateOf(false) }
    var expandedFavorite by rememberSaveable { mutableStateOf(false) }

    SettingsCardGroup {
        SettingsExpandableCardSection(
            title = stringResource(R.string.settings_data_quiz),
            fontSize = fontSize,
            expanded = expandedQuiz,
            onExpandedChange = { expanded ->
                expandedQuiz = expanded
                if (expanded) {
                    expandedWrong = false
                    expandedFavorite = false
                }
            },
            expandDescription = stringResource(R.string.expand_data_quiz),
            collapseDescription = stringResource(R.string.collapse_data_quiz),
            leadingIcon = Icons.AutoMirrored.Filled.MenuBook,
        ) {
            SettingsDataTransferActions(
                onImport = onImportQuiz,
                onExport = onExportQuiz,
            )
        }
        SettingsCardDivider()
        SettingsExpandableCardSection(
            title = stringResource(R.string.settings_data_wrongbook),
            fontSize = fontSize,
            expanded = expandedWrong,
            onExpandedChange = { expanded ->
                expandedWrong = expanded
                if (expanded) {
                    expandedQuiz = false
                    expandedFavorite = false
                }
            },
            expandDescription = stringResource(R.string.expand_data_wrongbook),
            collapseDescription = stringResource(R.string.collapse_data_wrongbook),
            leadingIcon = Icons.Filled.Report,
        ) {
            SettingsDataTransferActions(
                onImport = onImportWrong,
                onExport = onExportWrong,
                accent = tokens.accentPurple,
                accentSoft = tokens.accentPurpleSoft,
            )
        }
        SettingsCardDivider()
        SettingsExpandableCardSection(
            title = stringResource(R.string.settings_data_favorites),
            fontSize = fontSize,
            expanded = expandedFavorite,
            onExpandedChange = { expanded ->
                expandedFavorite = expanded
                if (expanded) {
                    expandedQuiz = false
                    expandedWrong = false
                }
            },
            expandDescription = stringResource(R.string.expand_data_favorites),
            collapseDescription = stringResource(R.string.collapse_data_favorites),
            leadingIcon = Icons.Filled.Favorite,
        ) {
            SettingsDataTransferActions(
                onImport = onImportFavorites,
                onExport = onExportFavorites,
                accent = tokens.accentTeal,
                accentSoft = tokens.accentTealSoft,
            )
        }
    }
}

@Composable
private fun SettingsDataTransferActions(
    onImport: () -> Unit,
    onExport: () -> Unit,
    accent: Color = AppElevatedActionSheetTokens.brandBlue,
    accentSoft: Color = AppElevatedActionSheetTokens.brandBlueSoft,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SettingsDataActionButton(
            label = stringResource(R.string.settings_import),
            icon = Icons.Filled.FileUpload,
            onClick = onImport,
            containerColor = accentSoft,
            contentColor = accent,
            elevated = true,
            modifier = Modifier.weight(1f),
        )
        SettingsDataActionButton(
            label = stringResource(R.string.settings_export),
            icon = Icons.Filled.FileDownload,
            onClick = onExport,
            containerColor = AppElevatedActionSheetTokens.cardWhite,
            contentColor = AppElevatedActionSheetTokens.textPrimary,
            elevated = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SettingsDataActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    elevated: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        tonalElevation = if (elevated) 1.dp else 0.dp,
        shadowElevation = if (elevated) 5.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 6.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
        }
    }
}
