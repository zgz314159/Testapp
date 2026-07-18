package com.example.testapp.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.feature.settings.R
import com.example.testapp.presentation.screen.settings.ui.SettingsFillPanelContent
import com.example.testapp.presentation.screen.settings.ui.SettingsTopBar
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import com.example.testapp.uicommon.design.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val fontSize by viewModel.fontSize.collectAsState()
    val fillQuestionGenerationMode by viewModel.fillQuestionGenerationMode.collectAsState()
    val fillBlankCount by viewModel.fillBlankCount.collectAsState()
    val fillFullAnswerRandomOrder by viewModel.fillFullAnswerRandomOrder.collectAsState()
    val fillFullAnswerRequireCorrect by viewModel.fillFullAnswerRequireCorrect.collectAsState()
    val fillAnswerScoreMin by viewModel.fillAnswerScoreMin.collectAsState()
    val fillAnswerScoreMax by viewModel.fillAnswerScoreMax.collectAsState()
    val fillAnswerTagFilter by viewModel.fillAnswerTagFilter.collectAsState()
    val availableFillAnswerTags by viewModel.availableFillAnswerTags.collectAsState()
    val fillQuestionFilterSummary by viewModel.fillQuestionFilterSummary.collectAsState()
    val practiceMemoryMode by viewModel.practiceMemoryMode.collectAsState()
    val practiceMemoryBatchSize by viewModel.practiceMemoryBatchSize.collectAsState()
    val practiceMemoryWrongMode by viewModel.practiceMemoryWrongMode.collectAsState()
    val practiceMemoryPoolMode by viewModel.practiceMemoryPoolMode.collectAsState()
    val context = LocalContext.current
    var showDetailedHelp by remember { mutableStateOf(false) }
    var memoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFontSettings()
        viewModel.ensureSettingsCollectionsStarted()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppElevatedActionSheetTokens.sheetBg,
        topBar = {
            SettingsTopBar(
                title = stringResource(R.string.settings_fill_settings),
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = AppSpacing.md)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { showDetailedHelp = !showDetailedHelp }) {
                    Text(
                        text = stringResource(
                            if (showDetailedHelp) R.string.fill_settings_hide_help
                            else R.string.fill_settings_show_help
                        ),
                        fontSize = 13.sp,
                        color = AppElevatedActionSheetTokens.brandBlue,
                    )
                }
            }
            SettingsFillPanelContent(
                fontSize = fontSize,
                fillQuestionGenerationMode = fillQuestionGenerationMode,
                fillBlankCount = fillBlankCount,
                fillFullAnswerRequireCorrect = fillFullAnswerRequireCorrect,
                fillFullAnswerRandomOrder = fillFullAnswerRandomOrder,
                fillAnswerScoreMin = fillAnswerScoreMin,
                fillAnswerScoreMax = fillAnswerScoreMax,
                fillAnswerTagFilter = fillAnswerTagFilter,
                availableFillAnswerTags = availableFillAnswerTags,
                fillQuestionFilterSummary = fillQuestionFilterSummary,
                memoryExpanded = memoryExpanded,
                memoryEnabled = practiceMemoryMode,
                memoryBatchSize = practiceMemoryBatchSize,
                memoryWrongMode = practiceMemoryWrongMode,
                memoryPoolMode = practiceMemoryPoolMode,
                onModeChange = { viewModel.setFillQuestionGenerationMode(context, it) },
                onBlankCountChange = { viewModel.setFillBlankCount(context, it) },
                onRequireCorrectChange = { viewModel.setFillFullAnswerRequireCorrect(context, it) },
                onRandomOrderChange = { viewModel.setFillFullAnswerRandomOrder(context, it) },
                onScoreRangeChange = { min, max -> viewModel.setFillAnswerScoreRange(context, min, max) },
                onTagFilterChange = { viewModel.setFillAnswerTagFilter(context, it) },
                onTagFilterClear = { viewModel.setFillAnswerTagFilter(context, "") },
                onMemoryExpandedChange = { memoryExpanded = it },
                onMemoryModeChange = { viewModel.setMemoryMode(context, it) },
                onMemoryBatchSizeChange = { viewModel.setMemoryBatchSize(context, it) },
                onMemoryWrongModeChange = { viewModel.setMemoryWrongMode(context, it) },
                onMemoryPoolModeChange = { viewModel.setMemoryPoolMode(context, it) },
                showDetailedHelp = showDetailedHelp,
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
        }
    }
}
