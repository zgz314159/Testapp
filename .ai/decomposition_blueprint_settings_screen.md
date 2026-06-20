<!--
  File: decomposition_blueprint_settings_screen.md
  Target: SettingsScreen.kt (1358 lines)
  Generated: 2026-06-13
  Status: READY for Phase D execution
-->

# Decomposition Blueprint — SettingsScreen

## Overview

```
Before: SettingsScreen.kt (1358 lines, 1 @Composable + 1 private)
After:
  settings/ui/SettingsBasicPanel.kt            (~80 lines)
  settings/ui/SettingsMemoryPanel.kt           (~155 lines)
  settings/ui/SettingsExamPanel.kt             (~100 lines)
  settings/ui/SettingsPracticePanel.kt         (~115 lines)
  settings/ui/SettingsFillPanel.kt             (~340 lines)
  settings/ui/SettingsImportExportPanel.kt     (~120 lines)
  settings/ui/SettingsLoadingOverlay.kt        (~80 lines)
  settings/ui/ExportSourceSelectionDialog.kt   (~70 lines)
  SettingsScreen.kt                            (~450 lines)
```

## Step 1: Create `settings/ui/` directory

```bash
mkdir -p app/src/main/java/com/example/testapp/presentation/screen/settings/ui
```

## Step 2-8: Extract each composable, then rewrite SettingsScreen

### SettingsBasicPanel — font size + font style
Lines 293-354. Params: `fontSize, fontStyle, onFontSizeChange, onFontStyleChange`

### SettingsMemoryPanel — memory mode expandable
Lines 389-525. Params: `expanded, onToggle, fontSize, practiceMemoryMode, practiceMemoryBatchSize, practiceMemoryWrongMode, practiceMemoryPoolMode, onMemoryModeChange, onBatchSizeChange, onWrongModeChange, onPoolModeChange`

### SettingsExamPanel — exam expandable
Lines 527-607. Params: `expanded, onToggle, fontSize, randomExam, examCount, examDelay, onRandomChange, onCountChange, onDelayChange`

### SettingsPracticePanel — practice expandable
Lines 609-703. Params: `expanded, onToggle, fontSize, randomPractice, practiceCount, correctDelay, wrongDelay, onRandomChange, onCountChange, onCorrectDelayChange, onWrongDelayChange`

### SettingsFillPanel — fill settings expandable (largest)
Lines 705-1020. Params: `expanded, onToggle, fontSize, fillQuestionGenerationMode, fillBlankCount, fillFullAnswerRequireCorrect, fillFullAnswerRandomOrder, fillAnswerScoreMin, fillAnswerScoreMax, fillAnswerTagFilter, availableFillAnswerTags, fillQuestionFilterSummary, onModeChange, onBlankCountChange, onRequireCorrectChange, onRandomOrderChange, onScoreRangeChange, onTagFilterChange, onTagFilterClear`

### SettingsImportExportPanel — import/export buttons
Lines 1022-1124. Params: `fontSize, quizFileNames, wrongBookFileNames, favoriteFileNames, onImportQuiz, onImportLocal, onExportQuiz, onImportWrong, onExportWrong, onImportFavorites, onExportFavorites`

### SettingsLoadingOverlay — loading overlay
Lines 1126-1186. Params: `isLoading, fontSize, importProgress, onCancel`

### ExportSourceSelectionDialog — already private; just move file
Lines 1299-1356. Move to `settings/ui/ExportSourceSelectionDialog.kt`, make `internal` instead of `private`.

## Line Count Summary

| File | Est. Lines |
|------|-----------|
| SettingsScreen.kt (original) | 1358 |
| SettingsScreen.kt (after) | ~450 |
| SettingsBasicPanel.kt | ~80 |
| SettingsMemoryPanel.kt | ~155 |
| SettingsExamPanel.kt | ~100 |
| SettingsPracticePanel.kt | ~115 |
| SettingsFillPanel.kt | ~340 |
| SettingsImportExportPanel.kt | ~120 |
| SettingsLoadingOverlay.kt | ~80 |
| ExportSourceSelectionDialog.kt | ~70 |
| **Total extracted** | ~1060 lines (78% reduction in main file) |
