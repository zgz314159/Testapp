<!--
  File: architecture_design_report_practice_screen.md
  Target: PracticeScreen.kt (1277 lines → target ~800 lines)
  Generated: 2026-06-13
  Status: DESIGN COMPLETE — ready for Phase C execution
-->

# Architecture Design Report — PracticeScreen

## 1. Current State

| Metric | Value |
|--------|-------|
| Lines | 1277 |
| Composables | 1 @Composable fun + 1 private helper |
| ViewModel deps | 6 (PracticeVM, SettingsVM, DeepSeekVM, SparkVM, BaiduVM, FavoriteVM) |
| Local state vars | ~25 `remember` blocks |
| LaunchedEffects | ~15 |
| BackHandler | 1 |
| Drag gesture | 1 (pointerInput with detectHorizontalDragGestures) |
| Already-extracted components | 20+ (ExamTopBar, ExamOptionsList, etc.) |

**Key difference from ViewModel extraction**: This is a Compose UI function, not a class. Extraction targets **composable sub-functions**, not coordinator classes. The file has already been partially decomposed — 20+ sub-components live in `presentation/screen/components/`.

## 2. Bounded Contexts Identification

### BC-1: Question Content Rendering (~70 lines, lines 821-885)
**Responsibility**: Render question content based on type (InlineBlank / TextResponse / generic)
- InlineBlank: `InlineBlankQuestionContent` + `StemImagesSection`
- TextResponse: `TextAnswerQuestionContent` + `StemImagesSection`
- Generic: `StemContent` + `ExamOptionsList`

### BC-2: Answer Result & Retry Controls (~115 lines, lines 920-1036)
**Responsibility**: Display correct/wrong result box + retry/retry-wrong-blanks buttons
- Content varies by question type (FillAnswerResultText for fill, RichText for others)
- Retry button (always visible when showResult)
- Retry-wrong-blanks button (fill type only)

### BC-3: Analysis Expandable Sections (~90 lines, lines 1042-1133)
**Responsibility**: Explanation, Note, DeepSeek, Spark, Baidu expandable sections
- Each section toggles expandedSection state
- Delete/long-press handlers wired to dialog state

### BC-4: Font Settings Menu (~65 lines, lines 661-727)
**Responsibility**: Dropdown menu items for font size, line spacing, letter spacing, edit question
- 6 adjustment items (increase/decrease * 3 metrics) + 1 edit question item
- Each persists to `FontSettingsDataStore`

## 3. Extraction Plan

### Extract: `PracticeQuestionContent.kt` (composable)
```
Location: app/.../presentation/screen/practice/PracticeQuestionContent.kt
Lines: ~80
Parameters: question, textAnswer, showResult, selectedOption, displayOptions,
            questionFontSize, questionLineSpacing, questionLetterSpacing,
            onAnswerChange, onOptionClick, submitCurrentAnswer
```

### Extract: `PracticeResultSection.kt` (composable)
```
Location: app/.../presentation/screen/practice/PracticeResultSection.kt
Lines: ~130
Parameters: question, showResult, textAnswer, resolvedFillAnswer, correctIndices,
            displayOptions, selectedOption, questionFontSize, questionLetterSpacing,
            allCorrect, allCorrectText, wrongFormatText, retryLabel, retryWrongLabel,
            onRetry, onRetryWrongBlanks
```

### Extract: `PracticeAnalysisSections.kt` (composable)
```
Location: app/.../presentation/screen/practice/PracticeAnalysisSections.kt
Lines: ~110
Parameters: showResult, question, expandedSection, onSectionToggle,
            explanationScroll, noteScroll, deepSeekScroll, sparkScroll, baiduScroll,
            analysisText, sparkText, baiduText, note, questionFontSize,
            onEditNote, onViewDeepSeek, onViewSpark, onViewBaidu,
            onDeleteAnalysis, onDeleteNote, onPauseAutoAdvance
```

### Extract: `PracticeFontSettingsMenu.kt` (composable)
```
Location: app/.../presentation/screen/practice/PracticeFontSettingsMenu.kt
Lines: ~80
Parameters: questionFontSize, questionLineSpacing, questionLetterSpacing,
            onFontSizeChange, onLineSpacingChange, onLetterSpacingChange,
            onEditQuestion, onDismiss
```

## 4. What Stays in PracticeScreen

After extraction (~1277 → ~800 lines), PracticeScreen retains:
- State collection from ViewModels (lines 118-255)
- LaunchedEffects for side effect orchestration (lines 256-447)
- BackHandler with exit logic (lines 448-471)
- Loading/empty state fallbacks (lines 473-493)
- Drag gesture setup (lines 495-614)
- TopBar + progress bar + header row (lines 616-819)
- Font menu wiring (reference to extracted composable)
- Copy button + remove-from-memory-pool button
- Submit controls
- Dialog wiring
- logRichTextAnswerDebug helper

## 5. Design Decisions

| Decision | Rationale |
|----------|-----------|
| Extract as composables, not classes | Compose UI is inherently functional; state lives in parent |
| Keep state in PracticeScreen | LaunchedEffect chains tie to currentIndex, question, showResult — centralizing avoids prop drilling |
| Pass many params vs. wrap in data class | Standard Compose pattern — explicit params aid recomposition skipping |
| No ViewModel extraction | All business logic is already in PracticeViewModel; PracticeScreen is pure UI |
| Move to `practice/` subpackage | Mirrors `settings/` pattern from Phase B |

## 6. Immutable Constraints

1. **NO changes to PracticeViewModel** — Screen-only extraction; VM boundaries remain intact
2. **NO changes to SettingsScreen** or other consumers
3. **NO changes to already-extracted components** in `presentation/screen/components/`
4. **All ViewModel state access stays in PracticeScreen** — extracted composables receive values, not ViewModels
5. **Public API preserved** — `PracticeScreen()` function signature unchanged
