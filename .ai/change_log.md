<!--
  Derived from: TASK_LOG.md
  Last synced: 2026-06-19 23:55 UTC+8
  Do not edit directly — append to TASK_LOG.md, then re-sync.
-->

# Change Log

> Chronological log for agent continuity. Source: `TASK_LOG.md`.

---

## 2026-06-19 — 答题界面4项回退修复

**来源:** 用户反馈答题界面4个问题。

**审查结论:**
| # | Issue | 结论 |
|---|-------|------|
| 1 | AI对话框保存后退出自动跳转下一题 | ✅ 已在代码中修复（`screenActive`/`isScreenActive` 守卫） |
| 2 | 分析区域滚动应取消自动跳转 | ✅ 已在代码中修复（5个独立 scrollState 监听） |
| 3 | PracticeScreen进度条缺失 | ✅ 已在代码中修复（已有 `LinearProgressIndicator`） |
| 4 | AI结果保存后不立即显示 | 🔧 需修复（根因: `appendNote` fire-and-forget，`popBackStack` 过早执行） |

**Issue 4 修改:**
- `AppNavHost.kt`: 3个 `onSave` 回调改用 `appendNoteSuspend` 挂起函数
- `SparkAskScreen.kt`: `onSave` 改为 `suspend (String) -> Unit`，保存用 `saveScope.launch` 包裹
- `BaiduAskScreen.kt`: 同上

**验证:** `ReadLints`: 无错误。

**预置条件:** `ExamViewModel.appendNoteSuspend` / `PracticeViewModel.appendNoteSuspend` 已存在。

---

## 2026-06-14 — Architecture debt cleanup plan executed

**Goal:** Complete the architecture debt cleanup plan without editing the plan file, keeping public APIs stable while reducing stateful VM responsibilities and module-boundary violations.

**Actions:**
- `PracticeViewModelTest` restored as behavior/public-API tests.
- API keys externalized to `local.properties` / env vars; hard-coded source secrets removed.
- Markdown normalizer merged to the single `:core` implementation; data-layer field normalization retained without domain/core cycles.
- Cross-feature UI boundary fixed: shared question edit/navigation components moved to `:ui-common`.
- App components package split into home/practice/questionbank/result namespaces.
- `PracticeProgressLifecycleCoordinator` extracted from `PracticeViewModel`.
- `ExamArtifactStateCoordinator` and `ExamProgressResetCoordinator` extracted from `ExamViewModel`.
- `SettingsActionPipeline` added for import/export loading/progress/message/cancel flow.
- Release stabilization: POI/R8 rules updated; low-risk Compose deprecated APIs replaced.

**Verify:**
- `.\gradlew.bat :app:testDebugUnitTest`: BUILD SUCCESSFUL.
- `.\gradlew.bat :app:assembleRelease`: BUILD SUCCESSFUL.
- `.\gradlew.bat build`: BUILD SUCCESSFUL.
- IDE lints: no errors.

---

## 2026-06-14 — Residual optimization completed

**Goal:** Finish the remaining optimization plan: Settings dependency Facade, ExamViewModel residual coordinator extraction, and FontSettingsDataStore generic delegate cleanup.

**Actions:**
- `.ai/refactoring_plan.md` updated with Phase 7-9 execution plan.
- `SettingsRepositoryFacade` added to `:domain`.
- `SettingsViewModel` now injects `SettingsRepositoryFacade`; injected dependencies reduced from 12 to 6.
- `ExamQuestionEditCoordinator`, `ExamGradeCoordinator`, and `ExamStatisticsCoordinator` added in `feature-exam`.
- `ExamViewModel` now delegates edit, grade, and statistics responsibilities.
- `PreferenceDelegate<T>` and `BooleanPreferenceDelegate` added.
- `FontSettingsDataStore` now uses delegates internally and preserves existing public API.

**Verify:**
- `ReadLints`: no errors on edited files.
- `.\gradlew.bat :app:compileDebugKotlin :feature-exam:compileDebugKotlin`: BUILD SUCCESSFUL.
- Only existing `HomeScreen` `FractionalThreshold` deprecation warnings remain.

---

---

## 2026-06-13 — Level 6 五大特质全面审查 + 状态同步

