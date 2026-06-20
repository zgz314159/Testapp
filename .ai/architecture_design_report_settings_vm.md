<!--
  Derived from: L6 Architecture Design Phase 2026-06-13
  Target: SettingsViewModel.kt (Score 74, ~1178 lines)
  Last synced: 2026-06-13 06:15 UTC+8
  Do not edit directly — re-run Architecture Design to regenerate.
  Frozen snapshot — 5 bounded contexts identified.
-->

# Architecture Design Report: SettingsViewModel.kt

> Target: Third-highest priority refactor candidate (Score 74/100, ~1178 lines, 5 responsibilities, 7 injected repos).

---

## 1. RESPONSIBILITY BREAKDOWN (5 Bounded Contexts)

| # | Bounded Context | Lines (est.) | Risk |
|---|-----------------|-------------|------|
| **C1** | FontSettingsCoordinator | ~300 | 🟡 30+ StateFlows + 25+ setters + DataStore persistence |
| **C2** | FillQuestionFilterCoordinator | ~80 | 🟢 Pure display filter logic |
| **C3** | ImportCoordinator | ~250 | 🟠 2 import flows (URI + direct file) + error handling |
| **C4** | ExportCoordinator | ~450 | 🔴 7 export flows + Excel workbook + POI + sheet building |
| **C5** | Cancel/Cleanup | ~20 | 🟢 Trivial |

**Key insight**: C2 is pure logic. C3/C4 share `progress`, `isLoading`, `currentJob` state and `messageResult`.

---

## 2. DEPENDENCY MATRIX

| Bounded Context | External Dependencies | Pure Logic? |
|-----------------|----------------------|-------------|
| C1: FontSettings | FontSettingsDataStore, `android.content.Context` | ❌ |
| C2: FillFilter | QuestionRepository (getQuestions), fill state flows | ❌ |
| C3: Import | QuestionRepository, WrongBookRepository, FavoriteRepository, Context, Uri, IO | ❌ |
| C4: Export | QuestionRepository, WrongBookRepository, FavoriteRepository, HistoryRepository, QuestionAnalysisRepository, QuestionAskRepository, QuestionNoteRepository, Context, Apache POI | ❌ |
| C5: Cancel | None (just coroutine Job + state reset) | ✅ |

---

## 3. STATE OWNERSHIP MAP

| State | Current Owner | Suggested Owner | Rationale |
|-------|---------------|----------------|-----------|
| 30+ font/feature settings | SettingsViewModel | **FontSettingsCoordinator** | Font-exclusive state (size/style/counts/delays/memory modes) |
| `fillBlankCount` etc. | SettingsViewModel | **FontSettingsCoordinator** (write) + **FillQuestionFilterCoordinator** (read) | Shared for filter summary calculation |
| `isLoading` / `progress` | SettingsViewModel | **SettingsViewModel** (shared) | Import + Export both use it |
| `currentJob` | SettingsViewModel | **SettingsViewModel** (shared) | Shared cancellation across import/export |
| `quizFileNames` / `wrongBookFileNames` / `favoriteFileNames` | SettingsViewModel | **FontSettingsCoordinator** | Set by `ensureSettingsCollectionsStarted()` |
| `cachedQuestionsSnapshot` | SettingsViewModel | **FontSettingsCoordinator** | Used by FillFilter refresh |
| `settingsReady` | SettingsViewModel | **FontSettingsCoordinator** | Set by `loadFontSettings()` |
| `messageResult` | SettingsViewModel | **SettingsViewModel** (shared) | Import + Export + Cancel all write to it |

---

## 4. COORDINATOR INTERFACES

