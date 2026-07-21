<!--
  Derived from: PROJECT_SCAN_DEPENDENCY_REPORT.md, settings.gradle.kts, ARCHITECTURE.md
  Last synced: 2026-06-11 20:30 UTC+8
  Do not edit directly — regenerate when modules change.
-->

# Module Map

> Per-module summary of files, lines, responsibilities, and health.

---

## `:app` — Application Shell

| Metric | Value |
|--------|-------|
| **Files** | 120 |
| **Lines** | 15,707 (~52%) |
| **Health** | 🟡 IMPROVING — 从 84% 降到 52% |
| **Drift** | PracticeScreen + ExamScreen wrapper + ExamAISyncEffects 仍在此 |

| Key directories | Contents |
|----------------|----------|
| `presentation/screen/exam/` | ExamScreen wrapper + ExamAISyncEffects |
| `presentation/screen/practice/` | PracticeScreen |
| `presentation/screen/components/` | 37 共享 UI 组件 |
| `presentation/navigation/` | `AppNavHost.kt` — all routes |
| `presentation/viewmodel/` | AI analysis VMs (BaiduQianfan, DeepSeek, Spark) |
| `data/datastore/` | `FontSettingsDataStore.kt` |
| `data/network/` | `BaiduApiService`, `DeepSeekApiService`, `SparkApiService` |
| `di/` | Hilt modules |

**✅ Extracted to feature modules:**
- ExamViewModel → feature-exam ✅
- ExamScreenContent + 14 UI components → feature-exam ✅
- PracticeViewModel → feature-practice ✅
- Practice 6 Coordinators → feature-practice ✅
- 37 UseCase + facades → :domain ✅
- 6 util.* → :core ✅
- 12 UI/model files → :ui-common ✅
- FontSettingsRepository interface → :core ✅
- SessionEngine + ProgressManager + AnalysisLoader → :core ✅

**热点**: `PracticeScreen`(~414L, 留在 :app), `SettingsScreen.kt`(~487L)

---

## `:data` — Persistence & IO

| Metric | Value |
|--------|-------|
| **Lines** | 3,121 (49 files) |
| **Health** | 🟡 STABLE — well-scoped but `QuestionRepositoryImpl` overloaded |
| **Drift** | None (correct layer) |

| Key directories | Contents |
|----------------|----------|
| `repository/` | `QuestionRepositoryImpl`(1441L), `WrongBookRepositoryImpl`(394L), `FavoriteQuestionRepositoryImpl`(273L), analysis/note repos |

**Hotspots**: `QuestionRepositoryImpl`(1441L, 16 injected deps, 5 import formats)

---

## `:domain` — Business Logic

| Metric | Value |
|--------|-------|
| **Lines** | 1,193 (28 files) |
| **Health** | ✅ HEALTHY — correct dependency direction |
| **Drift** | D-002: `PracticeProgressScope` progress ID policy should be here |

| Key directories | Contents |
|----------------|----------|
| `model/` | `Question`, `PracticeProgress`, `ExamProgress` entities |
| `repository/` | Repository interfaces |
| `usecase/` | Domain use cases |
| `util/` | `AnswerUtils`, `FillQuestionTransformUtils`, constants |

---

## `:feature-practice` — Practice Feature Module

| Metric | Value |
|--------|-------|
| **Files** | 26 |
| **Lines** | 3,698 (~12%) |
| **Health** | ✅ ACTIVE — PracticeViewModel + UI 组件 + Coordinators |

**Key files:** PracticeViewModel, 6 Coordinators, PracticeProgressIndicator, PracticeDialogsHost, PracticeBottomToolbar, Localization

**Note:** PracticeScreen 留在 :app（6 个 ViewModel + R 阻塞）

---

## `:feature-exam` — Exam Feature Module

| Metric | Value |
|--------|-------|
| **Files** | 31 |
| **Lines** | 3,282 (~11%) |
| **Health** | ✅ ACTIVE — ExamViewModel + ExamScreenContent + 14 UI 组件 |

**Key files:** ExamViewModel, ExamScreenContent, ExamTopBar/OptionsList/AnalysisSection/BottomControls 等

**Note:** ExamScreen wrapper 保留在 :app（收集 StateFlow）

---

## `:ui-common` — Shared UI

| Metric | Value |
|--------|-------|
| **Files** | 12 |
| **Lines** | 807 (~3%) |
| **Health** | ✅ ACTIVE — AnswerCardGrid, FontStyleProvider, RichTextParser, InlineBlankTokenizer, TraceUtils, IconUtils 等 |

---

## `:core` — Core Utilities

| Metric | Value |
|--------|-------|
| **Files** | 13 |
| **Lines** | 1,148 (~4%) |
| **Health** | ✅ ACTIVE — SessionEngine, ProgressManager, AnalysisLoader, MemoryMode, Navigator + util.* + FontSettingsRepository 接口 |

