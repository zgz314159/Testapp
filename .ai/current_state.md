<!--
  Derived from: CURRENT_STATE.md, 2_SYSTEM_HEALTH_ENGINE.md
  Last synced: 2026-06-19 23:55 UTC+8
  Do not edit directly — regenerate from source when source changes.
  Agent: read this file first for analysis context.
-->

# Current State (Memory Layer)

> **Single entry point for agents.** Canonical state lives here; root `CURRENT_STATE.md` is the source-of-truth synced here.

## Active tracks

| Track | Doc | Status |
|-------|-----|--------|
| Practice navigation + persistence remediation | `PRACTICE_NAVIGATION_REMEDIATION_TODO.md` | Code + unit tests **done**; **1 device smoke** open |
| Modular refactor (12 items) | `REFACTOR_TODO.md` | Items 1, 3, 6 done; **blocked until remediation VERIFY** |
| L6 Self-Evolving OS | `LEVEL6_OVERVIEW.md` + 10 engine files | **Deployed** 2026-06-11 |
| Memory Layer | `..ai/*.md` (this file) | **Initialized** 2026-06-11 |
| 🆕 Phase 5 模块迁移 | [refactoring_plan.md](refactoring_plan.md) | **~90% 完成** — feature-practice(26files) + feature-exam(31files) + :core(13files) + :ui-common(12files) |
| 🆕 ExamScreen 分离 | ExamScreenContent + Wrapper 模式 | **完成** — :app 薄包装层保留，纯 UI 在 feature-exam |
| 🆕 PracticeScreen 迁移 | PracticeScreen | **阻塞** — 6 个 :app ViewModel + R + Context |
| 🆕 import 清理 + 残留删除 | 5 files import 修复 + .history/ 删除 | **完成** |
| 🆕 2026-06-14 剩余优化 | [refactoring_plan.md](refactoring_plan.md) Phase 7-9 | **完成** — Settings Facade + Exam coordinators + DataStore delegates |
| 🆕 2026-06-14 架构债务清理执行 | TASK_LOG.md | **完成** — tests restored, API keys externalized, Markdown normalizer merged, feature boundaries cleaned, components split, Practice/Exam/Settings pipelines extracted, release build stabilized |
| 🆕 2026-06-19 答题界面4项回退修复 | TASK_LOG.md → EXEC-20260619-01 | **完成** — 审查确认 Issues 1-3 已预修复; Issue 4 修复(AppNavHost/SparkAskScreen/BaiduAskScreen) |

## System health: **🟡 IMPROVING**

| Indicator | Value | Threshold |
|-----------|-------|-----------|
| Coupling score | ~52% in `:app` (from 84%) | 🟡 IMPROVING |
| God file count | 0 (>1000 lines in :app) | ✅ OK |
| Module independence | 4/8 active (from 4/8 empty) | 🟡 IMPROVING |
| Duplication (Practice↔Exam) | REDUCED (SessionEngine shared) | 🟢 LOW |
| Dependency redline | Settings VM 12 deps → 6 injected deps | ✅ OK |
| Test coverage | LOW-MEDIUM | ⚠️ LOW-MEDIUM |
| Regression frequency | ~8 bugs/30d | ⚠️ MEDIUM |
| KB stability | Stable | ✅ OK |
| Pipeline depth | Settings action pipeline + VM coordinators | ✅ OK |

**Allowed**: Small-scale incremental refactor only. **Forbidden**: Large-scale restructuring.

## What is stable

- Practice navigation: unified navigation state container
- Practice progress: scoped ids; home aggregation; async save ordering (unit tested)
- Exam progress: scoped ids mirrored
- SettingsViewModel: Repository dependencies aggregated via `SettingsRepositoryFacade`
- Settings import/export: `SettingsActionPipeline` owns loading/progress/message/cancel lifecycle
- PracticeViewModel: progress lifecycle extracted to `PracticeProgressLifecycleCoordinator`
- ExamViewModel: edit/grade/statistics plus artifact/reset residual contexts extracted
- UI boundaries: shared question edit/navigation components live in `:ui-common`; app no longer imports feature-exam private UI
- API keys: `BuildConfig` reads from `local.properties` / env vars; hard-coded source secrets removed
- FontSettingsDataStore: generic preference delegates introduced; external API preserved
- Regression coverage: `PracticeViewModelTest`, `HomeViewModelTest`, `ProgressScopeTest`
- **AI结果保存立即显示**: `AppNavHost` `onSave` 回调改用 `appendNoteSuspend` 挂起版本; `SparkAskScreen`/`BaiduAskScreen` 改为 suspend 并等待保存完成再 `popBackStack`

## What is not done

1. **Device smoke** (K-001 HIGH): home-card persistence + atomic-bank re-entry
2. **CI/lint**: ktlint/detekt 未配置
3. **Device route smoke**: Exam route still needs manual verification
4. **Gesture deprecation migration**: Swipeable/FractionalThreshold replacement deferred; it changes gesture behavior

## Size hotspots (2026-07-04 — Phase 42 后)

全仓库 >500 行文件 **已清零**。近期拆分：

| 文件 | 行数 | Phase |
|------|------|-------|
| `ExcelQuestionParser.kt` | ~95 | 42 |
| `PracticeBasicComponents.kt` | 已拆 | 41 |
| `RichText.kt` | ~80 | 40 |
| `PracticeViewModel.kt` | ~461 | 39 |
| `ExamViewModel.kt` | ~471 | 38 |
| `PracticeScreen.kt` | ~390 | 35 |

全量清单 → `.ai/loc_audit.md`

## Modules (Gradle)

`:app` (120 files, 15707 lines, ~52%) → `:domain` (69 files, 1941 lines), `:data` (57 files, 3676 lines)
`:feature-practice` (26 files, 3698 lines, ~12%)
`:feature-exam` (31 files, 3282 lines, ~11%)
`:core` (13 files, 1148 lines, ~4%)
`:ui-common` (12 files, 807 lines, ~3%)
`:baseline-profile` — performance baseline

## Agent workflow position

| Phase | Status |
|-------|--------|
| ANALYZE | Done (2026-06-11) |
| PLAN | Phase 1-6 Plan completed |
| EXECUTE | Phase 1-9 + architecture debt cleanup + 2026-06-19 bugfix ✅ done |
| VERIFY | `.\gradlew.bat build` + `:app:assembleRelease` + `:app:testDebugUnitTest` ✅ |

## Do not do without explicit plan

- Large behavioral gesture/navigation migrations while K-001 device smoke is open
- Add page/file/mode-specific if patches
- Full-project scan or re-read all *_SUMMARY.md

## Quick verify commands

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.PracticeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.HomeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.ProgressScopeTest"
./gradlew build
./gradlew :app:assembleRelease
```