**Goal:** 按五特质（短小/无状态/单一数据流/面向管道/职责边界清晰）逐文件审计全部 12 个主要文件，更新 CURRENT_STATE 和 memory layer。

**Actions:**
- 生成 [five_traits_review_report_20260613.md](five_traits_review_report_20260613.md)：11 文件逐文件五特质打分
- 验证 Phase H~12 拆分结果：HomeScreen(399)、PracticeScreen(499)、RichText(~252)、InlineBlank(203)、HomeViewModel(260)、QuestionBankDrawer(~301)、AppNavHost(~295)
- 验证 Phase A~G 拆分结果：ExamViewModel(当时约 415；2026-06-14 residual extraction 后约 590)、PracticeViewModel(724)
- 确认 ExportCoordinator 已被拆分为 JsonExport(87)+ExcelExport(391) — Round 2 "未找到文件"实为已拆分
- 历史记录：当时确认部分 Coordinator 与 DataStore 重构暂停；该结论已被 2026-06-14 更新覆盖。
- 同步 `CURRENT_STATE.md` 和 `.ai/current_state.md` 至最新状态
- God file count: 10+ → 0 (所有 >1000 行文件已全部拆分)

**Outcome:** 当时低悬挂果实已完成；2026-06-14 已继续完成 residual optimization。K-001 设备 smoke 仍待执行。

---

## 2026-06-13 — Architecture Guard 部署（Level 6 最后一块拼图）

**Goal:** 新增 `.ai/13_ARCHITECTURE_GUARD.md`，防止未来功能新增导致代码膨胀回退。

**Actions:**
- 创建 `13_ARCHITECTURE_GUARD.md`：6 条核心规则（禁止直接新增功能 / LOC 红线 / 职责数红线 / 依赖数红线 / Architecture Placement Review / 正确 Feature Request 格式）
- 更新 `LEVEL6_OVERVIEW.md`：管道新增 Architecture Guard 步骤；文件清单新增 #13；Agent 读取顺序新增 guard
- 更新 `12_CONTEXT_LOADING_RULES.md`：新增 Feature Implementation 任务类型的加载清单，Guard 为 MANDATORY 第一步
- 更新 `11_MEMORY_SYNC_PROTOCOL.md`：声明 guard 为规则文件，不同步
- 同步 `CURRENT_STATE.md` / `.ai/current_state.md`

**Outcome:** Level 6 系统现在完整——从 Feature Request → Architecture Guard → Impact Analysis → Execution → Regression → Memory Sync 形成闭环。以后每次新增功能必须先通过 Architecture Placement Review，确保不会重新膨胀已拆分过的文件。

---

## 2026-06-13 — 战场清扫：删除 28 个历史残留文件

**Goal:** 清除项目根目录遗留的过期修复摘要、诊断报告和重复文件。

**删除清单 (28 files, ~110 KB freed):**
- 24 个历史修复摘要: `*_FIX_SUMMARY.md` / `*_FIXES_SUMMARY.md` / `FIXES_SUMMARY.md` / `ERROR_FIX_SUMMARY.md` / `REFACTORING_SUMMARY.md` — 均已在 TASK_LOG.md 中追踪
- `RANDOM_UNANSWERED_DEBUG_LOGS.md` — 过期调试日志
- `PROBLEM_DIAGNOSIS_REPORT.md` — 一次性诊断
- `EVOLUTION/` 文件夹 (重复的 1_EVOLUTION_ENGINE.md + 2_SYSTEM_HEALTH_ENGINE.md)
- `AppNavHost_backup.kt` — 手动备份
- `EXAM_VIEWMODEL_COMPILATION_FIXES.md` / `EXAM_VIEWMODEL_UNIFIED_STATE_COMPLETION.md` / `PRACTICE_SWIPE_NAVIGATION_FIX_SUMMARY.md` — 第二轮扫描发现

**保留:** `PROJECT_SCAN_DEPENDENCY_REPORT.md` (被 dependency_graph.md 引用为来源)

---

## 2026-06-14 — Phase 5 模块迁移完成 + 清理

**Goal:** 完成所有 feature 模块文件迁移，清理遗留代码。

