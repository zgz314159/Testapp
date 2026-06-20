<!--
  Derived from: L6 Architecture Design Phase 2026-06-13
  Target: QuestionRepositoryImpl.kt (Score 85, 1619 lines)
  Last synced: 2026-06-13 00:11 UTC+8
  Do not edit directly — re-run Architecture Design to regenerate.
  Frozen snapshot — 8 bounded contexts identified.
-->

# Architecture Design Report: QuestionRepositoryImpl.kt

> Target: Second-highest priority refactor candidate (Score 85/100, 1619 lines, 8 responsibilities, 16 injected deps).

---

## 1. RESPONSIBILITY BREAKDOWN (8 Bounded Contexts)

| # | Bounded Context | Pipeline Stage | Lines (est.) | Risk |
|---|-----------------|---------------|-------------|------|
| **C1** | Excel Parsing | Parse | ~600 | 🔴 5 row styles + header schema + images |
| **C2** | DOCX Parsing | Parse | ~200 | 🟠 Red-colored question detection + tables + images |
| **C3** | JSON Parsing | Parse | ~130 | 🟡 3 format variants (flat/atomic/index) |
| **C4** | SQLite Parsing | Parse | ~60 | 🟡 articles + segment_details join |
| **C5** | TXT Parsing | Parse | ~40 | 🟢 Pipe-delimited single format |
| **C6** | Metadata Management | Normalize | ~100 | 🟡 Edited-flag overlay for display |
| **C7** | Markdown Normalization | Normalize | ~60 | 🟢 Pure text transform |
| **C8** | CRUD Facade + Import Dispatch | Orchestrate | ~400 | 🔴 Depends on all C1-C7 + 16 daos/repos |

---

## 2. DEPENDENCY MATRIX

| Bounded Context | External Dependencies | Pure Logic? |
|-----------------|----------------------|-------------|
| C1: Excel | Apache POI, `java.io.File`, `quizStorageDir()` | ❌ |
| C2: DOCX | Apache POI, `java.io.File`, `quizStorageDir()`, `docxImageStorageDir()` | ❌ |
| C3: JSON | kotlinx.serialization, `java.io.File` | ❌ |
| C4: SQLite | `android.database.sqlite.SQLiteDatabase` (not Room) | ❌ |
| C5: TXT | `java.io.File` | ❌ |
| C6: Metadata | `java.io.File`, `quizStorageDir()` | ❌ |
| C7: Markdown | `QuestionEntity`, `MarkdownCleanupPreview` (domain models) | ❌ |
| C8: CRUD | All 11 DAOs, 3 Repos, DB, Initializer, C1-C7 parsers | ❌ |

**Key insight**: All parsers (C1-C5) share zero runtime state — they are pure `File → List<Question>` transforms. Extract first in parallel.

---

## 3. STATE OWNERSHIP MAP

| State | Current Owner | Suggested Owner | Rationale |
|-------|---------------|----------------|-----------|
| `importJson` instance | QuestionRepositoryImpl | **JsonQuestionParser** | Parser-owned config |
| `editedMetadataFile()` path | QuestionRepositoryImpl | **MetadataManager** | Metadata-exclusive |
| `quizStorageDir()` path | QuestionRepositoryImpl | **MetadataManager** | Shared by all parsers |
| `docxImageStorageDir()` path | QuestionRepositoryImpl | **DocxQuestionParser** | Parser-exclusive |
| `IMPORTED_FILL_*` regexes | QuestionRepositoryImpl companion | **ExcelQuestionParser** | Excel-exclusive |
| `INLINE_BLANK_PLACEHOLDER` | QuestionRepositoryImpl companion | **ExcelQuestionParser** | Excel-exclusive |
| `DuplicateFileImportException` | QuestionRepositoryImpl | Keep in repo | Shared by import dispatcher |
| `ImportFailedException` | QuestionRepositoryImpl | Keep in repo | Shared by import dispatcher |
| `ImportedQuestionPayload` | QuestionRepositoryImpl | Keep in repo | Shared inter-parser type |
| `AtomicSegmentPayload` | QuestionRepositoryImpl | **JsonQuestionParser** | JSON-exclusive |
| `AtomicArticlePayload` | QuestionRepositoryImpl | **JsonQuestionParser** | JSON-exclusive |
| `AtomicArticleIndexPayload` | QuestionRepositoryImpl | **JsonQuestionParser** | JSON-exclusive |

