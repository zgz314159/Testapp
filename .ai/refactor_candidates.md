<!--
  Derived from: L6 Refactor Candidate Analysis 2026-06-11
  Last synced: 2026-06-11 20:30 UTC+8
  Do not edit directly — re-run L6 health scan to regenerate.
  Frozen snapshot — scores reflect 2026-06-11 analysis.
-->

# Refactor Candidates (Ranked)

> Priority ranking from 2026-06-11 Refactor Candidate Analysis.
> Scores derived from: file size × responsibility count × coupling level × module violation.

---

| Rank | File | Module | Lines | Responsibilities | Injected Deps | Coupling | Score |
|------|------|--------|-------|-----------------|---------------|----------|-------|
| 🥇 1 | `PracticeViewModel.kt` | `:app` | **~3230** (was ~3900) | 4+ (已抽6个 + radical Phase 1) | 15+ | 🔴 EXTREME | **98** |
| 🥈 2 | `ExamViewModel.kt` | `:app` | ~2455 | 6+ (与Practice高度平行) | 12+ | 🔴 VERY HIGH | **92** |
| 🥉 3 | `QuestionRepositoryImpl.kt` | `:data` | 1441 | 5 (JSON/SQLite/Excel/DOCX/TXT) | 16 | 🔴 HIGH | **85** |
| 4 | `SettingsScreen.kt` | `:app` | ~1218 | 4 (设置UI/导入UI/导出UI/文件浏览) | 隐式 | 🟠 HIGH | **78** |
| 5 | `PracticeScreen.kt` | `:app` | ~1165 | 3 (题目渲染/答题交互/导航手势) | 隐式(VM全状态) | 🟠 HIGH | **76** |
| 6 | `SettingsViewModel.kt` | `:app` | ~1029 | 4 (字体设置/导入逻辑/导出逻辑/文件管理) | 8+ | 🟠 HIGH | **74** |
| 7 | `ExamScreen.kt` | `:app` | **390** (was 868) | 1 (Compose编排) | 隐式 | 🟢 LOW | **35** (was 66) |
| 8 | `HomeScreen.kt` | `:app` | 726 | 3 | 隐式 | 🟡 MEDIUM | **63** |
| 9 | `AppNavHost.kt` | `:app` | 675 | 1 (路由) | 所有Screen | 🟡 MEDIUM | **60** |
| 10 | `WrongBookRepositoryImpl.kt` | `:data` | 394 | 3 | 4 | 🟡 LOW-MEDIUM | **50** |
| 11 | `FontSettingsDataStore.kt` | `:app` | 374 | 1 | DataStore | 🟢 LOW | **38** |

---

## Detailed breakdown

### 🥇 #1: PracticeViewModel.kt — Score 98/100

| Dimension | Detail |
|-----------|--------|
| **Known debt** | K-002 (HIGH severity) |
| **Growth rate** | +998 lines since baseline scan |
| **Responsibilities** | 7 distinct domains in one file |
| **Coupling** | Directly coupled to 3 subsystems (Practice, Persistence, Home) |
| **Suggested decomposition** | Extract NavigationCoordinator → AnswerHandler → ProgressCoordinator → RandomModeCoordinator → FullAnswerCoordinator (5-7 PRs) |
| **Gate** | Must pass K-001 device smoke first |

### 🥈 #2: ExamViewModel.kt — Score 92/100

| Dimension | Detail |
|-----------|--------|
| **Known debt** | K-003 (MEDIUM severity) |
| **Duplicate of** | 60-70% code similarity with #1 |
| **Suggested decomposition** | Reuse PracticeVM extraction pattern AFTER #1 proven |

### 🥉 #3: QuestionRepositoryImpl.kt — Score 85/100

| Dimension | Detail |
|-----------|--------|
| **Known debt** | Implicit in D-001 |
| **Dependencies** | 16 injected (11 DAOs + 3 Repos + DB + Initializer) |
| **Public API** | 18 functions |
| **Suggested decomposition** | ExcelImportParser / JsonImportExport / DocxParser / QuestionCrudFacade |

---

## Recommended execution order

```
1. PracticeViewModel.kt    ← Immediate priority (largest risk, still growing)
2. PracticeScreen.kt        ← Synchronize with #1 (VM↔Screen coupling)
3. ExamViewModel.kt         ← Reuse proven pattern from #1
4. SettingsViewModel.kt     ← Extract import/export coordinators
5. QuestionRepositoryImpl.kt ← Split by file format
6. ExamScreen.kt / HomeScreen.kt / AppNavHost.kt ← Lower ROI
```

**Prerequisite to any code change: K-001 device smoke must pass.**

---

## Status (2026-06-11)

| Phase | Status | Artifact |
|-------|--------|----------|
| Architecture Design | ✅ Done | [.ai/architecture_design_report.md](architecture_design_report.md) |
| Decomposition Blueprint | ✅ Done | [.ai/decomposition_blueprint.md](decomposition_blueprint.md) |
| Step 5 Execution | ✅ Done | PracticeFullAnswerCoordinator.kt |
| Step 6 Execution | ✅ Placeholder | PracticeSessionCoordinator.kt (deferred) |
| Radical Phase 1 | ✅ Done | ModeCoordinator now holds _sessionState, 4 state-mutating methods migrated |

**Extraction plan**: 6 coordinators (Navigation→Answer→Progress→Mode→FullAnswer→Session), 3900→2300 lines across 7 files.
