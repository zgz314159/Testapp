package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.R

@Composable
fun SettingsFillTagClearChip(onClear: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClear,
        label = { Text(stringResource(R.string.fill_answer_tag_filter_clear)) }
    )
}
