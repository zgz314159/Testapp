package com.example.testapp.presentation.screen.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.testapp.uicommon.design.AppEmptyState
import com.example.testapp.uicommon.design.AppCard
import com.example.testapp.uicommon.design.AppContentText
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryQuestionDetailScreen(
    title: String,
    emptyMessage: String,
    items: List<String>,
    actionLabel: String,
    onBack: () -> Unit,
    onAction: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(title = title, onBack = onBack)
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(shadowElevation = AppSpacing.sm) {
                    Button(
                        onClick = onAction,
                        modifier = Modifier
                            .padding(AppSpacing.md)
                            .fillMaxWidth()
                    ) {
                        Text(actionLabel)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            AppEmptyState(
                message = emptyMessage,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            contentPadding = PaddingValues(bottom = AppSpacing.lg)
        ) {
            itemsIndexed(items) { _, line ->
                AppCard {
                    AppContentText(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