---

## 4. PARSER INTERFACE DESIGN

All parsers share a minimal contract:

```kotlin
interface QuestionFileParser {
    fun parse(file: File, originFileName: String): List<ImportedQuestionPayload>
}
```

```kotlin
interface SimpleQuestionFileParser {
    fun parse(file: File, originFileName: String): List<Question>
}
```

| Parser | Interface | Output Type |
|--------|-----------|-------------|
| ExcelQuestionParser | `QuestionFileParser` | `List<ImportedQuestionPayload>` (may carry analysis/notes) |
| DocxQuestionParser | `SimpleQuestionFileParser` | `List<Question>` |
| JsonQuestionParser | `QuestionFileParser` | `List<ImportedQuestionPayload>` |
| SqliteQuestionParser | `QuestionFileParser` | `List<ImportedQuestionPayload>` |
| TxtQuestionParser | `SimpleQuestionFileParser` | `List<Question>` |

---

## 5. EXTRACTION ORDER (Dependency Tree)

```
Step 0: Baseline (compile + test gate)
  │
Step 1: TxtQuestionParser (C5, simplest, ~40 lines)       ← Quick win
  │  Repo: 1619 → 1579
  │
Step 2: SqliteQuestionParser (C4, ~60 lines)              ← No Room, only raw SQLite
  │  Repo: 1579 → 1519
  │
Step 3: JsonQuestionParser (C3, ~130 lines)               ← Atomic article conversion included
  │  Repo: 1519 → 1389
  │
Step 4: DocxQuestionParser (C2, ~200 lines)               ← Complex but self-contained
  │  Repo: 1389 → 1189
  │
Step 5: ExcelQuestionParser (C1, ~600 lines)              ← Largest; 5 row styles + header schema
  │  Repo: 1189 → 589
  │
Step 6: MetadataManager (C6, ~100 lines)                  ← Needs quizStorageDir stable
  │  Repo: 589 → 489
  │
Step 7: MarkdownNormalizer (C7, ~60 lines)                ← Needs DAO
  │  Repo: 489 → 429
  │
Final: ~430 lines (CRUD facade) + ~1190 lines (7 extractors) across 8 files
```

---

## 6. IMMUTABLE CONSTRAINTS

- `QuestionRepository` interface signatures — **never change**
- `ImportedQuestionPayload` / `DuplicateFileImportException` / `ImportFailedException` — **never move** (shared inter-parser contract)
- `quizStorageDir()` path format — **never change**
- `editedMetadataFile()` path format — **never change**
- `docxImageStorageDir()` path format — **never change**
- All `IOConstants` keys used in LocalizedException — **never change**
- `companion object` regex constants (`IMPORTED_FILL_SPACE_REGEX`, `IMPORTED_FILL_BLANK_REGEX`, `INLINE_BLANK_PLACEHOLDER`) — move into ExcelQuestionParser

---

## 7. KEY DESIGN DECISIONS

1. **Interface pattern** (not abstract class) — parsers are stateless `File → List<Question>` transforms, perfect for interface extraction
2. **Same-package extraction** — `data.repository.parser/` directory, no module migration needed
3. **Backward-compatible** — `QuestionRepository` public API unchanged; import flow from `importFromFilesWithOrigin()` delegates to extracted parsers
4. **Supplemental data stays in repo** — `persistImportedSupplementalData()` couples to 3 analysis repos, stays in CRUD facade
5. **Import dispatcher stays in repo** — `importFromFilesWithOrigin()` switches on file extension, dispatches to extracted parsers
6. **Nested functions become top-level** — Excel parser's deeply nested `fun` declarations extract as private functions in the parser class