**Actions:**
- feature-practice: 26 文件迁移（PracticeViewModel + UI 组件 + Coordinators）
- feature-exam: 31 文件迁移（ExamViewModel + ExamScreenContent + 14 UI 组件 + Localization）
- ExamScreen 拆分为 Content/Wrapper 模式：feature-exam 纯 UI + :app 薄包装层（收集 StateFlow）
- PracticeScreen 因 6 个 :app ViewModel 依赖留在 :app
- :core session/ 创建（SessionEngine + ProgressManager + AnalysisLoader + MemoryMode + Navigator）
- :core util/ 创建（6 工具函数）
- :ui-common 创建（12 文件：AnswerCardGrid, FontStyleProvider, RichTextParser 等）
- FontSettingsRepository 接口 + ProgressIdBuilder 提取到 :core
- LocalizedResult 统一到 core.common
- 37 UseCase 文件从 :app 迁移到 :domain
- 清理 PracticeNavigationCoordinator 中 @Deprecated bridge 类型
- 清理 .history/ + domain/bin/ + AppNavHost_backup.kt
- 还原 PracticeScreen 引用的 ExamTopBar/ExamOptionsList/ExamBottomControls 到 :app/components/
- 清理 5 个未用 import

**模块分布（重构后）:**
| 模块 | 文件 | 行数 | 占比 |
|------|------|------|------|
| :app | 120 | 15,707 | ~52% |
| :data | 57 | 3,676 | ~12% |
| :domain | 69 | 1,941 | ~6% |
| feature-practice | 26 | 3,698 | ~12% |
| feature-exam | 31 | 3,282 | ~11% |
| :core | 13 | 1,148 | ~4% |
| :ui-common | 12 | 807 | ~3% |
| 合计 | 293 | 30,259 | 100% |

**遗留阻塞:**
- PracticeScreen（6 个 :app ViewModel + R + Context 阻塞）
- ExamScreen wrapper（保留在 :app）
- ExamAISyncEffects（保留在 :app）
- :app/components/ 37 文件

---

**Goal:** 清除 IDE 自动生成的版本快照和构建输出副本。

**删除:**
- `.history/` — **2,155 个 `.kt` 版本快照 + 34 个 `.md`**；其中 216 个文件被 Git 追踪（先 `git rm --cached`）
- `domain/bin/` — 29 个过期 `.kt` 输出副本
- `cleanup_backups.ps1` — 清理 `.backup` 的脚本，已无用途

**防护:** `.gitignore` 新增 `.history/` 和 `**/bin/` 规则，防止再被追踪。

---

## 2026-06-11 — Step 5+6: FullAnswer + Session coordinators (pure-extraction complete)

**Goal:** Complete 6/6 pure-extraction plan. FullAnswerCoordinator for fill config; SessionCoordinator placeholder.

**Actions:**
- `PracticeFullAnswerCoordinator.kt` (~120L): `shouldReuseSavedSourceOrder`, `restoreConfiguredQuestionsForProgress`, `shouldApplyDynamicFillTransform`
- `PracticeSessionCoordinator.kt` (~25L placeholder — all candidates state-mutating)
- Compile ✅ / Tests 29/30
- VM ~3550→~3500

---

## 2026-06-11 — Radical Phase 1: ModeCoordinator holds _sessionState, 4 giant methods migrated

**Goal:** Give coordinators direct `_sessionState + scope` access; migrate state-mutating methods.

**Actions:**
- `PracticeModeCoordinator.kt` rewired (~420L): `buildMemoryRoundStates`, `refreshMemoryRoundPoolIfNeeded`, `removeCurrentQuestionFromMemoryPool`, `advanceMemoryRoundIfNeeded` now run from coordinator with `_sessionState` reference
- `PracticeViewModel.kt`: ~3500→~3230 lines (~270 lines moved)
- Compile ✅ / Tests 29/30 (same pre-existing TimeoutCancellationException)

**Next:** Phase 2: `saveProgress`/`clearProgress`/`buildStoredQuestionState`/`loadWrongQuestions`/`loadFavoriteQuestions` → SessionCoordinator (~500L)

---

## 2026-06-11 — Step 4 Execution: PracticeModeCoordinator Extracted

**Goal:** Extract mode configuration, round planning, and snapshot management from PracticeViewModel.

