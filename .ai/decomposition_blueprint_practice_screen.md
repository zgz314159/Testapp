<!--
  File: decomposition_blueprint_practice_screen.md
  Target: PracticeScreen.kt (1277 lines)
  Generated: 2026-06-13
  Status: READY for Phase C execution
-->

# Decomposition Blueprint — PracticeScreen

## Overview

```
Before: PracticeScreen.kt (1277 lines, 1 @Composable + 1 helper)
After:
  practice/PracticeQuestionContent.kt  (~80 lines)
  practice/PracticeResultSection.kt    (~130 lines)
  practice/PracticeAnalysisSections.kt (~110 lines)
  practice/PracticeFontSettingsMenu.kt (~80 lines)
  PracticeScreen.kt                    (~800 lines, 1 @Composable + 1 helper)
```

## Step 1: Create `practice/` directory

```bash
mkdir -p app/src/main/java/com/example/testapp/presentation/screen/practice
```

## Step 2: Extract `PracticeQuestionContent`

**File**: `practice/PracticeQuestionContent.kt`

Lines 821-885 in original. Three branching renderers:

```kotlin
@Composable
fun PracticeQuestionContent(
    question: com.example.testapp.domain.model.Question,
    textAnswer: String,
    showResult: Boolean,
    selectedOption: List<Int>,
    displayOptions: List<String>,
    questionFontSize: Float,
    questionLineSpacing: Float,
    questionLetterSpacing: Float,
    onAnswerChange: (String) -> Unit,
    onOptionClick: (Int) -> Unit,
    submitCurrentAnswer: (Int?) -> Unit
)
```

Renders: InlineBlank → `InlineBlankQuestionContent` + `StemImagesSection`; TextResponse → `TextAnswerQuestionContent` + `StemImagesSection`; generic → `StemContent` + `ExamOptionsList`.

## Step 3: Extract `PracticeResultSection`

**File**: `practice/PracticeResultSection.kt`

Lines 920-1036. Two sub-blocks:
- Result box (correct/wrong colored background with `FillAnswerResultText` or `RichText` + `TextResponseAnswerContent`)
- Retry + retry-wrong-blanks buttons

```kotlin
@Composable
fun PracticeResultSection(
    question: com.example.testapp.domain.model.Question,
    showResult: Boolean,
    textAnswer: String,
    resolvedFillAnswer: String,
    correctIndices: List<Int>,
    displayOptions: List<String>,
    selectedOption: List<Int>,
    questionFontSize: Float,
    questionLetterSpacing: Float,
    allCorrect: Boolean,
    correctText: String,
    answerResultText: String,
    retryLabel: String,
    retryWrongLabel: String,
    onRetry: () -> Unit,
    onRetryWrongBlanks: (() -> Unit)?
)
```

## Step 4: Extract `PracticeAnalysisSections`

**File**: `practice/PracticeAnalysisSections.kt`

Lines 1042-1133. Three sub-blocks:
- Explanation box (when showResult && explanation non-blank)
- Note box (when showResult && note non-blank)
- AI analysis sections (DeepSeek, Spark, Baidu)

```kotlin
@Composable
fun PracticeAnalysisSections(
    showResult: Boolean,
    question: com.example.testapp.domain.model.Question,
    expandedSection: Int,
    explanationScroll: androidx.compose.foundation.ScrollState,
    noteScroll: androidx.compose.foundation.ScrollState,
    deepSeekScroll: androidx.compose.foundation.ScrollState,
    sparkScroll: androidx.compose.foundation.ScrollState,
    baiduScroll: androidx.compose.foundation.ScrollState,
    analysisText: String?,
    sparkText: String?,
    baiduText: String?,
    note: String?,
    questionFontSize: Float,
    onSectionToggle: (Int) -> Unit,
    onEditNote: (String, Int, Int) -> Unit,
    onViewDeepSeek: (String, Int, Int) -> Unit,
    onViewSpark: (String, Int, Int) -> Unit,
    onViewBaidu: (String, Int, Int) -> Unit,
    onDeleteAnalysis: (String) -> Unit,
    onDeleteNote: () -> Unit,
    onPauseAutoAdvance: () -> Unit
)
```

## Step 5: Extract `PracticeFontSettingsMenu`

**File**: `practice/PracticeFontSettingsMenu.kt`

Lines 661-727. 7 dropdown items:

```kotlin
@Composable
fun PracticeFontSettingsMenu(
    questionFontSize: Float,
    questionLineSpacing: Float,
    questionLetterSpacing: Float,
    onFontSizeChange: (Float) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onLetterSpacingChange: (Float) -> Unit,
    onEditQuestion: () -> Unit,
    onDismiss: () -> Unit
)
```

