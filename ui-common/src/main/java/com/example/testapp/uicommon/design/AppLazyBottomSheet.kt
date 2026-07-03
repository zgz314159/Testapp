package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Bottom sheet for LazyColumn content — no outer verticalScroll to avoid nested scroll jank. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLazyBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(AppLazyBottomSheetMetrics.heightFraction)
                .padding(bottom = AppSpacing.lg)
        ) {
            content()
        }
    }
}

object AppLazyBottomSheetMetrics {
    const val heightFraction = 0.92f
}