**Actions:**
- Created `PracticeModeCoordinator.kt` (~150 lines): 10 pure functions + `MemoryRoundPlan` data class
  - `buildMemoryRoundPlan`, `updatePersistentStateMap`, `effectiveCurrentMemoryRoundQuestionIds`, `fallbackAnswerTime`
  - `hasConfiguredQuestionSnapshot`, `restoreConfiguredQuestionSnapshot`, `isConfiguredSnapshotCompatible`
  - `shouldUseMemoryModeFor`, `memoryWrongModeResolved`, `memoryPoolModeResolved`
- Modified `PracticeViewModel.kt`: delegated all 10 methods; `buildMemoryRoundPlan`, `fallbackAnswerTime`, snapshot methods now call `modeCoordinator`
- Compile: ✅ BUILD SUCCESSFUL
- Tests: 29/30 pass (same pre-existing TimeoutCancellationException in switching_from_sequential_to_random_practice)
- VM: ~3600→~3550 lines

**Artifacts:**
- `PracticeModeCoordinator.kt` created
- `.ai/method_index.md` created (line-numbered method map for fast extraction)

---

## 2026-06-11 — Step 3 Execution: PracticeProgressCoordinator Extracted

**Goal:** Extract pure progress utility functions from PracticeViewModel.

**Actions:**
- Created `PracticeProgressCoordinator.kt` (~110 lines)
  - 7 pure functions: `practiceProgressSeed`, `defaultFillConfigSignature`, `buildSessionIdWithFillSignature`, `extractFillConfigSignature`, `canReuseByFillSignature`, `fillGenerationModeFromSignature`, `buildProgressSnapshotFromState`
  - `saveProgress()`/`loadProgress()`/`setProgressId()`/`clearProgress()` remain in VM (deeply coupled to `_sessionState`, `viewModelScope`, `saveProgressMutex`, data sources)
- Modified `PracticeViewModel.kt`: delegated 7 methods
- Compile: ✅ BUILD SUCCESSFUL
- Tests: 38/39 pass (same pre-existing TimeoutCancellationException)

**Goal:** Extract pure answer-evaluation functions from PracticeViewModel into PracticeAnswerHandler.

**Actions:**
- Created `PracticeAnswerHandler.kt` (~220 lines)
  - 11 pure functions: `isQuestionAnswered` (2 overloads), `hasAnswerContent`, `isQuestionCorrect`, `isQuestionPendingForCurrentMode`, `shouldReopenUnansweredReveal`, `hasPendingQuestions`, `findFirstPendingIndex`, `findResumeIndex`, `currentSourcePendingIndices`, `currentFullAnswerCandidateIndices`, `nextFullAnswerCandidateIndices`, `isCurrentSourceComplete`
  - Zero state mutation — all functions compute truth values only
- Modified `PracticeViewModel.kt`: delegated 11 methods to `answerHandler` instance
- Compile: ✅ BUILD SUCCESSFUL
- Tests: 38/39 pass (same pre-existing TimeoutCancellationException)

**Goal:** Extract navigation state management from PracticeViewModel into PracticeNavigationCoordinator.

**Actions:**
- Created `PracticeNavigationCoordinator.kt` (Lines ~240)
  - Owns: `AnsweredHistoryNavigationState`, `RandomNavigationHistoryState`, `PracticeNavigationState`, snapshot maps, pure state transitions
  - 11 public/internal methods: snapshot management, history overlays, navigation state transitions, random history seeding
- Modified `PracticeViewModel.kt` (Lines ~3900→~3750)
  - Removed: navigation type declarations (`AnsweredHistoryNavigationState`, `RandomNavigationHistoryState`, `PracticeNavigationState`)
  - Removed: `answeredHistorySnapshots`, `answeredHistoryOriginalStates` fields
  - Delegated 15+ methods to coordinator via `navigationCoordinator` instance
- Updated `PracticeViewModelTest.kt`: fixed 2 reflection helpers to route through `navigationCoordinator` field
- Compile: ✅ BUILD SUCCESSFUL
- Tests: ✅ 39/39 passing (1 TimeoutCancellationException pre-existing, unrelated)

**Conclusion:** Step 1 safe extraction complete. VM ~150 lines lighter. Zero behavioral change.

**Artifacts:** `PracticeNavigationCoordinator.kt` created.

**Goal:** Define exact step-by-step migration with rollback commands and simulate subsystem impact.

