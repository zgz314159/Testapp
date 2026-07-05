package com.example.testapp.presentation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.practice.R
import com.example.testapp.uicommon.design.AppEmptyState

@Composable
fun HomeEmptyLibraryPanel(
    reason: HomeLibraryEmptyReason,
    modifier: Modifier = Modifier
) {
    val message = when (reason) {
        HomeLibraryEmptyReason.NO_QUIZ_FILES -> stringResource(R.string.home_empty_no_quiz)
        HomeLibraryEmptyReason.ROOT_EMPTY_WITH_FOLDERS -> stringResource(R.string.home_empty_root)
    }
    AppEmptyState(message = message, modifier = modifier)
}
