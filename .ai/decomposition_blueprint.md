<!--
  Derived from: L6 Decomposition Blueprint 2026-06-11
  Target: PracticeViewModel.kt → 6 Coordinators
  Last synced: 2026-06-11 21:05 UTC+8
  Do not edit directly — re-run Decomposition to regenerate.
-->

# Decomposition Blueprint: PracticeViewModel.kt

> Extracting 6 coordinators from one God ViewModel.
> Pattern: All coordinators share ONE `MutableStateFlow<PracticeSessionState>`.

---

## COORDINATOR #1: PracticeNavigationCoordinator (Step 1)

**Why first**: Pure logic, zero deps, highest bug-density area.

```kotlin
class PracticeNavigationCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>
) {
    val currentIndex: Int
    val totalCount: Int
    val isInAnsweredHistory: Boolean
    val canGoNext: Boolean
    val canGoPrev: Boolean

    fun goToNext()
    fun goToPrev()
    fun jumpToQuestion(index: Int)
    fun enterAnsweredHistory()
    fun exitAnsweredHistory()
    fun goToNextRandom()
    fun goToPrevRandom()
    val answeredIndices: List<Int>
}
```
**Extracted lines**: ~330 | **VM after**: 3570

---

## COORDINATOR #2: PracticeAnswerHandler (Step 2)

**Why second**: Pure logic, second-largest code block.

```kotlin
class PracticeAnswerHandler(
    private val sessionState: MutableStateFlow<PracticeSessionState>
) {
    val answeredCount: Int
    val correctCount: Int
    val wrongCount: Int
    fun isAnswered(index: Int): Boolean
    fun isCorrect(index: Int): Boolean

    fun submitAnswer(index: Int, selectedOptions: List<Int>)
    fun showResult(index: Int)
    fun toggleOption(index: Int, optionIndex: Int)
    fun resetAnswer(index: Int)
}
```
**Extracted lines**: ~260 | **VM after**: 3310

---

## COORDINATOR #3: PracticeProgressCoordinator (Step 3)

**Why third**: 1 repo dep, critical for data integrity.

```kotlin
class PracticeProgressCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val progressRepository: PracticeProgressRepository,
    private val scope: PracticeProgressScope
) {
    val progressId: String

    suspend fun saveProgress(currentIndex: Int)
    suspend fun restoreProgress(): PracticeProgress?
    suspend fun clearProgress()
    fun buildScopedProgressId(params: ProgressParams): String
}
```
**Extracted lines**: ~230 | **VM after**: 3080

---

## COORDINATOR #4: PracticeModeCoordinator (Step 4)

**Why fourth**: Depends on C3 (NavigationCoordinator) for shuffled indices.

```kotlin
class PracticeModeCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val questionList: List<Question>
) {
    val isRandomMode: Boolean
    val isLimitedMode: Boolean
    val isMemoryMode: Boolean
    val shuffledIndices: List<Int>
    val effectiveQuestions: List<QuestionWithState>

    fun configureRandomMode(count: Int?)
    fun configureLimitedMode(count: Int)
    fun configureMemoryMode()
}
```
**Extracted lines**: ~185 | **VM after**: 2895

---

## COORDINATOR #5: PracticeFullAnswerCoordinator (Step 5)

**Why fifth**: Depends on C4 (AnswerHandler) for per-question state.

```kotlin
class PracticeFullAnswerCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val answerHandler: PracticeAnswerHandler,
    private val questionList: List<Question>
) {
    val isFullAnswerMode: Boolean
    val pendingSourceIndices: List<Int>

    fun deriveFullAnswerQuestions(): List<QuestionWithState>
    fun autoAdvanceAfterCorrectSubmit(currentIndex: Int): Int?
    fun getNextSourceAfter(currentIndex: Int): Int?
}
```
**Extracted lines**: ~245 | **VM after**: 2650

---

## COORDINATOR #6: PracticeSessionCoordinator (Step 6)

**Why last**: 2 repo deps, controls loading lifecycle for all coordinators.

```kotlin
class PracticeSessionCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val questionRepository: QuestionRepository,
    private val progressRepository: PracticeProgressRepository
) {
    val questions: List<Question>
    val isLoading: Boolean
    val isReady: Boolean
    val isEmpty: Boolean

    suspend fun loadForFile(fileName: String, route: PracticeRoute)
    suspend fun loadForWrongBook()
    suspend fun loadForFavorites()
    suspend fun loadForAtomicBank(term: String)
    fun onSettingsReady()
}
```
**Extracted lines**: ~255 | **VM after**: 800

---

## STATE BRIDGING PATTERN

```kotlin
// VM owns the single source of truth
private val _sessionState = MutableStateFlow(PracticeSessionState())

// All coordinators share it
navCoordinator = PracticeNavigationCoordinator(_sessionState)
answerHandler = PracticeAnswerHandler(_sessionState)
progressCoordinator = PracticeProgressCoordinator(_sessionState, repo, scope)
// ...

// VM exposes derived flows to UI (unchanged)
val currentIndex: StateFlow<Int> = _sessionState.map { it.currentIndex }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
```

---

## IMMUTABLE CONSTRAINTS

- `PracticeSessionState` data class fields — **never change**
- `PracticeProgressScope` import path — **never move**
- Public VM API signatures — **never break**
- `__scope=` progress ID format — **never change**
- `legacyRandomScopedPracticeProgressId` — **never remove**

---

## VALIDATION PER STEP

| Step | Compile | Unit Test | Device Check |
|------|---------|-----------|--------------|
| 1 (C3) | ✅ | ✅ PracticeVMTest | ⚠️ Navigation smoke |
| 2 (C4) | ✅ | ✅ PracticeVMTest | ⚠️ Answer flow |
| 3 (C5) | ✅ | ✅ + HomeVMTest | ⚠️ Progress persistence |
| 4 (C7) | ✅ | ✅ PracticeVMTest | ⚠️ Random mode |
| 5 (C6) | ✅ | ✅ PracticeVMTest | ⚠️ Full-answer flow |
| 6 (C2) | ✅ | ✅ All tests | ✅ Full smoke |
