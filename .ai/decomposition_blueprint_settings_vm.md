<!--
  Derived from: L6 Decomposition Blueprint 2026-06-13
  Target: SettingsViewModel.kt → 4 Coordinators + 1 thin VM
  Last synced: 2026-06-13 06:15 UTC+8
  Do not edit directly — re-run Decomposition to regenerate.
-->

# Decomposition Blueprint: SettingsViewModel.kt

> Extracting 4 coordinators from one Settings ViewModel.
> Pattern: VM owns `MutableStateFlow` + `viewModelScope`; coordinators are stateless or delegate-wrapped.
> Directory: `app/src/main/java/com/example/testapp/presentation/screen/settings/`

---

## COORDINATOR #1: FillQuestionFilterCoordinator (Step 1)

**Why first**: Pure logic (~80 lines), zero repository deps, quick win.

```kotlin
// settings/FillQuestionFilterCoordinator.kt
class FillQuestionFilterCoordinator {
    fun publish(
        questions: List<Question>, fillBlankCount: Int,
        generationMode: FillQuestionGenerationMode, fullAnswerRandomOrder: Boolean,
        minAnswerScore: Int, maxAnswerScore: Int, answerTagFilter: String
    ): FillQuestionFilterSummary

    fun refreshTags(questions: List<Question>): List<String>
}
```

**Moved**: `publishFillQuestionFilterSummary()` + `refreshFillQuestionFilterSummary()` logic + `_availableFillAnswerTags` computation.

**Extracted lines**: ~80 | **VM after**: ~1098

---

## COORDINATOR #2: FontSettingsCoordinator (Step 2)

**Why second**: 30+ StateFlows + 25 setters — migrate out to shrink VM significantly.

```kotlin
// settings/FontSettingsCoordinator.kt
class FontSettingsCoordinator() {
    fun loadFontSettings(context: Context)
    fun ensureCollectionsStarted(
        questionRepository: QuestionRepository,
        wrongBookRepository: WrongBookRepository,
        favoriteRepository: FavoriteQuestionRepository,
        scope: CoroutineScope
    )
    fun setFontSize(context: Context, size: Float)
    // ... 23 more setters
}
```

**Extracted lines**: ~300 | **VM after**: ~798

---

## COORDINATOR #3: ImportCoordinator (Step 3)

**Why third**: 4 import flows + URI helper.

```kotlin
// settings/ImportCoordinator.kt
class ImportCoordinator(
    private val questionRepository: QuestionRepository,
    private val wrongBookRepository: WrongBookRepository,
    private val favoriteRepository: FavoriteQuestionRepository,
) {
    suspend fun importQuestionsFromUris(context: Context, uris: List<Uri>, ...)
    suspend fun importQuestionsFromFiles(context: Context, files: List<File>, ...)
    suspend fun importWrongBookFromUri(context: Context, uri: Uri, ...)
    suspend fun importFavoritesFromUri(context: Context, uri: Uri, ...)
}
```

**Extracted lines**: ~250 | **VM after**: ~548

---

## COORDINATOR #4: ExportCoordinator (Step 4)

**Why fourth**: Largest (450 lines), 7 export flows + Excel sheet building + POI.

```kotlin
// settings/ExportCoordinator.kt
class ExportCoordinator(
    private val questionRepository: QuestionRepository,
    private val wrongBookRepository: WrongBookRepository,
    private val favoriteRepository: FavoriteQuestionRepository,
    private val historyRepository: HistoryRepository,
    private val questionAnalysisRepository: QuestionAnalysisRepository,
    private val questionAskRepository: QuestionAskRepository,
    private val questionNoteRepository: QuestionNoteRepository,
) {
    suspend fun exportQuestionsToFile(...)
    suspend fun exportQuestionsToExcelFile(...)
    // ... 5 more export flows
    // + private sheet-building helpers (~200 lines)
}
```

**Extracted lines**: ~450 | **VM after**: ~98

---

## POST-EXTRACTION VM (Step 5: Final)

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val fontSettings: FontSettingsCoordinator,
    private val fillFilter: FillQuestionFilterCoordinator,
    private val importCoordinator: ImportCoordinator,
    private val exportCoordinator: ExportCoordinator,
) : ViewModel() {
    // 30+ delegated StateFlows (unchanged from Screen perspective)
    val fontSize: StateFlow<Float> = fontSettings.fontSize
    // ...
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // ...
    fun importQuestionsFromUris(...) { currentJob = viewModelScope.launch { importCoordinator... } }
}
```

**Lines**: ~1178 → ~98 (VM) + ~1080 (4 coordinators) across 5 files.

---

## VALIDATION PER STEP

| Step | Compile | Test Gate |
|------|---------|-----------|
| 1 (C2: FillFilter) | ✅ | Manual: fill filter UI |
| 2 (C1: FontSettings) | ✅ | Settings load + persist |
| 3 (C3: Import) | ✅ | Import flow smoke |
| 4 (C4: Export) | ✅ | Export flow smoke |

---

## HILT DI CHANGES

```kotlin
@Provides @Singleton fun provideFillQuestionFilterCoordinator() = FillQuestionFilterCoordinator()
@Provides @Singleton fun provideFontSettingsCoordinator() = FontSettingsCoordinator()
@Provides @Singleton fun provideImportCoordinator(qr: ..., wbr: ..., fr: ...) = ImportCoordinator(qr, wbr, fr)
@Provides @Singleton fun provideExportCoordinator(qr: ..., wbr: ..., fr: ..., hr: ..., qar: ..., qsk: ..., qnr: ...) = ExportCoordinator(qr, wbr, fr, hr, qar, qsk, qnr)
```
