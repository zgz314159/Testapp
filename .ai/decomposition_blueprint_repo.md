<!--
  Derived from: L6 Decomposition Blueprint 2026-06-13
  Target: QuestionRepositoryImpl.kt → 5 Parsers + 2 Normalizers + 1 Facade
  Last synced: 2026-06-13 00:11 UTC+8
  Do not edit directly — re-run Decomposition to regenerate.
-->

# Decomposition Blueprint: QuestionRepositoryImpl.kt

> Extracting 7 components from one God Repository.
> Pattern: All parsers implement `QuestionFileParser` or `SimpleQuestionFileParser`.
> Directory: `data/src/main/java/com/example/testapp/data/repository/parser/`

---

## PARSER #1: TxtQuestionParser (Step 1)

**Why first**: Simplest parser (~40 lines), zero framework deps, quick win.

```kotlin
// parser/TxtQuestionParser.kt
class TxtQuestionParser : SimpleQuestionFileParser {
    override fun parse(file: File, originFileName: String): List<Question>
}
```

**Scope**: `parseTxtQuestions()` function body. Pipe-delimited (`|`) line parser, 11+ fields per line.

**Extracted lines**: ~40 | **Repo after**: 1579

**Verification**: `./gradlew :data:compileDebugKotlin` + `TxtQuestionParserTest`

---

## PARSER #2: SqliteQuestionParser (Step 2)

**Why second**: Simple raw SQLite read (~60 lines), no Room dependency, self-contained.

```kotlin
// parser/SqliteQuestionParser.kt
class SqliteQuestionParser : QuestionFileParser {
    override fun parse(file: File, originFileName: String): List<ImportedQuestionPayload>
}
```

**Dependencies**: `android.database.sqlite.SQLiteDatabase` (raw, not Room), `AtomicArticlePayload` (shared model)

**Extracted lines**: ~60 | **Repo after**: 1519

**Verification**: `./gradlew :data:compileDebugKotlin` + `SqliteQuestionParserTest`

---

## PARSER #3: JsonQuestionParser (Step 3)

**Why third**: 3 format variants (flat Question list, AtomicArticle list, AtomicArticleIndex), includes `AtomicArticlePayload.toImportedQuestionPayload()` conversion.

```kotlin
// parser/JsonQuestionParser.kt
class JsonQuestionParser : QuestionFileParser {
    override fun parse(file: File, originFileName: String): List<ImportedQuestionPayload>
}
```

**Moved types**: `AtomicSegmentPayload`, `AtomicArticlePayload`, `AtomicArticleIndexEntryPayload`, `AtomicArticleIndexPayload`

**Moved function**: `AtomicArticlePayload.toImportedQuestionPayload()` extension → private method in parser

**Extracted lines**: ~130 | **Repo after**: 1389

**Verification**: `./gradlew :data:compileDebugKotlin` + `JsonQuestionParserTest`

---

## PARSER #4: DocxQuestionParser (Step 4)

**Why fourth**: Complex self-contained parser (~200 lines), red-color question detection, tables, embedded images.

```kotlin
// parser/DocxQuestionParser.kt
class DocxQuestionParser() : SimpleQuestionFileParser {
    override fun parse(file: File, originFileName: String): List<Question>
}
```

**Dependencies**: Apache POI `XWPFDocument`, `quizStorageDir()`, `docxImageStorageDir()`

**Moved functions**:
- `parseDocxQuestions()` body → `parse()`
- `docxImageStorageDir()` helper → private in parser
- `buildDrawingAnswerWithImages()` → private in parser
- `isRedColor()`, `isParagraphRedColored()` → private in parser
- `extractTableAsString()` → private in parser

**Extracted lines**: ~200 | **Repo after**: 1189

**Verification**: `./gradlew :data:compileDebugKotlin` + `DocxQuestionParserTest`

---

## PARSER #5: ExcelQuestionParser (Step 5)

**Why fifth**: Largest parser (~600 lines), 5 fallback row styles + header schema detection + embedded images. Extract after C1-C4 stable.

```kotlin
// parser/ExcelQuestionParser.kt
class ExcelQuestionParser(
    private val imageStorageDir: File
) : QuestionFileParser {
    override fun parse(file: File, originFileName: String): List<ImportedQuestionPayload>
}
```

**Moved companion constants**: `IMPORTED_FILL_SPACE_REGEX`, `IMPORTED_FILL_BLANK_REGEX`, `INLINE_BLANK_PLACEHOLDER`

**Moved nested functions** (become private members):
- `cellText()`, `contentCellText()`, `rowValues()`, `normalizeHeader()`,
- `isOptionHeader()`, `isAnswerPartHeader()`, `extractIndexedHeaderNumber()`,
- `normalizeScoreLabel()`, `buildAnnotatedAnswerPart()`
- `ExcelAnswerPartSlot`, `ExcelHeaderSchema`
- `matchesAnyHeader()`, `detectWorkbookShortAnswerHint()`, `detectHeaderSchema()`
- `resolveType()`, `normalizeOptionsForType()`, `normalizeImportedFillContent()`
- `parseRowByHeader()`, `parseLegacyCalculationRow()`, `parseRowStyle1()`,
- `parseFillTemplateRow()`, `parseRowStyle2()`, `parseRowStyle3()`
- `extractEmbeddedExcelImages()`

**Extracted lines**: ~600 | **Repo after**: 589