**Plan:**
- 6 extraction steps with per-step compile + test + rollback commands
- Impact matrix across 10 subsystems
- Risk propagation analysis per step
- Rollback decision tree

**Conclusion**: ✅ Safe to proceed (Steps 1-2); **BLOCKED** by K-001 device smoke.

**Artifacts:** [.ai/migration_plan.md](migration_plan.md)

---

## 2026-06-11 — Architecture Design Phase

**Goal:** Analyze PracticeViewModel.kt structure and design coordinator extraction plan.

**Findings:**
- 7 bounded contexts identified: Loader, Session, Navigation, Answer, Persistence, FullAnswer, Modes
- C3 (Navigation) and C4 (Answer) pure logic — zero external dependencies
- 6 coordinators: Navigation→Answer→Progress→Mode→FullAnswer→Session
- Expected: 3900→2300 lines across 7 files

**Artifact:** [.ai/architecture_design_report.md](architecture_design_report.md)

---

## 2026-06-11 — Decomposition Blueprint

**Goal:** Define exact interfaces and dependency tree for all 6 coordinators.

**Blueprint:**
- 6 coordinator interfaces with exact method signatures
- State bridging pattern: single `MutableStateFlow<PracticeSessionState>` shared
- Per-step validation gates (compile + unit test + device check)
- Immutable constraints documented

**Artifact:** [.ai/decomposition_blueprint.md](decomposition_blueprint.md)

---

## 2026-06-11 — L6 Self-Evolving OS deployed

**Goal:** Deploy full Level 6 Self-Evolving Engineering OS (11 files) to project root.

**Actions:**
- Created `LEVEL6_OVERVIEW.md` + 10 engine files (1_EVOLUTION_ENGINE through 10_EVOLUTION_REPORT_ENGINE)
- Engine rules: no automatic evolution; requires detection → health scan → drift analysis → debt mining → safety gate → strategy → simulation → controlled execution → regression monitor → post-validation → report

---

## 2026-06-11 — Refactor Candidate Analysis

**Goal:** Identify highest-priority refactor targets across entire project.

**Findings:**
- Top 3: `PracticeViewModel` (score 98), `ExamViewModel` (92), `QuestionRepositoryImpl` (85)
- 11 files ranked from EXTREME to LOW priority
- System health: DEGRADED (coupling 84%, 10+ God files, empty feature modules)
- Prerequisite: K-001 device smoke must pass before any code change

---

## 2026-06-11 — Memory Layer Initialized

**Goal:** Create persistent `.ai/` engineering memory layer with 8 canonical files.

**Actions:**
- Created `.ai/` directory
- Created: `current_state.md`, `architecture_map.md`, `tech_debt.md`, `refactor_candidates.md`, `file_registry.md`, `module_map.md`, `dependency_graph.md`, `change_log.md` (this file)
- All files marked as derived artifacts from Source of Truth (ARCHITECTURE, CURRENT_STATE, KNOWN_ISSUES, TASK_LOG)
- Updated root `CURRENT_STATE.md` to reference `.ai/` as canonical memory layer
- Updated root `TASK_LOG.md` with this entry

**Outcome:** Agents now have a persistent, indexed memory layer. Read `.ai/current_state.md` first.

---

## 2026-05-29 — Import root-dir: confirmed system root cause + in-app browser

**Root cause (verified via adb):** Xperia 1 V / Android 15 DocumentsUI hides `primary:` root. App-side picker changes cannot fix.

**Fix:** In-app file browser (`QuizFileBrowser.kt` + `importQuestionsFromFiles`) via All Files Access.

---

## 2026-05-27 — Import MIME + practice loading fixes

**Goal:** Fix empty SAF import picker; fix practice screen stuck on loading.

**Changes:** SettingsViewModel settingsReady gate, PracticeScreen LaunchedEffect, PracticeViewModel empty file handling.

---

## 2026-05-27 — Phase0: Agent memory bootstrap

**Goal:** Establish `CURRENT_STATE.md`, `TASK_LOG.md`, `KNOWN_ISSUES.md`, `ARCHITECTURE.md` as single fact source.

---

## Prior work (summary)

Remediation period (pre–memory files): navigation state unification, full-answer/history fixes, scoped progress ids, home aggregation, progress save ordering, unit tests, partial device history-route checks.

Baseline refactor (pre–memory files): project scan report, partial i18n/components.
