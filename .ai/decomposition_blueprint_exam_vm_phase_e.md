# Decomposition Blueprint: ExamViewModel (Phase E — Steps 2-6)

> Generated: 2026-06-13 | Target: `ExamViewModel.kt` 740→~320 lines

## Step Summary

| Step | New File | Lines (est.) | Content |
|------|----------|--------------|---------|
| 2 | `ExamState.kt` | ~155 | 22 StateFlows + 6 computed props + 18 var fields + reset() + `appendNoteMutex` + `disposeGradeRequested` |
| 3 | `ExamProgressCoordinator.kt` | ~160 | saveProgressInternal, loadProgress, mergeCurrentStateToPersistentMap, clearProgressAndReload, resetAllStates, clearProgress |
| 4 | `ExamArtifactCoordinator.kt` | ~170 | Notes: saveNoteAndWait, saveNote, appendNote, getNote, loadNotesFromRepository. Analysis: loadAnalysis (×3) + updateAnalysis (×3) |
| 5 | `ExamMemoryModeEngine.kt` (enhance) | +~90 | Add: restoreStateForMemoryRound, initializeMemoryRound, refreshMemoryRoundPool, advanceMemoryRound |
| 6 | `ExamViewModel.kt` (rewrite) | ~320 | Thin orchestrator: mode config, navigation, load entry, answer interaction, edit question, grade exam, statistics, clear/reset thin delegators |

## Step 2: ExamState

**Path**: `app/.../presentation/screen/ExamState.kt`  
**Annotation**: `@Singleton`  
**Constructor**: No injected deps  
**Content**:

```kotlin
package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
class ExamState @Inject constructor() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()
    // ... all 22 StateFlows ...
    
    val totalCount: Int get() = _questions.value.size
    val answeredCount: Int get() = ...
    val correctCount: Int get() = ...
    val wrongCount: Int get() = answeredCount - correctCount
    val unansweredCount: Int get() = totalCount - answeredCount
    
    // 18 legacy var fields
    var progressId: String = "exam_default"
    var progressSeed: Long = System.currentTimeMillis()
    // ...
    
    val appendNoteMutex = Mutex()
    val disposeGradeRequested = AtomicBoolean(false)
    
    fun reset() { /* reset all to defaults */ }
}
```

**Step 3: ExamProgressCoordinator** — Injection: `ExamState`, `ExamNavigationHelper`, `ExamFillTransform`, `SaveExamProgressUseCase`, `ClearExamProgressUseCase`, `GetExamProgressFlowUseCase`, 4 analysis load USecases

**Step 4: ExamArtifactCoordinator** — Injection: `ExamState`, `SaveQuestionNoteUseCase`, `GetQuestionNoteUseCase`, `SaveQuestionAnalysisUseCase`, `GetQuestionAnalysisUseCase`, `SaveSparkAnalysisUseCase`, `GetSparkAnalysisUseCase`, `SaveBaiduAnalysisUseCase`, `GetBaiduAnalysisUseCase`

**Step 5: ExamMemoryModeEngine enhancement** — Add 4 functions: `restoreStateForMemoryRound`, `initializeMemoryRound`, `refreshMemoryRoundPool`, `advanceMemoryRound`. Injected: `ExamNavigationHelper`

**Step 6: ExamViewModel rewrite** — Remove: 12 unused USecase injections (now handled by coordinators). Keep: `ExamState`, `ExamLoadDelegate`, `Enhanced Engine`, `ExamProgressCoordinator`, `ExamArtifactCoordinator`, `ExamNavigationHelper`, `ExamFillTransform`, `ExamAnswerRules`, `AnswerRules`, `getQuestionsUseCase`, `saveQuestionsUseCase`, `gradeExamUseCase`, `saveExamProgressUseCase`, `getExamProgressFlowUseCase`, `clearExamProgressUseCase` (for init callback). Delegate all progress/artifact/memory calls to coordinators.