**Verification**: `./gradlew :data:compileDebugKotlin` + `ExcelQuestionParserTest`

---

## EXTRACTOR #6: MetadataManager (Step 6)

**Why sixth**: Edited-flag overlay + edit metadata persistence. Needs `quizStorageDir()` stable.

```kotlin
// parser/MetadataManager.kt
class MetadataManager {
    fun quizStorageDir(): File
    fun editedMetadataFile(fileName: String): File
    fun loadEditedQuestionIds(fileName: String?): Set<Int>
    fun writeEditedQuestionIds(fileName: String, questions: List<Question>)
    fun overlayEditedFlags(questions: List<Question>): List<Question>
}
```

**Extracted lines**: ~100 | **Repo after**: 489

**Verification**: `./gradlew :data:compileDebugKotlin`

---

## EXTRACTOR #7: MarkdownNormalizer (Step 7)

**Why seventh**: Pure entity transform, depends on QuestionEntity/QuestionRepository domain types.

```kotlin
// repository/MarkdownNormalizer.kt
class MarkdownNormalizer(private val dao: QuestionDao) {
    fun buildCleanupPreview(entity: QuestionEntity): MarkdownCleanupPreview?
    suspend fun normalizeStored(entity: QuestionEntity): QuestionEntity?
    fun QuestionEntity.normalizeMarkdownFields(): QuestionEntity
    fun QuestionEntity.markdownCleanupSnippet(): String
}
```

**Extracted lines**: ~60 | **Repo after**: 429

---

## POST-EXTRACTION REPO STRUCTURE (Step 8: Final)

```kotlin
class QuestionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: QuestionDao,
    private val favoriteDao: FavoriteQuestionDao,
    // ... 12 more daos/repos ...
    private val initializer: QuestionDataInitializer,
    // NEW: extracted components
    private val txtParser: TxtQuestionParser,
    private val sqliteParser: SqliteQuestionParser,
    private val jsonParser: JsonQuestionParser,
    private val docxParser: DocxQuestionParser,
    private val excelParser: ExcelQuestionParser,
    private val metadataManager: MetadataManager,
    private val markdownNormalizer: MarkdownNormalizer,
) : QuestionRepository {

    // importFromFilesWithOrigin() delegates to parser by extension:
    //   "xls"/"xlsx" → excelParser.parse()
    //   "docx"       → docxParser.parse().map(::ImportedQuestionPayload)
    //   "json"       → jsonParser.parse()
    //   "sqlite"/"db"→ sqliteParser.parse()
    //   "txt"        → txtParser.parse().map(::ImportedQuestionPayload)
    //   else         → fallback chain as before

    // getQuestions() / getQuestionFileNames() → dao + metadataManager.overlayEditedFlags()
    // previewMarkdownCleanup() / normalizeStoredMarkdown() → markdownNormalizer
    // saveQuestionsToJson() → metadataManager.writeEditedQuestionIds()
    // deleteFileAndRelatedData() → unchanged (CRUD orchestration)
}
```

**Lines**: 1619 → ~430 (CRUD facade) + ~1190 (7 extractors) across 8 files.

---

## VALIDATION PER STEP

| Step | Compile | Unit Test | Integration Check |
|------|---------|-----------|-------------------|
| 1 (C5: TXT) | ✅ data:compileDebugKotlin | ✅ TxtQuestionParserTest | ⚠️ TXT import smoke |
| 2 (C4: SQLite) | ✅ data:compileDebugKotlin | ✅ SqliteQuestionParserTest | ⚠️ SQLite import smoke |
| 3 (C3: JSON) | ✅ data:compileDebugKotlin | ✅ JsonQuestionParserTest | ⚠️ JSON import smoke |
| 4 (C2: DOCX) | ✅ data:compileDebugKotlin | ✅ DocxQuestionParserTest | ⚠️ DOCX import smoke |
| 5 (C1: Excel) | ✅ data:compileDebugKotlin | ✅ ExcelQuestionParserTest | ⚠️ Excel import smoke |
| 6 (C6: Metadata) | ✅ data:compileDebugKotlin | ✅ | ⚠️ Edited flag display |
| 7 (C7: Markdown) | ✅ data:compileDebugKotlin | ✅ | ⚠️ Cleanup preview |
| 8 (Final) | ✅ data:compileDebugKotlin | ✅ All tests | ✅ Full import smoke |

---

## HILT DI MODULE CHANGES

```kotlin
// app/di/RepositoryModule.kt — ADD:
@Provides @Singleton
fun provideTxtQuestionParser(): TxtQuestionParser = TxtQuestionParser()

@Provides @Singleton
fun provideSqliteQuestionParser(): SqliteQuestionParser = SqliteQuestionParser()

@Provides @Singleton
fun provideJsonQuestionParser(): JsonQuestionParser = JsonQuestionParser()

@Provides @Singleton
fun provideDocxQuestionParser(): DocxQuestionParser = DocxQuestionParser()

@Provides @Singleton
fun provideExcelQuestionParser(): ExcelQuestionParser = ExcelQuestionParser(
    imageStorageDir = File("/data/data/com.example.testapp/files/quiz/")
)

@Provides @Singleton
fun provideMetadataManager(): MetadataManager = MetadataManager()

@Provides @Singleton
fun provideMarkdownNormalizer(dao: QuestionDao): MarkdownNormalizer = MarkdownNormalizer(dao)
```