Hardcoded step values: font ±2, line ±0.1, letter ±0.05. Persistence stays in caller via callbacks.

## Step 6: Rewire PracticeScreen

Replace extracted blocks with composable calls:

```kotlin
// Replace lines 821-885:
PracticeQuestionContent(
    question = question,
    textAnswer = textAnswer,
    showResult = showResult,
    selectedOption = selectedOption,
    displayOptions = displayOptions,
    questionFontSize = questionFontSize,
    questionLineSpacing = questionLineSpacing,
    questionLetterSpacing = questionLetterSpacing,
    onAnswerChange = { viewModel.updateTextAnswer(it) },
    onOptionClick = { idx ->
        if (QuestionTypes.isSingle(question.type) || QuestionTypes.isJudge(question.type)) {
            submitCurrentAnswer(idx)
        } else {
            viewModel.toggleOption(idx)
        }
    },
    submitCurrentAnswer = submitCurrentAnswer
)

// Replace lines 920-1036:
PracticeResultSection(
    question = question,
    showResult = showResult,
    textAnswer = textAnswer,
    resolvedFillAnswer = resolvedFillAnswer,
    correctIndices = correctIndices,
    displayOptions = displayOptions,
    selectedOption = selectedOption,
    questionFontSize = questionFontSize,
    questionLetterSpacing = questionLetterSpacing,
    allCorrect = allCorrect,
    correctText = correctText,
    answerResultText = answerResultText,
    retryLabel = stringResource(R.string.retry_current_question),
    retryWrongLabel = stringResource(R.string.retry_wrong_blanks),
    onRetry = { autoJob?.cancel(); viewModel.retryQuestion(currentIndex); answeredThisSession = sessionAnsweredCount > 1 },
    onRetryWrongBlanks = if (QuestionTypes.isInlineBlank(question.type)) ({
        autoJob?.cancel(); viewModel.retryWrongFillBlanks(currentIndex); answeredThisSession = true
    }) else null
)

// Replace lines 1042-1133:
PracticeAnalysisSections(
    showResult = showResult,
    question = question,
    expandedSection = expandedSection,
    explanationScroll = explanationScroll,
    noteScroll = noteScroll,
    deepSeekScroll = deepSeekScroll,
    sparkScroll = sparkScroll,
    baiduScroll = baiduScroll,
    analysisText = analysisText,
    sparkText = sparkText,
    baiduText = baiduText,
    note = note,
    questionFontSize = questionFontSize,
    onSectionToggle = { expandedSection = it },
    onEditNote = onEditNote,
    onViewDeepSeek = onViewDeepSeek,
    onViewSpark = onViewSpark,
    onViewBaidu = onViewBaidu,
    onDeleteAnalysis = { showDeleteDialog = true; deleteTarget = it },
    onDeleteNote = { showDeleteNoteDialog = true },
    onPauseAutoAdvance = ::pausePendingAutoAdvance
)

// Replace settingsMenuContent lambda:
settingsMenuContent = {
    PracticeFontSettingsMenu(
        questionFontSize = questionFontSize,
        questionLineSpacing = questionLineSpacing,
        questionLetterSpacing = questionLetterSpacing,
        onFontSizeChange = { newSize ->
            questionFontSize = newSize
            coroutineScope.launch { FontSettingsDataStore.setPracticeFontSize(context, newSize) }
        },
        onLineSpacingChange = { newSpacing ->
            questionLineSpacing = newSpacing
            coroutineScope.launch { FontSettingsDataStore.setPracticeLineSpacing(context, newSpacing) }
        },
        onLetterSpacingChange = { newSpacing ->
            questionLetterSpacing = newSpacing
            coroutineScope.launch { FontSettingsDataStore.setPracticeLetterSpacing(context, newSpacing) }
        },
        onEditQuestion = {
            pausePendingAutoAdvance()
            focusManager.clearFocus(force = true)
            viewModel.clearEditableQuestion()
            viewModel.prepareEditableQuestion(currentIndex)
            showEditQuestionDialog = true
        },
        onDismiss = { menuExpanded = false }
    )
}
```

## Step 7: Build verification

```bash
./gradlew compileDebugKotlin
```

## Line Count Summary

| File | Est. Lines |
|------|-----------|
| PracticeScreen.kt (original) | 1277 |
| PracticeScreen.kt (after) | ~800 |
| PracticeQuestionContent.kt | ~80 |
| PracticeResultSection.kt | ~130 |
| PracticeAnalysisSections.kt | ~110 |
| PracticeFontSettingsMenu.kt | ~80 |
| **Total extracted** | ~400 lines (31% reduction in main file) |
