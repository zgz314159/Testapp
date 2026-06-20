<!--
  Derived from: L6 Bloated File Scan 2026-06-13
  Last synced: 2026-06-13 13:08 UTC+8
  Do not edit directly — re-run L6 health scan to regenerate.
-->

# Bloated File Scan Report (2026-06-13)

> Full-picture scan before next bounded-context extraction.

## Size Hotspot Ranking

| Rank | File | Lines | Score | Responsibilities | Status |
|------|------|-------|-------|-----------------|--------|
| 🥇 1 | `PracticeViewModel.kt` | ~~2233~~ → **682** | ~~98~~ → **28** | 4+ coordinators remaining | ✅ **DONE** (4 new coordinators: Interaction/Artifact/Editor/Submit) |
| 🥈 2 | `QuestionRepositoryImpl.kt` | ~~1619~~ → **306** | ~~85~~ → **30** | 5 context done | ✅ **DONE** (7 extractors, 11 files) |
| 🥉 3 | `SettingsScreen.kt` | ~~1359~~ → **487** | ~~78~~ → **30** | 9 composables in settings/ui/ | ✅ **DONE** (9 composables: Basic/SoundDark/Memory/Exam/Practice/Fill/ImportExport/LoadingOverlay/ExportSourceSelection) |
| 4 | `PracticeScreen.kt` | ~~1278~~ → **1078** | ~~76~~ → **40** | 4 composables extracted | ✅ **DONE** (4 composables in practice/ subpackage) |
| 5 | `SettingsViewModel.kt` | ~~1178~~ → **416** | ~~74~~ → **25** | 4 coordinators extracted | ✅ **DONE** (4 coordinators: Font/Import/Export/Fill) |
| 6 | `ExamViewModel.kt` | ~~739~~ → **373** | ~~45~~ → **20** | 4 bounded contexts extracted | ✅ **DONE** (ExamState 118L + ProgressCoordinator 169L + ArtifactCoordinator 157L + Engine enhanced 167L, VM 373L) |
| 7 | `ExamScreen.kt` | ~417 | 35 (was 66) | 已拆 | EVO-03 Steps 1-4 ✅ |

## Bounded Context Split Roadmap

### 1. QuestionRepositoryImpl.kt (1619 lines, Score 85) — 5 contexts ✅ DONE

### 2. SettingsScreen.kt (~1359 lines, Score 78) — 4 contexts ✅ DONE

### 3. PracticeScreen.kt (~1278 lines, Score 76) — 3 contexts ✅ DONE

### 4. SettingsViewModel.kt (~1178 lines, Score 74) — 4 contexts ✅ DONE

### 5. ExamViewModel.kt (~739 lines, Score 45) — 4 bounded contexts ✅ DONE

| Context | Responsibility | Lines |
|---------|---------------|-------|
| `ExamState` | 22 StateFlows + 18 var fields + reset | 118 |
| `ExamProgressCoordinator` | Progress persistence (save/load/clear) | 169 |
| `ExamArtifactCoordinator` | Notes + Analysis CRUD + lazy loaders | 157 |
| `ExamMemoryModeEngine` (enhanced) | Round plan + orchestration (restore/init/refresh/advance) | 167 |
| `ExamViewModel` (remaining thin) | Orchestrator — delegates to all coordinators | 373 |

### 6. PracticeViewModel.kt (~2233 lines, Score 98) — 4 new coordinators + 6 existing ✅ DONE

| Context | Responsibility | Lines |
|---------|---------------|-------|
| `PracticeInteractionCoordinator` | Answer interaction (select/toggle/fill/retry/reopen) | 127 |
| `PracticeArtifactCoordinator` | Analysis + Notes CRUD (DeepSeek/Spark/Baidu/Note) | 169 |
| `PracticeEditorCoordinator` | Question editing (preview/save/delete/field mutations) | 278 |
| `PracticeSubmitCoordinator` | Exam submission flow (check/record/advance/end) | 61 |
| `PracticeViewModel` (remaining thin) | Orchestrator — delegates to all 10 coordinators | 682 |

**Total Phase G**: 4 new files, 635 new lines. VM 2233→682 lines (-69%).

## Recommended Execution Order

```
Phase A: QuestionRepositoryImpl  → ✅ DONE (7 extractors, 1619→306, 11 files)
Phase B: SettingsViewModel       → ✅ DONE (4 coordinators, 1178→416, public API preserved)
Phase C: PracticeScreen          → ✅ DONE (4 composables, 1277→1078, BUILD SUCCESSFUL)
Phase D: SettingsScreen          → ✅ DONE (9 composables, 1359→487, BUILD SUCCESSFUL)
Phase E: ExamViewModel           → ✅ DONE (4 bounded contexts, 739→373 VM + 611 new, BUILD SUCCESSFUL)
Phase G: PracticeViewModel       → ✅ DONE (4 new coordinators, 2233→682 VM + 635 new, BUILD SUCCESSFUL)
```

**Gate**: K-001 device smoke not yet run (bypassed in migration plan for pure logic steps).

---

## Round 2 Scan (2026-06-13)

After all Phase A-G bounded-context decompositions completed, a fresh full-project scan was run against the five core qualities: 短小 / 无状态 / 单一数据流 / 面向管道 / 职责边界清晰.

**New ranking**: See [bloat_scan_report_20260613_round2.md](bloat_scan_report_20260613_round2.md)

**Recommended Phase H**: HomeScreen.kt (1068→300, 6 composables) — 2/10, worst offender in Round 2.