```kotlin
// Thin: 25+ setters, each calls DataStore + emits
class FontSettingsCoordinator(
    private val dataStore: FontSettingsDataStore,  // not injected — passed context per call
) {
    // Owns: 30+ MutableStateFlow fields
    // Methods: loadFontSettings(context), ensureSettingsCollectionsStarted(...), 25+ setters
    fun loadFontSettings(context: Context)   // reads all from DataStore snapshot
    fun setFontSize(context: Context, size: Float)
    fun setExamQuestionCount(context: Context, count: Int)
    // ... 23 more setters
}

// Pure: takes questions + settings → produces filter summary
class FillQuestionFilterCoordinator(
    private val dataStore: FontSettingsDataStore,
) {
    fun publish(questions: List<Question>): FillQuestionFilterSummary
    // reads fill-related state flows from dataStore or param
}

// Orchestrator: URI→File→import, with progress tracking
class ImportCoordinator(
    private val questionRepository: QuestionRepository,
    private val wrongBookRepository: WrongBookRepository,
    private val favoriteRepository: FavoriteQuestionRepository,
) {
    fun importQuestionsFromUris(context: Context, uris: List<Uri>, onResult: ...)
    fun importQuestionsFromFiles(context: Context, files: List<File>, onResult: ...)
    fun importWrongBookFromUri(context: Context, uri: Uri, onResult: ...)
    fun importFavoritesFromUri(context: Context, uri: Uri, onResult: ...)
}

// Builder: 7 export flows + Excel sheet building utilities
class ExportCoordinator(
    private val questionRepository: QuestionRepository,
    private val wrongBookRepository: WrongBookRepository,
    private val favoriteRepository: FavoriteQuestionRepository,
    private val historyRepository: HistoryRepository,
    private val questionAnalysisRepository: QuestionAnalysisRepository,
    private val questionAskRepository: QuestionAskRepository,
    private val questionNoteRepository: QuestionNoteRepository,
) {
    fun exportQuestionsToFile(context: Context, uri: Uri, onResult: ...)
    fun exportQuestionsToExcelFile(context: Context, uri: Uri, fileName: String?, onResult: ...)
    // ... 5 more export flows
    // + private sheet-building helpers
}
```

---

## 5. EXTRACTION ORDER

```
Step 0: Baseline (compile + existing tests)
  │
Step 1: FillQuestionFilterCoordinator (C2, ~80 lines)      ← Pure logic, quick win
  │
Step 2: FontSettingsCoordinator (C1, ~300 lines)            ← 30+ StateFlows migrate out
  │
Step 3: ImportCoordinator (C3, ~250 lines)                  ← 2 flows + URI helpers
  │
Step 4: ExportCoordinator (C4, ~450 lines)                  ← Largest; 7 flows + Excel
  │
Final: ~80 lines (VM thin orchestrator) + ~1080 (coordinators) across 5 files
```

---

## 6. IMMUTABLE CONSTRAINTS

- All `StateFlow` property names and types exposed from VM — **never change** (Screen reads them directly)
- `settingsReady` emission timing — **never change** (practice/exam screens gate on this)
- `messageResult` type `StateFlow<LocalizedResult?>` — **never change**
- `cancelImportExport()` / `clearMessageResult()` public API — **never change**
- `FontSettingsDataStore` save/read format — **never change** (persisted on disk)
- `FillQuestionFilterSummary` structure — **never change** (consumed by SettingsScreen)

---

## 7. KEY DESIGN DECISIONS

1. **Delegation pattern** — VM delegates to coordinators via direct method calls; coordinators write to shared `MutableStateFlow` refs passed by VM
2. **ViewModel owns the scope** — `viewModelScope` stays in VM; coordinators are `suspend` functions or receive `CoroutineScope` param
3. **Same-package extraction** — `presentation.screen.settings/` directory, no module migration needed
4. **Backward-compatible** — all `StateFlow` property names stay identical on VM; `SettingsScreen` needs zero modifications
5. **FontSettingsCoordinator does NOT inject `Context`** — receives it per-call (Hilt best practice for ViewModel)
6. **ExportCoordinator is the largest** — 7 flows share POI import + sheet-building utilities; splitting further would fragment too much shared code
