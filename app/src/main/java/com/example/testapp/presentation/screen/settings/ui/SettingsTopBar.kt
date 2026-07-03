package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.R
import com.example.testapp.uicommon.design.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(
    title: String = stringResource(R.string.settings_title),
    onBack: () -> Unit
) {
    AppTopBar(
        title = title,
        onBack = onBack,
        backContentDescription = stringResource(R.string.back)
    )
}
