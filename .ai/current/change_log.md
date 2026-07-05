<!--
  Derived from: TASK_LOG.md
  Last synced: 2026-06-19 23:55 UTC+8
  Do not edit directly — append to TASK_LOG.md, then re-sync.
-->

# Change Log

## 2026-07-05 — Engineering OS：`.ai` 升级为 PowerAI Engineering OS

- 新增 `PROJECT_CONSTITUTION.md`、`README.md`（阅读顺序入口）
- 分层：`architecture/`、`workflows/`、`current/`、`rules/`；`ADR/` 保留
- 权威 Guard：`.ai/workflows/architecture_guard.md`（六步：Scan → Dependency → Reuse → ADR → Impact → Plan）
- 活文档迁入 `current/`（`current_state`、`loc_audit`、`architecture_map`、`module_map` 等）
- 根目录旧路径保留 **MOVED stub**，避免断链
- Cursor Rules：`.cursor/rules/powerai-engineering-os.mdc` + 更新 `architecture-guard.mdc`

**验证：** 文档层无编译依赖；Agent 下次任务从 `README.md` 启动。

---

## 2026-07-05 — P79：归档计划收尾 — Room 瘦身 + domain 命名 + 写回单测

- `domain/usecase/QuestionAnalysisUseCases.kt`（原误名 `QuestionAnalysisRepository.kt`）
- `:app` 移除直接 Room 依赖
- `AppNavAiWritebackPipelineTest`；`config/detekt/detekt.yml` 标注根配置为准

**验证：** `compileDebugKotlin` + 单测 + `detekt` ✅

---

## 2026-07-05 — P78：可选收尾 — AI 写回 Command + POI 瘦身 + detekt

**P78a — AI 叠层写回 Command 收编：**
- `AppNavAiWritebackPipeline`；`AppNavAiRoutes` 不再直调 `bindings.update*`
- `SessionCommand.AppendNote` + bindings `appendNote` + CommandHandler 接线

**P78b — `:app` 冗余 POI/StAX 移除**（`:data` / `:feature-settings` 保留）

**P78c — detekt 收紧：** UnusedImports/Member/Property、WildcardImport、EmptyCatchBlock、IgnoredReturnValue、VarCouldBeVal

**验证：** `compileDebugKotlin` + `detekt` + `ArchitectureTest` ✅

---

## 2026-07-05 — P77：Phase 24 — Settings IO 全量下沉 `:feature-settings`

- 迁入：`SettingsIoUriPipeline`、`ImportCoordinator`、`JsonExportCoordinator`、`ExcelExportCoordinator`
- `SettingsIoBindingModule` → `:feature-settings`；删 `:app` 副本
- `:app` `presentation/` **25 → 21** `.kt`

**验证：** `compileDebugKotlin` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P76：Strategy 长尾 #3 — Prev 门禁 + ExamProgressIdPipeline

**P76a — NavigationSequentialPrev Strategy 门禁：**
- `SessionPracticePostAnswerNavigationPipeline.shouldTryMultiRoundPostAnswerPrev`
- `NavigationSequentialPrev` 经 `effectiveOrchestration()` + `resolveBackwardAdvanceRoute`

**P76b — ExamProgressIdPipeline：**
- `exam_` 前缀 `ensurePrefix`；`ExamLoadDelegate.loadReviewSession` / `ExamReviewSessionCoordinator` 收编

**验证：** `compileDebugKotlin` + Pipeline 单测 + `ArchitectureTest` ✅

---

## 2026-07-05 — P75：Strategy 长尾 #2 — NavigationController 编排 + Exam/Practice 对称

**P75a — NavigationController 编排迁入 Strategy：**
- `NavigationEnvironment.effectiveOrchestration()` + `prepareStateForUnansweredIconNav` 尊重 `exitAnsweredHistoryBeforeIconNav`
- `NavigationSequentialNext` 全面门禁：`shouldResumePendingAfterHistoryExit` / `shouldTryFullAnswerSourceStay` / `shouldTryReopenOnPostAnswerAdvance` / `shouldTryNextSourceEntry` / `shouldTryAdjacentDerived` / `resolveFinalAdvanceRoute`

**P75b — Exam/Practice Progress Lifecycle 对称：**
- `ExamReviewSessionLoadPipeline` + `ExamReviewSourceQuestionsPipeline`（`ExamLoadDelegate.loadReviewSession` 收编）
- `ExamProgressPersistencePipeline.shouldRestoreAnswersFromMap`；`ExamSessionProgressCoordinator` 经 Strategy `persistenceConfig` 判定恢复

**验证：** `compileDebugKotlin` + Pipeline 单测 + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P74：Strategy 长尾 #1 — Progress/Navigation 管道化

**P74a — Practice Progress Lifecycle 管道化：**
- `PracticeProgressIdPipeline`（`practice_` 前缀）
- `PracticeReviewSessionLoadPipeline`（复盘 load 判定）
- `PracticeProgressLifecycleCoordinator` 收编上述管道

**P74b — Navigation Strategy 对称：**
- `PracticeNavigationCoordinator`：`bindNavigationOrchestration { strategyCoordinator... }`，移除可变 orchestration 字段
- `ExamNavigationCoordinator`：删除未使用的 `navigationOrchestration` / `applyNavigationOrchestration`（门禁已由 `ExamSessionNavigationDelegate` + Strategy 承担）

**验证：** `compileDebugKotlin` + 新增 Pipeline 单测 + `ArchitectureTest` ✅

---

## 2026-07-05 — P73：Phase 22 — Settings IO 收尾 + 去除 Host 间接层

**P22a — `SettingsImportFilePipeline` 并入 `SettingsIoUriPipeline`：**
- Uri→临时文件 / 文件名解析 / 持久化读权限 统一入 `SettingsIoUriPipeline`
- `ImportCoordinator` 仅注入 `SettingsIoUriPipeline`

**P22b — 去除 `SettingsLocalFileImportHost` 间接层：**
- `SettingsScreen` 直接调用 `QuizFileBrowserDialog` / `hasStorageAccess`
- 删除 `SettingsLocalFileImportHost` / `Impl` / `SettingsHostEntryPoint` / Hilt host 绑定

**验证：** `compileDebugKotlin` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P72：Phase 21 — 统一 SettingsIoUriPipeline

**P21 — 合并 `*UriPipeline`：**
- `SettingsJsonExportUriPipeline` + `SettingsExcelWorkbookUriPipeline` + `SettingsImportUriPipeline` → `SettingsIoUriPipeline`
- `ImportCoordinator` / `JsonExportCoordinator` / `ExcelExportCoordinator` 注入统一管道
- 保留 `SettingsImportFilePipeline`（Uri→临时文件工具，供 Import 与 IoUri 共用）

**验证：** `compileDebugKotlin` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P71：Phase 20 — 导入管道化 + QuizFileBrowser 下沉

**P20a — 导入管道化：**
- `SettingsImportResultPipeline`（批量结果聚合）
- `SettingsImportUriPipeline`（单文件/单 Uri 导入）；`ImportCoordinator` 瘦身

**P20b — 文件浏览下沉：**
- `QuizFileBrowser` → `:feature-settings`；`SettingsLocalFileImportHostImpl` 同步迁入
- 删除死代码 `OpenMultipleQuizDocumentsContract`（零引用）
- `file_browser_*` 字符串迁入 `feature-settings` res

**验证：** `compileDebugKotlin` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P70：Excel 导出管道化

**P70 — Excel 导出拆分：**
- `ExcelSheetBuilder` → `:feature-settings`（表结构构建，无 POI）
- 新增 `SettingsExcelExportPipeline` / `HistoryExcelExportPipeline`（纯编排）
- 新增 `SettingsExcelWorkbookUriPipeline`（`:app` POI 写 Uri）
- `ExcelExportCoordinator` 瘦身；历史导出移除 `HistoryRepositoryImpl` 强转

**验证：** `compileDebugKotlin` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P69：Phase 18 — 遗留导航死代码清理 + Settings JSON 管道化

**P18a — 删除 `AppNavRoutes` 遗留 VM 路由：**
- 移除 `registerAnalysisRoutes` / `registerScreenRoute` / `createAnalysisCallbacks` / `AnalysisCallbacks`（全仓库无调用）
- 保留 `navToResult`（`AppNavSessionRoutes` 仍用）

**P18b — Settings JSON 导出管道化：**
- 新增 `SettingsJsonExportUriPipeline`；`JsonExportCoordinator` 三处写 URI 复用
- 删除 `:app` 重复 `presentation/util/Localization.kt`（feature-practice/exam 各有副本）

**验证：** `compileDebugKotlin` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P68：Phase 17 — AI 叠层回调导航 + Settings IO 收尾

**P17a — AI 叠层 `NavController` → 回调：**
- `feature-ai` 全部 AI/Note/Explanation Screen：`onBack` / `onOpenAsk` / `onOpenDeepSeekAsk`
- `AppNavAiRoutes` 瘦身 + `AiOverlayRouteHelpers`；`ExplanationRoute` 薄包装
- `AppNavRoutes.registerAnalysisRoutes` 同步更新（遗留 VM 路径）
- `feature-ai` 移除 `navigation-compose` 依赖

**P17b — Settings IO 接口化收尾：**
- 删除 `ImportCoordinator` 重复 `ImportResult`（统一 `feature-settings`）
- 抽取 `SettingsImportFilePipeline`（URI/临时文件工具）

**验证：** `compileDebugKotlin` + `ArchitectureTest` ✅

---

## 2026-07-05 — P67：Phase 16 — Practice/Exam Screen 壳下沉 + 删除 shared 垫片

**P16a — `PracticeScreen` → `:feature-practice`：**
- 从 `:app` 迁入；`parsing` → `feature.practice.R`
- 音效 raw 由薄路由注入 `correctSoundResId` / `wrongSoundResId`（`PracticePracticeRoute` 传 `app R.raw`）

**P16b — `ExamScreen` → `:feature-exam`：**
- 收藏解耦：`isQuestionFavorite` / `onToggleQuestionFavorite` 回调
- `:app` 新增 `rememberExamFavoriteBindings()`（`FavoriteViewModel` 接线，不引入 feature-exam→feature-practice 依赖）
- `feature-exam` 补 `feature-settings` 依赖

**P16c — 清理：**
- 删除 `:app` `PracticeScreen.kt` / `ExamScreen.kt` / `SessionCommandDispatch.kt` 垫片

**验证：** `compileDebugKotlin` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P66：Phase 15 尾 — FavoriteScreen 下沉 `:feature-practice`

- `FavoriteScreen` → `feature-practice/.../favorite/`；`R` → `feature.practice.R`
- 回调导航：`onBack` / `onOpenFile` / `onStartFavoriteQuiz`（移除 `NavController`）
- 薄路由 `:app`：`FavoriteRoute.kt`；`AppNavHost` 接线
- `strings.xml` 补 `favorites` / `no_favorites` / `practice_file_favorites`

**验证：** `compileDebugKotlin` ✅

---

## 2026-07-05 — P65：Phase 15 — question_detail→Browse Session + Result/History/WrongBook 下沉

**P15a — `question_detail` 改接 Browse Session：**
- `onViewQuestionDetail` → `question/{quizId}?targetQuestionId=0`（从头 Browse）
- `AppNavSessionRoutes`：`targetQuestionId >= 0` 走 `BrowsePracticeRoute`
- 删除 legacy `QuestionScreen.kt`、`RememberAppPracticeSessionBindings.kt`、`question_detail` 路由

**P15b — Result / History / WrongBook → `:feature-practice`：**
- `ResultScreen` / `ResultHistorySheet` / `ResultViewModel` / `ResultDisplayStats` / 组件（`ResultScoreCards`、`ResultStatBlock`、`ResultAccuracyChart`）
- `HistoryScreen` / `HistoryViewModel`；`WrongBookScreen`（回调 `onBack`/`onOpenFile`/`onStartWrongBookQuiz`，无 `NavController`）
- `library/*`（`ScopedQuestionLibraryScreen` 等）R → `feature.practice.R`
- 薄路由 `:app`：`ResultRoute`、`HistoryRoute`、`WrongBookRoute`

**单测：** `BrowseSessionRoutePipelineTest` 补 `targetQuestionId=0` 用例

**验证：** `compileDebugKotlin` + session route/arch 单测 + `PracticeSessionStrategyCoordinatorTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P64：Phase 14 — DrawerQuestionEdit 接线 + feature-practice 单测基建

**P14a — `DrawerQuestionEditHost` 导航接线：**
- `QuestionEditSessionRoutePipeline`（`:core`）+ 路由 `question_edit/{quizId}/{questionId}`
- 薄路由 `QuestionEditPracticeRoute`（`:app`）；`AppNavSessionRoutes` 注册
- 题库抽屉题目行 **长按** → `onEditQuestion`；点击仍走 Browse
- `HomeRoute` / `HomeScreen` / `HomeScreenDrawerHost` 传递 `onEditQuestion`

**P14b — `feature-practice` 单测基建：**
- `testImplementation` junit + kotlinx-coroutines-test
- 禁用 `kaptDebugUnitTestKotlin`（纯 JUnit 单测无需 Hilt kapt）

**验证：** `compileDebugKotlin` + `QuestionEditSessionRoutePipelineTest` + `DrawerQuestionEditPipelineTest` + `PracticeSessionStrategyCoordinatorTest` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P63：Phase 13 — FontSettingsRepositoryImpl + DrawerQuestionEdit 下沉

**P13a — `FontSettingsRepositoryImpl` → `:data`：**
- 物理迁移 `app/.../FontSettingsRepositoryImpl.kt` → `data/.../datastore/`
- `DataBindModule` 新增 `bindFontSettingsRepository`
- 删除空壳 `AppModule.kt`（原仅 FontSettings 绑定）

**P13b — `DrawerQuestionEditHost` Strategy 清理：**
- 迁入 `:feature-practice/questionbank/`；抽取 `DrawerQuestionEditPipeline`（纯逻辑）
- `save_success` → `feature.practice.R`；单测 `DrawerQuestionEditPipelineTest`（`:app` test）

**Settings IO（保持）：** coordinators 留 `:app` Gateway ✅

**验证：** `compileDebugKotlin` + `DrawerQuestionEditPipelineTest` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P62：P12c/d — Home→feature-practice + QuestionBank 下沉

**P12c — Home Screen 包迁入 `:feature-practice`：**
- 物理迁移 `home/**`（30 文件）+ `FileFolderViewModel` / `DragDropViewModel`
- `HomeScreen` 改注入式 VM；`:app` 新增薄 `HomeRoute`（Hilt 绑定 + 导航回调）
- `AppNavHost` 改调用 `HomeRoute`
- Home 串迁入 `feature-practice/res`；`FontSettingsDataStore` + `PreferenceDelegate` → `:data`

**P12d — QuestionBank drawer 下沉 `:feature-practice`：**
- 迁入 `QuestionBankDrawer` / Header / Rows / Search / Expansion / Item / `QuestionBankDrawerViewModel`
- `DrawerQuestionEditHost` 留 `:app`（Session bindings）
- `QuestionBankDrawer` 改必填 `viewModel` 参数（无默认 `hiltViewModel`）

**Settings IO（保持）：** `ImportCoordinator` / `JsonExportCoordinator` / `ExcelExportCoordinator` 留 `:app`，经 `SettingsIoBindingModule` Gateway 绑定 ✅

**验证：** `compileDebugKotlin` + `HomeViewModelTest` + `QuestionBankDrawerExpansionTest` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P61：Phase 12 入口 — OptimizedFileCard 下沉 + Progress save/apply 管道

**`:ui-common` 组件下沉：**
- `OptimizedFileCard` / `DraggingFileCard` → `ui-common/component/OptimizedFileCard.kt`
- 统计标签串迁入 `ui-common/res`（`uicommon_label_*`）；从 `app/strings.xml` 去重
- `HomeFileListGrid` / `HomeFileListColumn` / `DraggingFileOverlay` 改 `uicommon` import

**Progress Coordinator 续管道化（328 LOC）：**
- `PracticeProgressApplyLoadedPipeline` — Ready 结果 → catalog/map/session 补丁 + 日志上下文
- `PracticeProgressSaveRequestPipeline` — save 请求载荷组装

**验证：** `compileDebugKotlin` + Pipeline 单测 + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P60：P11f HomeFileListColumn 外置

**P11f — 列表分支外置：**
- `HomeFileListColumn.kt` — LazyColumn + 拖拽滚动 + `homeFileListColumnFiles` / `homeFileListColumnFolders`
- `HomeFileList.kt` 降至 ~130 LOC（grid/column 分发 + 拖拽包装）

**验证：** `compileDebugKotlin` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P59：P11d HomeFileListGrid + Progress 加载管道化

**P11d — Home 网格外置：**
- `HomeFileListGrid.kt` — 2 列网格 + 拖拽边缘滚动
- `HomeFileListDragScroll.kt` — 自动滚动常量/判定（列表共用）
- `HomeFileList.kt` 降至 ~290 LOC

**PracticeProgressLifecycleCoordinator 管道化（331 LOC）：**
- `PracticeProgressLoadQuestionsPipeline` — `loadQuestionsForCurrentSource` 轮次/恢复计划
- `PracticeReviewSourceQuestionsPipeline` — 错题/收藏题库过滤
- `PracticeProgressLocalResetPipeline` — `clearProgress` 本地状态重置
- Coordinator 仅执行 IO / `sessionState` 写回

**验证：** `compileDebugKotlin` + 新 Pipeline/DragScroll 单测 + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P58：Settings UI 下沉 + Home 预防性拆分 + 文档同步

**删 app 重复 settings coordinators（6 个）：**
- `FontSettingsCoordinator`、`FillQuestionFilterCoordinator`、`SettingsActionPipeline`、`SupplementaryDataBatchLoader`、`SettingsImportSnackbarPipeline`、`SettingsExportRequestPipeline` — 仅保留 `:feature-settings` 副本

**SettingsScreen / FillSettings + ui/* → `:feature-settings`：**
- 迁入 `SettingsScreen.kt`、`FillSettingsScreen.kt`、`SettingsStoragePermissionDialog.kt` 及 `ui/*`（22 文件）
- `feature-settings` 启用 Compose + `ui-common`；`SettingsTopBar` 改用 `settings_nav_back`
- `SettingsLocalFileImportHost` 接口（feature-settings）+ `SettingsLocalFileImportHostImpl`（app，包装 `QuizFileBrowserDialog` / storage）
- `SettingsHostEntryPoint` + `SettingsIoBindingModule` 绑定 host；`AppNavHost` EntryPoint 注入
- **留 `:app`：** `ImportCoordinator` / `JsonExportCoordinator` / `ExcelExportCoordinator` / `ExcelSheetBuilder`（`R`/`Context`/`Uri`）

**Home 预防性拆分：**
- `HomeScreenDialogs` — 文件/文件夹增删改 AlertDialog 集合
- `HomeImportLoadingOverlay` — 导入进度遮罩
- `HomeFileListSwipeDeleteBackground` — 侧滑删除背景复用
- `HomeActionOverlays` 134 LOC、`HomeFileList` 363 LOC（均 ≤500 ✅）

**验证：** `compileDebugKotlin`（app + feature-settings）+ `ArchitectureTest` + `check-loc-over-500.ps1` ✅

**`:app` 占比（2026-07-05 复测）：** 99 文件 / 10,115 行 → **~12% 文件 / ~18% 行**（全模块 822 文件 / 57,549 行；目标 ≤40% ✅）

> **Agent 约定：** 每完成一个 Phase，同步更新 `change_log.md`、`current_state.md`、`loc_audit.md`、`refactoring_plan.md`、`tech_debt.md`（有触达时）。

---

## 2026-07-05 — P57：Phase 11 入口 — Home drawer 外置 / settings res / Progress 管道化

**HomeScreen drawer/overlays 外置（293 LOC ✅）：**
- `HomeScreenState` — `rememberHomeLibraryDisplayState` / `rememberHomeNavPrefsState`
- `HomeScreenShell` — `HomeScreenDrawerHost` / `HomeScreenScaffoldContent` / `pruneHomeDragBounds`
- `HomeDrawerContent` 注入共享 `QuestionBankDrawerViewModel`；Browse 返回恢复搜索态

**`:feature-settings` res 下沉（续）：**
- 全量 settings UI 串迁入 `feature-settings/res`（appearance / answer / memory / fill / import-export sheet / Excel 表头）
- `app/.../settings/**/*.kt` 统一 `feature.settings.R`；`SettingsTopBar` 的 `back` 仍用 `AppR`
- 从 `app/strings.xml` 移除 146+ 条重复 key（保留 `back`、`settings_font`、`fill_blank*`、`file_browser_*` 等跨模块引用）
- 修复误还原的 gateway `override` 绑定；删除重复 `app/.../SettingsViewModel.kt`

**PracticeProgressLifecycleCoordinator Strategy 管道化（413 LOC）：**
- `PracticeProgressLoadRoundContextPipeline` + `PracticeProgressLoadOrderPipeline` — load 轮次复用/排序
- `PracticeProgressSavePayloadPipeline` — save extras / map 合并
- 删除死代码 `orderedForPractice`

**验证：** `compileDebugKotlin` + arch/session/新 Pipeline 单测 + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P56：Phase 10 尾 — Settings VM / Note / Strategy / Screen 拆分

**P10j — SettingsViewModel → `:feature-settings`：**
- `SettingsImportGateway` / `SettingsJsonExportGateway` / `SettingsExcelExportGateway` 接口化
- `ImportCoordinator` / `JsonExportCoordinator` / `ExcelExportCoordinator` 留 `:app` 实现（`R`/`Context`）
- `SettingsIoBindingModule` Hilt 绑定；`ImportResult` 迁至 feature-settings

**P10k — NoteScreen → `:feature-ai`：**
- 字体解耦 `rememberAiArtifactTypography(DEEPSEEK)`；移除 `SettingsViewModel` / `FontSettingsDataStore`
- 路由层注入 `currentNote`（bindings/VM `noteList`），无 practice/exam 直依赖

**Strategy — PracticeProgressLifecycleCoordinator：**
- 默认持久化改 `SessionStrategyFactory.persistence(Practice)`；`activePersistenceConfig()` 委托 Strategy

**Home/Result 组件化：**
- `ResultScreen` → `ResultStatBlock` / `ResultScoreCards` / `ResultAccuracyChart`（143 LOC）
- `HomeScreen` → `HomeScreenLibrarySection`（622 LOC，待续拆 drawer/overlays）

**验证：** `compileDebugKotlin` + arch/session 单测 + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P55：Phase 10h/i + Home/Result VM 模块化

**P10h — AI Screens → `:feature-ai`：**
- `feature-ai/res/values/strings.xml` — AI 屏独立字符串
- `AiFontBindings` + `FontSettingsEntryPoint` — 解耦 `SettingsViewModel` / `FontSettingsDataStore`
- `ActionModeTextToolbar` → `ui-common`（`askMenuLabel` 参数化）
- 迁入 7 个 AI Screen + `ExplanationScreen`；导航移除 `settingsViewModel` 透传

**P10i — Settings coordinators 外置：**
- 新建 `:feature-settings` — `FontSettingsCoordinator`、`FillQuestionFilterCoordinator`、`SettingsActionPipeline`、`SettingsExportRequestPipeline`、`SupplementaryDataBatchLoader`、`SettingsImportSnackbarPipeline`
- `SettingsViewModel` + `Import/Json/Excel` coordinators 暂留 `:app`（`R` / `Context` / `Uri`）

**Strategy — Home/Result：**
- `HomeViewModel` → `:feature-practice`
- `ResultViewModel` + `ResultDisplayStats` → `:feature-practice`

**验证：** `compileDebugKotlin` + `ArchitectureTest` + `PracticeSessionEngineTest` + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P54：Phase 10e/f — `:feature-ai` + pipelines + DrawerQuestionEdit

**P10e — AI / Sync 下沉：**
- 新建 `:feature-ai` — AI ViewModels、`SessionAnalysisLoader/SyncPipeline`、`rememberSessionAnalysisLoader`
- `data/network/**` → `:data`（`BuildConfig` API keys + `NetworkModule`）
- `PracticeAISyncEffects` → `:feature-practice`；`ExamAISyncEffects` → `:feature-exam`
- AI Screens 暂留 `:app`（`R` / `SettingsViewModel` / `FontSettingsDataStore` 阻塞）

**P10f — ui-common 下沉：**
- `FileCardTone/Color/Stat`、`ResultStat/HistoryLine`、`QuestionBankDrawerWidth` pipelines

**Strategy — DrawerQuestionEdit：**
- `DrawerQuestionEditHost` → `rememberQuestionEditSessionBindings` + `SessionCommand`（移除 `PracticeViewModel` 直依赖）

**验证：** `compileDebugKotlin` + arch/session 单测 + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P53：Phase 10a — VM / ui-common 下沉

**范围：** WrongBook/Favorite VM 迁入 `:feature-practice`；共享 util/component 迁入 `:ui-common`；Exam 编辑 Command 收尾。

**迁移：**
- `WrongBookViewModel` / `FavoriteViewModel` → `feature-practice/.../wrongbook|favorite/`
- `ImportQuestionsUseCase` → `:domain`（解除 VM 对 `:app` 依赖）
- `SoundEffects` → `ui-common/util`（资源 ID 由调用方注入）
- `SwipeRevealActionBox` → `ui-common/component`

**Command：**
- Exam `QuestionEditDialog` → `SaveEditedQuestionFields` + `ClearEditableQuestion`
- `ExamScreenSaveSuccessEffect` 保存成功 → `ClearEditableQuestion`

**验证：** `compileDebugKotlin` + session/arch 单测 + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P52：Init/Overlay/Lifecycle Command + Exam 对称

**范围：** Practice Init/Overlay Effects → `SessionCommand`；Exam init/交卷/直接结束 → Command；文档同步 + TD-014 关闭。

**domain：**
- `GoToQuestion` 增加 `source` 参数（默认 `"goToQuestion"`）
- 新增 `LeaveReviewSession`

**feature-practice：**
- `PracticeScreenEffects` — ReviewInit/QuizInit/OverlayPin/Lifecycle restore → `sendCommand`
- `PracticeScreenSessionHelpers` — `LeaveReviewSession` 替代 `bindings.leaveReviewSession()`
- Handler：`GoToQuestion(index, source)` / `LeaveReviewSession`

**feature-exam：**
- `ExamCommandDispatch.kt` — `dispatchExamCommand` / `suspendExamCommand`（对称 Practice）
- `ExamScreenEffects` — ReviewInit/QuizInit → Command；交卷 `suspendExamCommand(GradeSession)`
- `ExamScreenContent` — FinishDirect → Command
- `ExamEndFlow` → `suspendExamCommand`

**文档：** `tech_debt` / `refactoring_plan` Phase 10 路线图 / `session_architecture` K-001 CLOSED / `current_state` P52

**验证：** `compileDebugKotlin` + session 单测 + `check-loc-over-500.ps1` ✅

---

## 2026-07-05 — P51：bindings 长尾 Command + PracticeScreen → feature-practice

**范围：** 答题/编辑/分析写回经 `SessionCommand`；Practice UI 迁入 `:feature-practice`（Exam 对称）。

**新增：**
- `PracticeScreenContent` / `ExternalPracticeState` / `PracticeCommandDispatch`（`:feature-practice`）
- `PracticeAISyncEffects`（`:app` — AI VM + `SessionAnalysisLoader`）
- `PracticeRichAnalysisSection` — 结果区解析块（替代 app 内联 `ExamAnalysisSection`）

**变更：**
- Practice：选项/填空/编辑/笔记/解析删除/重答/历史写回 → `sendCommand`
- Exam：`UpdateTextAnswer` / `PrepareEditableAtIndex` / `SaveNote` / `UpdateAnalysis*` 对称 Command
- `ExamAISyncEffects` → `SessionAnalysisSyncPipeline` + `dispatchCommand`
- `app/PracticeScreen.kt` 薄包装（Settings/Favorite/WrongBook/Sound/AI VM → `ExternalPracticeState`）
- components 迁至 `feature-practice/.../components/`；`feature.practice.R` + 去重 `export.header.*` 冲突

**验证：** K-007 真机 PASS；`compileDebugKotlin` + `PracticeSessionEngineTest` + `check-loc-over-500.ps1`

**P51b：** `PracticeScreenOverlays` 交卷确认 → `suspendPracticeCommand(GradeSessionOnSubmit)` + `sendCommand(AddHistoryRecord)`

---

## 2026-07-05 — P50：bindings→Command 导航收编 + detekt + K-007 清单

**范围：** V5 长尾 #2 — Practice/Exam 导航 UI 改走 `SessionCommand`；静态分析门禁；Exam 路由冒烟清单。

**新增：**
- `ExamCommandOutcome` — 复盘历史滑动结果
- `rememberPracticeCommandDispatcher` / `rememberExamCommandDispatcher`
- `detekt.yml` + 根工程 detekt 插件
- `.ai/K007_EXAM_ROUTE_SMOKE.md` — Exam SessionHost 路由真机清单

**变更：**
- `PracticeScreenBottomBar` / `practiceHistorySwipe` / `PracticeScreen` — 导航经 `SessionCommand`
- `ExamScreenBottomBar` / `examScreenGesture` / `ExamScreenContent` / `ExamScreen` — 对称 Command 路径
- `ExamSessionCommandHandler.dispatch` — `BrowseAnsweredHistory*` → `browseReviewAnswered*`

**验证：** `ktlintCheck` + `presentation.session.*` + `detekt` + `check-loc-over-500.ps1`

**待续：** 答题/编辑/分析写回 Command 收编；K-007 真机执行

---

## 2026-07-05 — P49：V5 长尾 #2 — History Gate 对称 + Practice prevQuestion 路线化

**范围：** Exam history slice（Strategy 门禁对称接线）；Practice `prevQuestion` 与 Exam 对齐。

**新增：**
- `NavigationSequentialPrev.kt` — 答后 backward 路线（`SessionPostAnswerNavigationPipeline` BACKWARD）
- `SessionPracticePostAnswerNavigationPipeline.resolveBackwardAdvanceRoute`

**变更：**
- `AnsweredHistorySwipeNavigator` — 去掉 `internal`，跨 package 可调用
- `NavigationController` — `prevQuestion`；`goToQuestion` / `resetNavigationForManualJump` 经 `SessionNavigationHistoryGate`
- `PracticeNavigationCoordinator` — `prevQuestion`；快照写入经 Gate
- `PracticeSessionNavigationDelegate` / `ExamSessionNavigationDelegate` — `prevQuestion` 对称路线 + `isReviewPostAnswerNavOnly`；Exam `goToQuestion` Gate 对称（无 NavigationHistory 时为 no-op）

**验证：** `NavigationHistoryIntegrationTest` + `core.session.strategy.navigation.*` + `PracticeSessionEngineTest` + `ktlintCheck` + `check-loc-over-500.ps1`

---

## 2026-07-05 — P48：NavigationHistory Strategy 收编 + LOC 瘦身

**范围：** V5 长尾 #1 — 删除 `NavigationHistory` 内联旧实现，启用 P21–P23 Pipeline 路径。

**变更：**
- `NavigationHistory.kt` — 762→~230 行；快照/overlay/random 委托 `NavigationHistorySnapshots` + `SessionRandomNavigationHistoryPipeline`；滑动导航委托 `AnsweredHistorySwipeNavigator`
- `AnsweredHistorySwipeNavigation.kt` — extension 改为 `AnsweredHistorySwipeNavigator` object（跨 package 可见）
- 移除与 Pipeline 版重复的 member 实现（此前 extension 被 member 遮蔽，P23 未生效）

**验证：** `NavigationHistoryIntegrationTest` + `core.session.strategy.navigation.*` + `ktlintCheck` + `check-loc-over-500.ps1`

---

## 2026-07-05 — P47：AppNavHost → SessionHost 路由

**范围：** tech_debt 建议顺序 #1 — 移除 global Practice/Exam VM，Practice/Exam Screen 改走 bindings。

**新增：**
- `AppNavSessionRoutes.kt` — `registerPracticeSessionRoutes` / `registerExamSessionRoutes`

**变更：**
- `AppNavHost` — 删除 `globalPracticeViewModel` / `globalExamViewModel`；Practice/Exam/Review/错题/收藏 → `*Route` + `SessionHost`；AI 叠层 → `registerAiOverlayRoutes` + `NavSessionOwners`
- `PracticeScreen` + components — `PracticeScreenBindings` + `sessionHosted`（跳过 Quiz/Review Init）
- `ExamScreen` / `ExamScreenContent` + components — `ExamScreenBindings` + `sessionHosted`

**验证：** `:app:compileDebugKotlin` + `ktlintCheck` + `check-loc-over-500.ps1`

---

## 2026-07-05 — VERIFY：K-001 真机冒烟 PASS

**结论：** 首页进度持久化 + 抽屉 Browse 重入通过（A/B 段）。

**文档：** `tech_debt.md` K-001 → Closed；`K001_DEVICE_SMOKE.md` 执行记录更新。

---

## 2026-07-05 — P46：Browse 路由 + 抽屉点题接线

**范围：** K-001 通过后落地抽屉 Browse 导航（此前仅清单/单测）。

**新增：**
- `HomeDrawerBrowseNavigationPipeline` — 离开主页前快照搜索态

**变更：**
- `AppNavHost` — `question/{quizId}?targetQuestionId=` → `BrowsePracticeRoute` 或 `PracticeScreen`
- `HomeScreen` — 抽屉点题携带 `questionId`；返回后 `HomeDrawerRestoreHolder` 恢复抽屉/搜索
- `QuestionBankDrawer` — `onQuestionSelected(fileName, questionId, searchQuery)`

**验证：** `:app:compileDebugKotlin` + Browse 相关单测

---

## 2026-07-05 — P45：K-001 冒烟清单 + 文档同步 + ktlint 收紧

**范围：** 真机验证清单、记忆层文档更新、lint 门禁收紧。

**新增：**
- `.ai/K001_DEVICE_SMOKE.md` — 首页进度 + 抽屉 Browse 重入步骤
- `scripts/check-loc-over-500.sh` — Linux CI LOC 门禁
- `OptimizedFileCard` — `home_file_card:` 语义（Macrobenchmark 对齐）

**变更：**
- `current_state.md` / `tech_debt.md` / `loc_audit.md` / `session_architecture.md` / `ARCHITECTURE.md` 同步至 P44
- `build.gradle.kts` — `ktlint ignoreFailures=false`
- `.editorconfig` — Compose/项目惯例例外 + 关闭需全库重排的 wrapping 规则；`*.gradle.kts` 排除
- `ktlintFormat` — import-order / unused-import 等增量修复（~191 文件）
- CI — LOC gate 步骤
- 删除空占位 `BaiduQianfanApiService.kt` / `ChatGptApiService.kt`

**验证：** `ktlintCheck` ✅（`ignoreFailures=false`）

**待执行：** K-001 真机冒烟（清单 A/B 段）

---

## 2026-07-05 — P44：裸引擎 Extension + ktlint CI

**范围：** `rememberPracticeSessionBindings` 接 Extension 事件桥；CI 增加 ktlint / 单测 / JDK 21。

**新增：**
- `PracticeSessionExtensionEffects` — 裸引擎 `sessionState` → Extension 事件
- `SessionExtensionNotifier` — Extension 通知共用
- `rememberAppPracticeSessionBindings` — QuestionScreen 用
- `.editorconfig` — ktlint 规则
- CI：`ktlintCheck`、session/arch 单测、JDK 21

**变更：**
- `PracticeScreenBindings.sessionState` — 引擎暴露状态流
- `rememberQuestionEditSessionBindings` — 修复 `extensions` 传入 Registry
- `QuestionScreen` — `QuestionSessionKind.Practice` + Extension

---

**范围：** QuestionEdit 经 Hilt 注入 Extension；SessionHost 路径关闭 Effects 切题 DB 同步（改由 Extension）。

**新增：**
- `SessionExtensionsEntryPoint` / `rememberSessionExtensions()`

**变更：**
- `rememberQuestionEditSessionBindings` — 支持 `extensions` 参数
- `DrawerQuestionEditHost` — 注入 `rememberSessionExtensions()`
- `SessionAnalysisSyncEffects` — `syncStoredOnQuestionChange` 默认 `false`（无 Extension 时可开启）

---

## 2026-07-05 — P42：AnswerSubmitted 事件 + GradeDelegate 单测

**范围：** 批改展示时 emit `AnswerSubmitted` 驱动 Extension DB 同步；事件接线抽到 `:core`。

**新增：**
- `SessionExtensionEventWiring` — `QuestionChanged` / `AnswerSubmitted` 状态监听
- `SessionExtensionEventWiringTest`、`PracticeSessionGradeDelegateTest`

**变更：**
- `AbstractPracticeQuestionSession` / `AbstractExamQuestionSession` — 接入 Wiring + `AnswerSubmitted`
- `SessionAnalysisSyncEffects` — 移除 showResult / resultDisplayReady DB 同步（改由 Extension）

---

## 2026-07-05 — P41：Extension → SessionCommand 写回

**范围：** `SessionAiAnalysisExtension` 实装 DB 同步；切题时经 `SessionCommand` 写回会话，不再由 Effects 直连 bindings。

**变更：**
- `FeatureExtension.onEvent` — 改为 `suspend`，增加 `dispatch: (SessionCommand) -> Unit`
- `SessionAiAnalysisExtension` — `QuestionChanged` / `AnswerSubmitted` 时 Loader + Pipeline → `Update*Analysis`
- `AbstractPracticeQuestionSession` / `AbstractExamQuestionSession` — index 监听 + extension 接线（对称 Exam）
- `BrowseSession` — `onEvent` 传入 `dispatch`
- `SessionAnalysisSyncEffects` — 保留 ON_RESUME / showResult / 流式 VM；切题 DB 同步改由 Extension

---

**范围：** 退出 / 解析 / 作答提交逻辑外提，`PracticeScreen.kt` 压至 500 行以下。

**新增：**
- `PracticeScreenSessionHelpers` — analysis 解析、session 退出、post-answer advance
- `PracticeScreenOptionSubmitHandlers` — 选项点击与提交副作用

**变更：**
- `PracticeScreen.kt` — ~443 LOC（门禁通过）

---

## 2026-07-05 — P39：QuestionEdit SessionRegistry 注册

**范围：** 抽屉改题接入 `SessionRegistry`；`DrawerQuestionEditHost` 经 Registry 创建会话。

**新增：**
- `QuestionEditSession` / `QuestionEditSessionCreator` / `QuestionEditSessionBootstrap`
- `rememberQuestionEditSessionBindings` — Registry + `start()` / `destroy()` 生命周期
- `PracticeSessionRegistryEntryPoint`

**变更：**
- `SessionRegistryModule` — 注册 `QuestionSessionKind.QuestionEdit`
- `DrawerQuestionEditHost` — 改用 `rememberQuestionEditSessionBindings`；加载逻辑迁入 `start()`

**待续：** `PracticeScreen.kt` LOC 再拆（~508 行贴线）。

---

## 2026-07-05 — P38：QuestionEdit Kind + drawerBrowse 收编

**范围：** 抽屉改题从 `Practice+drawerBrowse` overlay 迁到独立 `QuestionSessionKind.QuestionEdit`；删除 browse overlay 层。

**新增：**
- `QuestionSessionKind.QuestionEdit` — 题库抽屉单题编辑
- `QuestionEditSessionStrategyBootstrap` — 编辑 Strategy 绑定
- `SessionCapabilitiesPresets.questionEdit` — Browse 类策略 + `canEditQuestion`

**变更：**
- `DrawerQuestionEditHost` — 绑 `QuestionEdit` kind
- `SessionStrategyContexts` — 移除 Practice+drawerBrowse 分支
- `PracticeSessionStrategyCoordinator` — 去掉 `browseOverlay` / `rebindBrowseOverlay`
- `rememberPracticeSessionBindings` — 移除 `persistenceContext` 参数
- 删除 `SessionBrowseOverlayContext`；`SessionPersistenceContext` / `SessionExitContext` 去掉 `drawerBrowse`

**待续：** SessionRegistry 可选注册 QuestionEdit；全量 LOC 扫描归档。

---

## 2026-07-05 — P37：Practice CoordinatorAssembly 对称装配

**范围：** Practice Engine 协调器装配外提，对齐 Exam Assembly + WireContext + RuntimeState 模式。

**新增：**
- `PracticeSessionRuntimeState` — progress / fill / random 可变状态
- `PracticeSessionWireContext` / `PracticeSessionCoordinatorHub` / `PracticeSessionCoordinatorAssembly`
- `PracticeSessionNavigationWiring` — `initPhase4` 导航编排接线

**变更：**
- `PracticeSessionEngine` — init 委托 Assembly + NavigationWiring（~360 LOC）

**待续：** `DrawerQuestionEditHost` Strategy 清理；Practice+drawerBrowse overlay 收编。

---

## 2026-07-05 — P36：Practice 批改/编辑委托 + Exam RuntimeState 瘦身

**范围：** Practice Engine 批改与题目编辑区块外提；Exam WireContext 经 `ExamSessionRuntimeState` 收敛可变状态。

**新增：**
- `PracticeSessionGradeDelegate` — reveal / grade / retry
- `PracticeSessionQuestionContentDelegate` — 题目内容编辑
- `ExamSessionRuntimeState` — progress / memory / artifact 标志分组

**变更：**
- `PracticeSessionEngine` — 批改与编辑方法委托 Delegate（~442 LOC）
- `ExamSessionWireContext` — 注入 `runtime`，computed accessor 替代 ~30 个 lambda 字段
- `ExamSessionLoadDelegateWiring` — 接受 `ExamSessionRuntimeState`
- `ExamSessionEngine` — 运行时可变字段委托 `runtime`（~430 LOC）

**待续：** `PracticeSessionCoordinatorAssembly` 对称装配；`DrawerQuestionEditHost` Strategy 清理。

---

## 2026-07-05 — P35：Exam Engine 装配拆分 + Feature 模块单测

**范围：** ExamSessionEngine 协调器装配外提；Navigation Delegate 补全；feature 模块 JUnit 基建。

**新增：**
- `ExamSessionCoordinatorAssembly` / `ExamSessionWireContext` / `ExamSessionCoordinatorHub`
- `ExamSessionLoadDelegateWiring`
- `feature-exam` / `feature-practice` — `testImplementation(junit)` + Coordinator 单测

**变更：**
- `ExamSessionNavigationDelegate` — 顺序导航 / 未答跳转 / 复盘历史浏览
- `ExamSessionEngine` — init 委托 Assembly + LoadDelegateWiring（目标 ≤500 LOC）

**待续：** Practice Engine 编辑/批改区块抽取；Assembly 上下文进一步瘦身。

---

## 2026-07-05 — P34：Exam Strategy Coordinator + Navigation Delegate 拆分

**范围：** Practice/Exam Engine LOC 瘦身；Strategy 与导航门禁对称抽取。

**新增：**
- `ExamSessionStrategyCoordinator` — 对称 Practice Strategy 绑定 / 复盘快照
- `PracticeSessionNavigationDelegate` — Strategy 门禁 + NavigationCoordinator
- `ExamSessionNavigationDelegate` — Exam 导航对称委托

**变更：**
- `PracticeSessionEngine` — 导航方法委托 Delegate（目标 ≤500 LOC）
- `ExamSessionEngine` — Strategy / 导航委托 Coordinator + Delegate

**待续：** Engine 编辑/批改区块进一步抽取；`feature-practice` 模块单测基建。

---

## 2026-07-05 — P33：Exit deprecated 清理 + PracticeSessionEngine Strategy 拆分

**范围：** 移除 Screen 层 boolean 退出路由；Strategy 绑定从 Engine 抽出 Coordinator。

**新增：**
- `PracticeSessionStrategyCoordinator` — bind / config 门面 / 复盘快照

**变更：**
- 删除 `PracticeSessionExitPipeline.resolveForScreen`
- 删除 `ExamSessionExitPipeline.resolve(isReviewMode, …)`
- 删除 `SessionExitGate.configForPracticeScreen` / `configForExamScreen`
- `PracticeSessionEngine` — Strategy 逻辑委托 Coordinator（~580 LOC）
- 单测改走 `exitConfig` / `SessionExitConfig` 直传

**待续：** Exam Strategy Coordinator 对称拆分；Engine 进一步 navigation 区块抽取。

---

## 2026-07-05 — P32：抽屉浏览路由收编 + Review 退出 Strategy 恢复

**范围：** 点题跳转统一走 `BrowsePracticeRoute`；移除 Practice QuizInit 抽屉浏览 overlay 路径；Review 退出恢复 Strategy。

**新增：**
- `BrowseSessionRoutePipeline` — `shouldUseBrowseSession` / `browseKind` / `practiceQuestionRoute`
- `sessionStrategyConfig()` on Practice & Exam bindings
- `leaveReviewSession()` — 退出复盘时恢复进入前 Strategy 快照
- `BrowseSessionRoutePipelineTest`

**变更：**
- `AppNavHost` / `BrowsePracticeRoute` — 路由判定经 Pipeline
- `PracticeQuizInitPipeline` — 移除 `pinnedQuestionId` / `drawerBrowse` 分支
- `PracticeScreen` / `PracticeScreenEffects` — 移除 `targetQuestionId` 参数
- `SessionCommand.SetProgressId` — 精简为加载参数（去除 persistence overlay 字段）
- `PracticeScreen` / `ExamScreenContent` — ReviewBack 先 `leaveReviewSession()`

**待续：** `resolveForScreen` 等 deprecated exit 重载清理；`PracticeSessionEngine` LOC 拆分。

---

## 2026-07-05 — P31：Review Session Strategy 收编

**范围：** `enterReviewSession` 触发 Strategy 重绑；Screen 从 bindings 取 config，去除 exit 层 isReviewMode 路由。

**新增：**
- `ReviewSessionStrategyBootstrap` — practice/exam Review kind 解析 + context
- `bindSessionStrategy` / `navigationConfig()` on Practice & Exam bindings
- `ReviewSessionStrategyBootstrapTest`

**变更：**
- `PracticeSessionEngine.enterReviewSession` / `ExamSessionEngine.enterReviewSession` — 先 `bindStrategy(Review)`
- `PracticeScreen` / `ExamScreenContent` — `exitConfig()` / `navigationConfig()` 驱动 UI
- `*SessionExitPipeline` — 主入口 `resolve(exitConfig, …)`

**待续：** 抽屉浏览迁移至 `Browse` 路由。

---

## 2026-07-05 — P30：Exam 持久化对称收编 + NavigationHistory 集成测

**范围：** Exam 侧 `SessionStrategyContext.persistence` 为唯一数据源；NavigationHistory 端到端回归。

**新增：**
- `ExamProgressPersistencePipeline` + 单测
- `NavigationHistoryIntegrationTest` — snapshot/apply/resume、ordered indices、random history
- `persistenceConfig()` / `exitConfig()` on `ExamScreenBindings`

**变更：**
- `ExamSessionEngine` — 移除独立 `persistenceConfig` 字段；`applyStrategyContext` 对称 Practice

**待续：** 抽屉浏览迁移至 `Browse` 路由；Review Session Strategy 收编。

---

## 2026-07-05 — P29：PracticeProgressLifecycleCoordinator Strategy 收编

**范围：** 进度持久化单一数据源 `SessionPersistenceConfig`；去除三 boolean 分叉。

**新增：**
- `PracticeProgressPersistencePipeline` — restore / persist / saveOnNavigation 判定
- `persistenceConfig()` on `PracticeScreenBindings`
- `PracticeProgressPersistencePipelineTest`

**变更：**
- `PracticeProgressLifecycleCoordinator` — `applyPersistenceConfig`；`setProgressId` 不再覆盖 Strategy
- `PracticeSessionEngine.applyStrategyContext` — 唯一持久化策略注入点
- `SetProgressId` 命令 — 仅 `rebindBrowseOverlay` + 加载参数

**待续：** NavigationHistory 集成测；Exam 侧对称收编。

---

## 2026-07-05 — P28：Browse Session Strategy 垂直切片

**范围：** 抽屉浏览 / `BrowseSession` 四类 Strategy 对齐；`exitConfig()` 接线。

**新增：**
- `SessionBrowseOverlayContext` — drawerBrowse 统一覆盖上下文
- `SessionBrowseStrategyGate` / `BrowseSessionStrategyBootstrap`
- `SessionBrowseStrategyGateTest`

**变更：**
- `SessionStrategyContexts` — Practice+drawerBrowse 时 navigation/reveal 降级为 Browse 策略
- `BrowseSession.start()` — Strategy 绑定 + 线性浏览断言
- `PracticeSessionEngine` — `bindStrategy(browseOverlay)` / `exitConfig()` / `rebindBrowseOverlay()`
- `PracticeScreen` — 退出经 `bindings.exitConfig()` + `resolveForScreen`
- `SetProgressId` — 触发 `rebindBrowseOverlay`

**待续：** `PracticeProgressLifecycleCoordinator` 收编；NavigationHistory 集成测。

---

## 2026-07-05 — P27：ExitPolicy Strategy Gate 收编

**范围：** Exit 行为经 `SessionExitConfig` + `SessionExitGate` 统一门禁；StrategyContext 扩展第四维。

**新增：**
- `SessionExitMode` / `SessionExitConfig` / `SessionExitContext`（domain）
- `SessionExitGate` — resolve / screen config / submit-dialog 门禁
- `ExitPolicyFactory.configForKind` / `policyForConfig`
- `SessionExitGateTest` + StrategyContext exit 用例

**变更：**
- `SessionStrategyContext` 增加 `exit`；`SessionStrategyFactory.exit()`
- `PracticeSessionExitPipeline` / `ExamSessionExitPipeline` 薄委托 Gate

**待续：** Browse Session 垂直切片；`PracticeProgressLifecycleCoordinator` 进一步收编。

---

## 2026-07-05 — P26：Persistence / Reveal Strategy Gate 首 slice

**范围：** 持久化与 reveal 行为经 Strategy Gate 统一门禁；Engine / Coordinator / CommandHandler 对称接线。

**新增：**
- `SessionPersistenceGate` — persist / restore / saveOnNavigation
- `SessionRevealGate` / `SessionRevealSubmitPipeline` — reveal 模式与显式 RevealAnswer 路由
- `SessionPersistencePolicy.resolveConfig()` / `SessionRevealPolicy.resolveConfig()` 扩展
- 对应单测

**变更：**
- `PracticeProgressLifecycleCoordinator` — 持有 `SessionPersistenceConfig` + Gate
- `PracticeSessionEngine` / `ExamSessionEngine` — `revealConfig()`；Exam 持久化 Gate
- `PracticeSubmitRevealPipeline` / CommandHandler — RevealAnswer 经 Pipeline
- `PracticeScreen` — `autoAdvanceAfterReveal` 经 `SessionRevealGate`

**待续：** ExitPolicy 收编；Browse Session 垂直切片。

---

## 2026-07-05 — P25：NavigationHistory 瘦容器 + IndexPipeline + 记忆常量统一

**范围：** `NavigationHistory` 压至瘦容器；快照/状态转换拆文件；orderedIndices 构建收编至 Strategy。

**新增：**
- `SessionAnsweredHistoryIndexPipeline` — buildSwipeHistoryIndices / applyMemoryRoundPriority
- `NavigationHistorySnapshots` / `NavigationHistoryStateTransitions`（feature-practice）
- `SessionAnsweredHistoryIndexPipelineTest`

**变更：**
- `NavigationHistory` — 委托 Snapshots / StateTransitions / IndexPipeline（~150 行）
- `PracticeAnsweredBrowseNavigation` — 瘦委托 IndexPipeline
- `NavigationController` / `PracticeNavigationCoordinator` — `SessionMemoryMode.MEMORY_POOL_MODE_ROUND`

**待续：** NavigationHistory 相关单测；Exam history（`swipeAnsweredHistory=false` 暂不需要）。

---

## 2026-07-05 — P24：NavigationHistory overlay / log 拆分 + Pipeline 收编

**范围：** overlay / resume / debug 纯逻辑迁入 Pipeline；滑动导航与 debug 日志拆至独立文件；`NavigationHistory` 瘦身。

**新增：**
- `SessionAnsweredHistoryOverlayPipeline` / `SessionAnsweredHistoryResumePipeline` / `SessionAnsweredHistoryDebugPipeline`
- `SessionAnsweredHistorySnapshotPipeline` 扩展 resolveBrowsable / shouldKeepLive
- `NavigationHistoryDebugLog` / `AnsweredHistorySwipeNavigation`（feature-practice）
- 对应单测

**变更：**
- `NavigationHistory` — 快照/overlay/resume 委托 Pipeline；滑动导航迁出
- `PracticeFullAnswerHistoryNavigation.formatOrderedDebugLine` — 委托 DebugPipeline

**待续：** `NavigationHistory` 继续压至 LOC 红线以下；Exam history slice。

---

## 2026-07-05 — P23：NavigationHistory browse context / commit / random Pipeline

**范围：** browse 上下文去重、commit 计划、random 历史栈收编至 Strategy Pipeline。

**新增：**
- `SessionAnsweredHistoryBrowseContext` / `SessionAnsweredHistoryNavigationUpdate`（domain）
- `SessionAnsweredHistoryBrowseContextPipeline` — origin / anchor / ordered 统一解析
- `SessionAnsweredHistoryCommitPipeline` — navigationUpdate / forward-miss resume 判定
- `SessionRandomNavigationHistoryPipeline` — seed / append origin
- 对应单测

**变更：**
- `NavigationHistory` — `resolveBrowseContext`；双滑分支去重；random / commit 委托 Pipeline

**待续：** `NavigationHistory` 进一步瘦身（log / overlay 区块）；Exam history slice。

---

## 2026-07-05 — P22：全答历史滑动 Pipeline 收编 + 目标路由统一

**范围：** `PracticeFullAnswerHistoryNavigation` 纯逻辑迁入 `:core`；`NavigationHistory` 滑动目标经统一路由。

**新增：**
- `SessionFullAnswerHistoryBrowsePipeline` — 池内优先 / 跨词条 resolveOlder/Newer
- `SessionFullAnswerSourcePoolPipeline` — 词条池索引
- `SessionAnsweredHistoryTargetPipeline` — 标准 / 全答目标路由
- 对应单测

**变更：**
- `PracticeFullAnswerHistoryNavigation` — 瘦委托 + debug 日志
- `PracticeFullAnswerIconNavigation.sourceIndices` — 委托 SourcePoolPipeline
- `NavigationHistory` — sourcePool / older / newer 经 Strategy Pipeline

**待续：** `NavigationHistory` random seed / commit 状态机拆分；`formatOrderedDebugLine` 可选下沉。

---

## 2026-07-05 — P21：NavigationHistory 纯逻辑 Pipeline 首 slice

**范围：** 从 `NavigationHistory` 收编快照写入与标准滑动目标解析至 `:core` Pipeline；行为不变。

**新增：**
- `SessionAnsweredHistorySnapshotPipeline` — shouldCapture / shouldReplaceExisting
- `SessionAnsweredHistoryBrowsePipeline` — resolveOlder/Newer / backwardStop / historyPosition
- 对应单测

**变更：**
- `NavigationHistory` — 快照与标准（非全答）browse 分支委托 Pipeline

**待续：** 全答 browse 分支（`PracticeFullAnswerHistoryNavigation`）收编；`NavigationHistory` 状态机进一步拆分。

---


**范围：** `SessionNavigationHistoryPhases` / `SessionNavigationHistoryGate` 首 slice；Practice 答后 forward prepare 与历史快照经 Gate；复盘 `prevQuestion` 语义不变（仅 `tryReviewNavigate`）。

**新增：**
- `SessionNavigationHistoryPhases` — orchestration → history 策略快照
- `SessionNavigationHistoryGate` — allowsBrowse / prepareForward / clearOnManualJump / trackSnapshots / isReviewPostAnswerNavOnly
- `SessionNavigationPolicy.historyPhases()` 扩展
- `SessionNavigationHistoryGateTest`

**变更：**
- `NavigationEnvironment.prepareStateForPostAnswerForward()` — Gate 包装 `history.prepareStateForForwardNavigation`
- `NavigationSequentialNext` — 经 env wrapper 而非直接 history
- `NavigationController` browse / manual jump — 改用 `SessionNavigationHistoryGate`
- `PracticeNavigationCoordinator` — 持有 orchestration；`rememberAnsweredHistorySnapshot` 经 Gate

**待续：** 完整 `NavigationHistory.kt` 提取；Exam history slice（若产品需要）。

---

> Chronological log for agent continuity. Source: `TASK_LOG.md`.


---


## 2026-07-05 — P19：Exam prevQuestion 对称路线化 + postAnswerPhases

**范围：** Exam 答后 `prevQuestion` 与 `nextQuestion` 共用 Pipeline；Policy 暴露答后阶段编排。

**新增：**
- `SessionPostAnswerPhases` — 从 Orchestration 投影答后五标志
- `SessionPostAnswerAdvanceDirection` — FORWARD / BACKWARD 路线解析
- `SessionNavigationPolicy.postAnswerPhases()`

**变更：**
- `SessionPostAnswerNavigationPipeline.routeAfterRoundPoolChecks(direction)` — backward 跳过 FULL_ANSWER_SEQUENTIAL
- `ExamNavigationCoordinator.runPostAnswerAdvance` — next/prev 对称

**待续：** Practice `prevQuestion` 路线化（若产品需要）；NavigationHistory 迁入 Strategy。

---

## 2026-07-05 — P18：NavigationSequentialNext 路线化 + Policy 答后编排

**范围：** Practice 答后 auto-advance 内部分支经 Pipeline 门禁；mode 专属答后编排写入 `SessionNavigationOrchestration`。

**新增：**
- `SessionNavigationOrchestration` 答后字段（resume / sourceStay / nextSource / adjacent / reopen）
- `SessionPracticePostAnswerNavigationPipeline` — SequentialNext 各阶段 allows* 判定

**变更：**
- `SessionNavigationOrchestrationResolver` — 四类 Policy mode 答后编排矩阵
- `NavigationSequentialNext` — 分阶段走 Pipeline + `SessionPostAnswerAdvanceRoute` final 分支

**待续：** Policy 对象直接暴露 `postAnswerPhases()`；Exam `prevQuestion` 对称路线化。

---

## 2026-07-05 — P17：IconTap / PostAnswer 路线 Pipeline（首 slice）

**范围：** icon 单击/双击与答后 advance 分支决策迁入 core Pipeline；Practice/Exam Coordinator 改走统一路线解析。

**新增：**
- `SessionIconTapNavigationPipeline` — `SessionIconTapPath` / `SessionIconDoubleTapAction` + orchestration 门禁
- `SessionPostAnswerNavigationPipeline` — 轮次池 guard + `SessionPostAnswerAdvanceRoute`

**变更：**
- `NavigationUnansweredIconNav` / `NavigationEnvironment` / `NavigationController` — icon 路线走 Pipeline
- `ExamNavigationCoordinator` — icon + `nextQuestion` 答后路线走 Pipeline
- `PracticeIconUnansweredNavigationPipeline` / `ExamIconUnansweredNavigationPipeline` — 委托 Pipeline

**待续：** `NavigationSequentialNext` 内部分支持路线化；Policy 实现类承载 mode 专属分支。

---

## 2026-07-05 — P16：SessionNavigationOrchestration + OrchestrationGate（首 slice）

**范围：** 编排决策从 Controller/Coordinator 上收至 Strategy 层；Engine/Coordinator 统一经 `SessionNavigationOrchestrationGate` 门禁。

**新增：**
- `SessionNavigationOrchestration` — behavior + 历史退出 / 手动跳题清栈 / 双击全答门禁
- `SessionNavigationOrchestrationResolver` — `SessionNavigationConfig` → orchestration
- `SessionNavigationOrchestrationGate` — 统一 allows* / shouldExit* 判定
- `SessionNavigationPolicy.orchestration()` 扩展

**变更：**
- `NavigationEnvironment` / `NavigationController` — `navigationOrchestration` + Gate
- `PracticeNavigationCoordinator` / `PracticeSessionEngine` — `applyNavigationOrchestration`
- `ExamNavigationCoordinator` / `ExamSessionEngine` — 对称迁移

**待续：** iconTap 路径 / sequentialNext 分支等执行逻辑逐步迁入 Policy 实现 + Pipeline 单测。

---

## 2026-07-05 — P15：ExamNavigationCoordinator behavior 注入 + 双层门禁

**范围：** Exam 导航 Coordinator 与 Practice `NavigationController` 对称；Engine + Coordinator defense-in-depth。

**变更：**
- `ExamNavigationCoordinator.navigationBehavior` — 默认 `examDefault()`，`applyNavigationBehavior` 更新
- 门禁：icon/sequential/postAnswer/double-click/canNavigate*/canGo*Sequential
- `ExamSessionEngine.bindStrategy` — 同步 behavior 到 Coordinator

**待续：** NavigationController / ExamNavigationCoordinator 编排逻辑迁入 Strategy 实现类（长期）。

---

## 2026-07-05 — P14：NavigationController behavior 注入 + 双层门禁

**范围：** `SessionNavigationBehavior` 注入 `NavigationEnvironment`；`NavigationController` 与 Engine 对称 defense-in-depth 门禁。

**变更：**
- `NavigationEnvironment.navigationBehavior` — 默认 `practiceDefault()`，由 `bindStrategy` 更新
- `NavigationController.applyNavigationBehavior` — icon/sequential/history/double-click/canNavigate* 门禁
- `PracticeNavigationCoordinator.applyNavigationBehavior` — 委托 Controller
- `PracticeSessionEngine.bindStrategy` — 同步 navigation behavior 到 Coordinator

**待续：** 将 NavigationController 编排逻辑逐步迁入 Strategy 实现类（长期）；Exam `ExamNavigationCoordinator` 对称 behavior 注入。

---

## 2026-07-05 — P13：Navigation Behavior 解析 + Engine 门禁

**范围：** 将 NavigationController 编排决策提炼为 `SessionNavigationBehavior`；Practice/Exam Engine 导航路径统一经 behavior 门禁。

**新增：**
- `SessionNavigationBehavior` — icon/顺序/历史浏览/双击跨源等行为开关
- `SessionNavigationBehaviorResolver` — `SessionNavigationConfig.mode` → behavior；`practiceDefault()` / `examDefault()`

**变更：**
- `SessionNavigationStrategyGate` — `behavior(config)` / `swipeAnsweredHistoryEnabled` 委托 resolver
- `PracticeSessionEngine` — `navigationBehavior()` 门禁 next/icon/double-click/history/canNavigate*
- `ExamSessionEngine` — 对称门禁 next/icon/double-click/sequential/history/canNavigate*

**待续：** `NavigationController` 内部编排迁入 Strategy 实现；可选将 behavior 注入 `NavigationEnvironment`。

---

## 2026-07-05 — P12：Exam bindStrategy + Navigation 门禁对称化

**范围：** Exam Engine 接入 Strategy；导航/持久化门禁与 Practice 对齐；QuizInit 去重；非 Host 场景可绑 Kind。

**新增：**
- `SessionNavigationStrategyGate`（替代 `PracticeNavigationStrategyGate`）— 复盘滑动 / 考试顺序导航等门禁
- `ExamQuizInitPipeline` — Bootstrap 与 `ExamScreenQuizInitEffect` 共用

**变更：**
- `ExamSessionEngine.bindStrategy` — 持久化 + 复盘导航 + 历史滑动门禁；`saveProgress` / `scheduleNavigationSave` 受策略控制
- `AbstractExamQuestionSession` init 绑定策略
- `PracticeSessionEngine.tryReviewNavigate` — 复盘浏览走统一门禁
- `rememberPracticeSessionBindings(sessionKind?, persistenceContext)` — `DrawerQuestionEditHost` 绑 `drawerBrowse` 策略

**待续：** `NavigationController` 内部编排迁入 Strategy 实现类。

---

## 2026-07-05 — P11：Strategy 上下文 + Coordinator 策略接线（首版）

**范围：** Policy 从「仅 UI 读配置」扩展到 Engine/Coordinator 行为门禁；QuizInit 去重。

**新增：**
- `SessionStrategyContext` / `SessionStrategyContexts` — Kind → 三类 Policy 快照
- `PracticeNavigationStrategyGate` — 历史滑动 / 复盘浏览门禁
- `PracticeQuizInitPipeline` — Bootstrap 与 `PracticeScreenQuizInitEffect` 共用

**变更：**
- `PracticeSessionEngine.bindStrategy(kind)` — Session 启动时注入策略；`scheduleNavigationSave` 受 `saveOnNavigation` 控制
- `PracticeProgressLifecycleCoordinator.applyPersistenceStrategy` — 持久化三标志统一入口
- `SetProgressId` 支持 `persistenceContext` → `PersistencePolicyFactory` 解析
- `AbstractPracticeQuestionSession` init 绑定策略
- `PracticeSessionBootstrap` 瘦身 → 委托 `PracticeQuizInitPipeline`

**待续：** NavigationController 编排逻辑迁入 Strategy；Exam Engine 对称 `bindStrategy`。

---

## 2026-07-05 — P10：Lifecycle Command 化 + Bootstrap 走 Handler

**范围：** 会话启动 / 复盘 / 交卷批改等生命周期写操作改走 `SessionCommand`；Bootstrap 与 Effects 不再直连 `bindings`。

**新增 Command：**
- `EnterReviewSession` / `EnterExamReviewSession`
- `SetRandomPractice` / `SetRandomExam` / `SetMemoryModeConfig`
- `ReloadForFillConfig` / `SetProgressId`
- `LoadWrongQuestions` / `LoadFavoriteQuestions` / `LoadQuestions`
- `GradeSessionOnSubmit`（suspend）

**变更：**
- `PracticeSessionBootstrap` / `ExamSessionBootstrap` → `*SessionCommandHandler`
- `PracticeScreenEffects` / `ExamScreenEffects` / `ReviewPracticeSession`
- `PracticeScreenOverlays` — `gradeSessionOnSubmit` 经 `suspendPracticeCommand`
- `DrawerQuestionEditHost` / `QuestionScreen` — `SetProgressId`

**待续：** Coordinator 实体逻辑迁入 Strategy 实现（Navigation / Progress 大块）。

---

## 2026-07-05 — P9：编辑 / 历史 / 重试 / 交卷 Command 化

**范围：** 余下 Screen 直连 `bindings` 的写操作改走 `SessionCommand`；suspend 写操作经 `suspendHandle`。

**新增 Command：**
- `ClearEditableQuestion`、`PrepareEditableQuestion`、`PrepareEditableAtIndex`
- `UpdateQuestionAllFields`、`AddHistoryRecord`
- `RetryCurrentQuestion`、`RetryWrongBlanks`、`SelectOptionWithSkip`
- `SaveEditedQuestionFields`、`SaveEditedQuestion`、`GradeSession`

**变更：**
- `PracticeSessionCommandHandler` / `ExamSessionCommandHandler` — 扩展 handle + `suspendHandle`
- `SessionCommandDispatch` — `suspendPracticeCommand` / `suspendExamCommand`
- `PracticeScreen` / `PracticeScreenOverlays` / `PracticeScreenQuestionScrollContent`
- `ExamScreenContent` / `ExamScreenEffects` / `ExamEndFlow`
- `DrawerQuestionEditHost` / `QuestionScreen`

**待续：** Lifecycle 命令（`setProgressId`、`enterReviewSession`）；Coordinator 实体迁入 Strategy。

---

## 2026-07-05 — P8：底栏 / 手势 CQRS + Policy 接线

**范围：** 底栏、历史滑动、考试手势等主交互改走 `SessionCommand`；Navigation/Reveal Policy 驱动 UI。

**新增：** `PracticeCommandOutcome`；Command 扩展 `Nav*Icon`、`BrowseAnsweredHistory*`、`SetShowResult`、`SaveNote` 等

**待续：** Coordinator 实体迁入 Strategy；编辑题 / 交卷弹窗 Command 化。

---

## 2026-07-05 — P7：Strategy 收编 + SessionCommand CQRS

**范围：** §7 未做项首版落地——三类 Policy 工厂、AI 写回走 Command、Screen 主路径 CQRS、Flows 重命名。

**新增：**
- `domain/session/persistence|navigation|reveal/*Policy`
- `core/session/policy/persistence|navigation|reveal/*` + `SessionStrategyFactory`
- `SessionCommand` 扩展：`ToggleOption`、`RevealAnswer`、`Update*Analysis`
- `PracticeSessionCommandHandler` / `ExamSessionCommandHandler`
- `SessionCommandDispatch`（`dispatchPracticeCommand` / `dispatchExamCommand`）

**变更：**
- `PracticeSessionBootstrap` / `PracticeScreenQuizInitEffect` → `PersistencePolicyFactory`
- `SessionAnalysisSyncPipeline` / `Effects` → `dispatch(SessionCommand)`
- `PracticeScreen` / `ExamScreen` — 答题、跳转、解析写回走 `dispatchCommand`
- `PracticeViewModelSessionFlows` → `PracticeSessionFlows`；`ExamViewModelSessionFlows` → `ExamSessionFlows`

**待续：** Coordinator 实体迁入 Strategy 实现；Screen 余下 bindings 直连（底栏、编辑题等）逐步 Command 化。

---

## 2026-07-05 — P6：遗留 VM 退役

**范围：** 答题主流程不再依赖 `PracticeViewModel` / `ExamViewModel` 薄包装。

**删除：** `PracticeViewModel.kt`、`ExamViewModel.kt`

**新增：** `rememberPracticeSessionBindings()` — 非 SessionHost 场景（题库编辑 / 抽屉改题）直接持 `PracticeSessionEngine`

**变更：**
- `PracticeScreen` / `ExamScreen` — `sessionBindings` 必填（由 SessionHost 注入）
- `QuestionScreen` / `DrawerQuestionEditHost` — 改 `rememberPracticeSessionBindings()`
- `PracticeViewModelTest` → `PracticeSessionEngineTest.kt`（测引擎；`runWithEngine` + 独立 scope 避免 `runTest` 与 `Dispatchers.Default` 死锁）

**验证：** `PracticeSessionEngineTest` 6/6、`ExitPolicyFactoryTest`、`PracticeSessionExitPipelineTest`、`ExamSessionExitPipelineTest` 通过。

## 2026-07-05 — P5：SessionAnalysisLoader + Extension 接线

**范围：** AI 解析 DB 读取与 ViewModel 解耦；`SessionAiAnalysisExtension` 注入 Loader。

**新增：**
- `SessionAnalysisLoader`（`GetQuestionAnalysisUseCase` 等）
- `SessionAnalysisLoaderEntryPoint` + `rememberSessionAnalysisLoader()`
- `SessionAnalysisSyncPipeline` 改走 Loader（流式 VM 仍负责 `analysisPair` 推送）

**待续：** Extension → Session 写回需 `SessionCommand` 通路；当前写回仍在 `SessionAnalysisSyncEffects`。

---

## 2026-07-05 — P4：ExitPolicy 策略收编

**范围：** 退出/交卷判定从 Screen Pipeline 迁入 `:domain` + `:core` 策略。

**新增：**
- `SessionExitAction` / `SessionExitRequest` / `SessionExitPolicy`
- `BrowseExitPolicy` / `ReviewExitPolicy` / `PracticeExitPolicy` / `ExamExitPolicy`
- `ExitPolicyFactory.forKind(QuestionSessionKind)`

**适配：** `PracticeSessionExitPipeline` / `ExamSessionExitPipeline` 薄委托；`ExitPolicyFactoryTest`（含 AT-05 Browse 不交卷）。

---

## 2026-07-05 — P3：UI / Pipeline 去重

**范围：** Practice / Exam 共用 AI 同步与导航回调，消除双份实现。

**新增：**
- `core/session/SessionAnalysisSyncBindings` — 解析写回共用接口
- `SessionAnalysisSyncPipeline` + `SessionAnalysisSyncEffects`（`:app` shared）
- `QuestionSessionNavCallbacks` + `rememberQuestionSessionNavCallbacks`

**删除 / 合并：**
- `PracticeSessionAnalysisSyncPipeline`、`PracticeScreenAnalysisSyncEffects`
- `ExamAISyncEffects`
- `PracticeNavCallbacks` / `ExamNavCallbacks` 重复定义

**Bindings：** `PracticeScreenBindings` / `ExamScreenBindings` 继承 `SessionAnalysisSyncBindings`。

---

## 2026-07-05 — P2d：ExamSession + AiExtension 垂直切片

**范围：** 考试/复盘/错题本/收藏考试改走 `QuestionSessionKind.Exam`，`ExamViewModel` 瘦身为 `ExamSessionEngine` 包装。

**新增：**
- `ExamSessionEngine` / `ExamSessionDeps` / `ExamScreenBindings`
- `ExamSession` / `AbstractExamQuestionSession` / `ExamSessionCreator`
- `ExamSessionBootstrap` / `ExamSessionSnapshotMapper`
- `ExamExamRoute` / `rememberExamNavCallbacks`
- `SessionAiAnalysisExtension` + `SessionExtensionModule`（ADR-004 hook）
- Registry 注册 `ExamSessionCreator`

**路由：** `exam/{quizId}`、`exam_review/*`、`exam_wrongbook/*`、`exam_favorite/*` → SessionHost。

**VM 双轨：** `ExamViewModel.bindings` 供遗留入口；`ExamScreen` 接受 `sessionBindings` + `sessionHosted` 跳过 init effect。父叠层 `NavSessionOwners` 改 `examBindings`。

---

## 2026-07-05 — P2c：PracticeSession + ReviewSession 垂直切片

**范围：** 练习/复盘/错题本/收藏练习改走 `QuestionSessionKind.Practice|Review`，`PracticeViewModel` 瘦身为 `PracticeSessionEngine` 包装。

**新增：**
- `PracticeSessionEngine` / `PracticeSessionDeps` / `PracticeScreenBindings`
- `PracticeSession` / `ReviewPracticeSession` / `AbstractPracticeQuestionSession`
- `PracticeSessionBootstrap`（QuizInit 逻辑复用）
- `PracticePracticeRoute` / `ReviewPracticeRoute` / `rememberPracticeNavCallbacks`
- Registry 注册 `PracticeSessionCreator` + `ReviewPracticeSessionCreator`

**路由：** `question/{quizId}`（无 target）、`practice_review/*`、`practice_wrongbook/*`、`practice_favorite/*` → SessionHost；Browse 仍走 P2b。

**VM 双轨：** `PracticeViewModel.bindings` 供遗留入口；`PracticeScreen` 接受 `sessionBindings` + `sessionHosted` 跳过 init effect。

---

## 2026-07-05 — P2b：BrowseSession 垂直切片 + SessionHost

**范围：** 抽屉搜题（`targetQuestionId`）改走 `QuestionSessionKind.Browse`，与练习 VM 解耦。

**新增：**
- `BrowseSession` / `BrowseSessionLoadPipeline` / `BrowseSessionScreen`
- `QuestionSessionHostViewModel` + `SessionHost` composable
- `BrowsePracticeRoute`；`SessionRegistry` 注册 `BrowseSessionCreator`
- `SessionCreationContext`（scope + deps）

**路由：** `question/{quizId}?targetQuestionId=` → Browse；无 target → 原 `PracticeScreen`

---

**范围：** 纯契约与骨架，无运行时切换（BrowseSession 切片留 P2b）。

**新增：**
- `domain/session/*` — `QuestionSessionKind`, `SessionCapabilities`, Command/Event, Snapshot, `QuestionSession`, Extension, Host
- `core/session/registry/SessionRegistry` — Map O(1) Creator
- `core/session/policy/UiPolicyFactory` — Capabilities → UiContract
- `SessionRegistryModule`（Hilt 空注册表）
- `ArchitectureTest`（ArchUnit AT-01~AT-04 骨架）+ Registry/Factory 单测

---

**现象：**
1. 抽屉搜题进入练习后返回仍弹交卷确认，且主页抽屉/搜索态丢失
2. 未开全答模式时 `requireCorrect` 仍影响 pending 判定
3. 全局 `PracticeViewModel` / `ExamViewModel` 导致跨路由状态污染

**改动：**
- `PracticeSessionExitPipeline` — `isDrawerBrowse` 直退
- 抽屉浏览 `browse_{quizId}` 进度隔离，跳过恢复/持久化
- `HomeDrawerRestoreHolder` — 返回主页恢复抽屉与搜索词
- `PracticeFullAnswerModeActivePipeline` + `PracticeAnswerHandler` 门控修复
- `AppNavHost` — `hiltViewModel(backStackEntry)`；AI 叠层 `rememberParentSessionViewModels`

---

- `.ai/session_architecture.md` — SessionHost / Registry(Map) / Command·Event / Extension 分层 / 不可变 Snapshot
- `.ai/ADR/001-session.md` ~ `005-architecture-tests.md` — 架构决策与 ArchUnit/Detekt 规则

---

## 2026-07-05 — 主页抽屉搜索点题跳转

**现象：** 抽屉搜索后点击题目能进入对应题库，但未定位到该题。

**根因：** `HomeScreen` 忽略 `questionId`，仅 `onStartQuiz(fileName)`；练习加载后按随机/保存进度选题。

**改动：**
- 导航 `question/{quizId}?targetQuestionId=` 传递目标题 ID
- `PracticePinnedQuestionPipeline` — 确保目标题纳入本轮题单并定位起始 index
- `PracticeViewModel.goToQuestionById` — 同题库已加载时直接跳转
- 抽屉点选后关闭抽屉并携带 `questionId`

---

## 2026-07-05 — AI 问答保存确认 / 文本选择 / 追问续存

**现象：**
1. 保存确认弹窗点击外部等同于「取消保存」
2. AI 问答内容无法长按复制
3. 全屏对话模式再次提问后保存未接续已有结果

**改动：**
- `AiAskSaveConfirmDialog` — 外点/返回仅关闭弹窗；「取消」按钮才放弃保存
- `DeepSeekAskScreen` / `SparkAskScreen` / `BaiduAskScreen` — 弹窗显示时独立 `BackHandler`
- `RichText` + `AiChatBubble` — `selectable` 支持 `SelectionContainer` + `ActionModeTextToolbar`
- `ExamAnalysisSection`（练习页）— AI 区展开后可长按选字复制
- `DeepSeekAskLoadSeedPipeline` + `seedAnalysis` — 进入全屏问答时合并会话内已有解析，追问保存不丢历史

---

## 2026-07-04 — 练习页 AI 问答返回误跳题

**现象：** 未作答题先进入 AI 提问，保存返回后自动跳到其他题。

**根因：** 打开 overlay 路由时 Activity 仍 RESUMED，NavBackStackEntry 的 `ON_PAUSE` 可能滞后；此前答题触发的 `autoAdvance` 延迟任务在 AI 页期间仍可能执行 `nextQuestion()`。

**改动：**
- `PracticeAutoAdvanceController.setScreenActive` — 页面不可见时禁止调度/执行跳题
- `PracticeOverlayNavigationPipeline` + `rememberPracticeOverlayNavigation` — 打开 AI/笔记/解析前记录锚点，`ON_RESUME` 若 index 漂移则 `goToQuestion` 恢复
- 生命周期：`ON_PAUSE`/`ON_RESUME` 改为 `setScreenActive`（移除无意义的 `resume()`）
- 答题卡选题、所有 overlay 导航统一 cancel / 锚点
- 单测 `PracticeOverlayNavigationPipelineTest`
- **排查日志** `PracticeJumpDebugLog`（TAG=`PracticeJump`）— autoAdvance / overlay / lifecycle / index.changed / vm.nextQuestion / vm.goToQuestion / analysis.save / ai.popBack
- **根因修复** AI 返回后 `QuizInitEffect` 重跑 `reloadForFillConfig` → `loadQuestionsForCurrentSource` 在 `restoreFromMap=false` 时重随机题号；现用 `PracticeQuizInitReloadPipeline` 跳过同 key 重载，并用 `PracticeSessionStartIndexPipeline` 在必须重载时保留 `currentIndex`

---

## 2026-07-04 — AI 解析批改后立即显示（全题型）

**现象：** 计算/论述/画图等文本题保存 AI 后提交批改，AI 区不立即显示；切题再回来才出现。判断/单选/填空同类根因。

**根因：**
- 从 AI 页返回时 `ON_RESUME` 仅在 `showResult=true` 才同步（保存 AI 时多为 false）
- 文本题 `revealShowResult` 不落盘，与异步 `saveProgress` 竞态
- `AnalysisManager` 用 stale snapshot 覆盖会话（丢失内存解析 / showResult）
- `SessionAnalysisResolvePipeline` 流式占位优先于已保存会话

**改动：**
- `PracticeScreenAnalysisSyncEffects` — `ON_RESUME` 始终同步；`resultDisplayReady` 时再同步
- `ExamAISyncEffects` — 同上 + `questionId` 初次同步
- `PracticeStateUpdater.revealShowResult` — 立即 `saveProgress`
- `PracticeSessionAnalysisMergePipeline` — 异步载入合并到最新会话
- `SessionAnalysisResolvePipeline` — 会话/列表优先于流式
- 单测 2 个

---

## 2026-07-04 — AI 问答上下文（题型/无答案）+ `\(...\)` 公式渲染

**问题 1：** 进入 AI 问答时 `formatQuestionForCopy` 携带「答案:」；system 锚点含「标准答案」。

**问题 2：** DeepSeek 回复常用 `\(...\)` / `\[...\]` 定界符，RichText 仅识别 `$…$`，截图中公式显示为原文。

**改动：**
- `QuestionAiContextPipeline.formatQuestionForAi` — 含题型+题干+选项，不含答案
- Practice/Exam 导航 AI 改用 `formatQuestionForAi`
- `DeepSeekExamPromptPipeline` — 锚点去掉标准答案；system 改为独立推导
- `RichTextLatexDelimiterPipeline` — `\(...\)` → `$...$`，接入 `prepareRichDisplayText`
- 单测 3 个

---

## 2026-07-04 — AI 解析即时刷新 + 公式 RichText 渲染

**问题 1：** Practice 保存 AI 后批改区不立即显示，需切题才出现（Exam 已有 showResult 同步，Practice 缺失）。

**问题 2：** 答题页 AI 区与 Exam 模块误用 stub `RichText`（纯 Text），公式/单位无法像计算题批改区一样渲染。

**改动：**
- `PracticeScreenAnalysisSyncEffects` — showResult / ON_RESUME 时从 DB 同步；`PracticeSessionAnalysisSyncPipeline`
- `SessionAnalysisResolvePipeline` — 流式/会话/列表多源取非空正文
- `SessionAnalysisInlineDisplayPipeline` — 结构化持久化 → 展示正文
- `ExamAnalysisSection`（app + feature-exam）— 改用 ui-common `RichText`；删除 exam stub RichText
- 单测 2 个

---

## 2026-07-04 — 双击 AI 区域 → DeepSeek 问答全屏 + 历史还原

**问题：** 双击答题页 DeepSeek 解析区进入 `deepseek/` 静态编辑器；路由 `text` 传的是解析正文，导致 `loadSaved()` 无法正确解码多轮历史。

**改动：**
- 全部 `onViewDeepSeek` 导航改为 `deepseek_ask/`（含 `AppNavHost` / `AppNavRoutes`）
- 遗留 `deepseek/` 路由同样渲染 `DeepSeekAskScreen`（向后兼容）
- 双击传 **题干**（`questionCopyText` / `questionTextForAi`），不再传 `analysisText`
- `DeepSeekAskSessionRestorePipeline` — 首问 fallback 到 exam anchor 题干
- `DeepSeekAskViewModel.restoreSession()` — 打开页时从持久化解码为 `chatTurns`
- 单测：`DeepSeekAskSessionRestorePipelineTest`

---

## 2026-07-04 — DeepSeek 问答稳定性（反 Agreement Bias）

**根因：** `DeepSeekChatConfig.SYSTEM_PROMPT` 与 `DeepSeekAskFollowUpPipeline` 默认注入「请修正」追问，temperature 0.7 导致答案随用户质疑漂移。

**改动（复用现有 deepseek/*Pipeline，无新模块）：**
- `DeepSeekExamPromptPipeline` — 铁路考试 system prompt + 五段输出格式 + 题目锚点
- `DeepSeekChatHistoryPipeline` — 保留最近 3 轮；识别纯质疑句
- `DeepSeekAskFollowUpPipeline` — 移除自动修正注入；质疑句走稳定包装
- `DeepSeekMultiTurnMessagesPipeline` — 接入锚点 + 裁剪历史
- `DeepSeekChatConfig` — temperature 0.35 / presence_penalty 0
- `DeepSeekAskViewModel` + `AppNavHost` — 从当前题注入 `DeepSeekExamAnchor`
- 单测：`DeepSeekAskFollowUpPipelineTest` 等 3 个

---

**P2 PracticeBasicComponents（652→删除）：**
- `PracticeCollapsibleText` / `PracticeDialogComponents` / `PracticeInlineBlankContent` / `PracticeTextAnswerContent` / `PracticeStemContent` / `PracticeOptionsList` / `PracticeFillAnswerComponents` / `PracticeQuestionTextStyle` / `PracticeBlankTextPipeline`
- 编译 ✅

**P2 ExcelQuestionParser（602→~95）：**
- `ExcelParserModels` / `ExcelParserCellPipeline` / `ExcelParserFillPipeline` / `ExcelParserTypePipeline` / `ExcelParserRowPipeline` / `ExcelParserImagePipeline`
- 全仓库 >500 行 **清零**
- 编译 ✅

---

## 2026-07-04 — Phase 40：RichText 拆分

**P2 RichText.kt（670→~86）：**
- `RichTextFormulaPipeline` / `RichTextBitmapPipeline` / `RichTextFormulaViews` / `RichTextInlineLayout` / `RichTextBlockView`
- 编译 ✅

---

## 2026-07-04 — Phase 38–39：ExamViewModel + PracticeViewModel 拆分

**P1 ExamViewModel（647→~471）：**
- `ExamViewModelSessionFlows` / `ExamSessionProgressCoordinator` / `ExamReviewSessionCoordinator`
- 编译 ✅

**P1 PracticeViewModel（608→~461）：**
- `PracticeViewModelSessionFlows` / `PracticeReviewSessionCoordinator` / `PracticeQuestionReopenPipeline`
- 门禁：`scripts/check-practice-vm-loc.ps1`
- 规格：`.ai/practice_viewmodel_decomposition.md`
- 编译 ✅

---

## 2026-07-04 — Phase 36–37：NavigationController + ExamScreenContent 拆分

**P0 NavigationController（730→~160）：**
- `NavigationEnvironment` / `NavigationTargetNavigator` / `NavigationMultiRoundIconNav` / `NavigationUnansweredIconNav` / `NavigationSkipSource` / `NavigationSequentialNext`
- 规格：`.ai/navigation_controller_decomposition.md`

**P1 ExamScreenContent（616→404）：**
- `ExamScreenGestureModifier` / `ExamScreenBottomBar` / `ExamScreenEffects` / `ExamSessionExitPipeline`
- 门禁：`scripts/check-exam-screen-loc.ps1`
- 规格：`.ai/exam_screen_decomposition.md`

**测试：** `ExamSessionExitPipelineTest`；编译 ✅

---

## 2026-07-04 — 全仓库 LOC 扫描 + Agent 守门规则

**扫描:** `scripts/check-loc-over-500.ps1` — 7 个文件 >500 行（见 `.ai/loc_audit.md`）

**新增:**
- `.cursor/rules/architecture-guard.mdc` — 每轮对话自动加载架构约束
- `.ai/loc_audit.md` — 超标文件清单与拆分优先级

---

## 2026-07-03 — PracticeScreen Phase 35：拆分 + 防复发门禁

**问题:** `PracticeScreen.kt` 膨胀至 ~1010 行；本地 `DialogsHost` 与 feature-practice 组件未接线。

**拆分:**
- `components/PracticeScreenBottomBar.kt` — 底栏导航/提交
- `components/PracticeScreenQuestionScrollContent.kt` — 题目与解析区
- `components/PracticeScreenHistorySwipeModifier.kt` — 已答历史滑动
- `components/PracticeScreenEffects.kt` — LaunchedEffect / 生命周期
- `components/PracticeScreenOverlays.kt` — Sheet / 对话框 / `PracticeDialogsHost`
- `PracticeSessionExitPipeline` / `PracticeSessionExitConfirmPipeline` — 退出与交卷判定

**防复发:**
- `scripts/check-practice-screen-loc.ps1` — `PracticeScreen.kt` >500 行 fail
- `.ai/practice_screen_decomposition.md` — 职责与 LOC 预算

**测试:** `PracticeSessionExitPipelineTest`；`:app:compileDebugKotlin` ✅

---

## 2026-07-03 — 答题页 Phase 30：多轮全答轮次池出池硬阻断

**问题:** 当前题已有输入后单击 ←/→ 仍跨词条；单源多轮第 1 轮完成后误跳下一词条而非同词条第 2 轮。

**根因:** `maySingleTapExitRoundPool` 与 `tryNavigateWithinRoundPool` 分离，池内仍有 pending 时仍可能 `skipToUnansweredSource`；单源单题轮次池完成后直接跨词条。

**新增:**
- `PracticeFullAnswerRoundIconNavPipeline` — 池内 pending 导航 + 出池前校验
- `PracticeFullAnswerSameSourceRoundAdvancePipeline` — 同词条换轮
- `PracticeFullAnswerNextRoundPoolPipeline` — 相邻轮次号池

**调整:**
- `NavigationController.navigateMultiRoundViaIcon` — 四步决策链；`skipToUnansweredSource` 池内 pending 硬阻断
- 移除单击路径对 `maySingleTapExitRoundPool` 的依赖

**测试:** `PracticeFullAnswerRoundIconNavPipelineTest`

---

## 2026-07-03 — 答题页 Phase 31：轮次池改回同源同轮（修复跨词条误跳）

**日志根因:** `roundPool` 为全局第 1 轮 170+ 题；随机 `step1_inRoundPool` 从 src=36 跳到 src=95。

**修复:**
- `PracticeFullAnswerSourceRoundPoolPipeline` — 单击 step1 池 = **同源 + 同轮**
- `PracticeFullAnswerSourcePendingPipeline` — 跨词条守卫按**整词条** pending
- 移除单击路径 step3 全局相邻轮次池
- `PracticeFullAnswerRoundPoolPipeline` 保留为全局轮次号池（守卫/历史）

**测试:** `PracticeFullAnswerSourceRoundPoolPipelineTest`；更新 `PracticeFullAnswerRoundIconNavPipelineTest`

---

## 2026-07-03 — 答题页 Phase 32：双击强制跨词条 + 有输入 pending 修正

**问题:** 双击 ←/→ 被 `skipSource BLOCKED`；同源各轮均有输入仍被判 pending，step2 循环且单击无法出池。

**修复:**
- `skipToUnansweredSource(forceCrossSource=true)` — 双击绕过同源 pending 守卫
- `PracticeFullAnswerRoundSlotPendingPipeline` — 须全对：有输入未批改不再 pending；批改答错仍 pending

**测试:** `PracticeFullAnswerRoundSlotPendingPipelineTest`

---

## 2026-07-03 — 答题页 Phase 33：未触碰词条单击直接跨词条

**问题:** 词条各轮均未输入时，单击 ←/→ 在 step2 轮次间循环，无法跳到上下词条。

**修复:**
- `PracticeFullAnswerSourceTouchPipeline` — 检测同源是否任一轮有输入
- `navigateMultiRoundViaIcon` step0：未触碰 → 直接 `skipToUnansweredSource`
- 单击跨词条守卫：仅当 **已有输入且仍有 pending** 时阻断

**测试:** `PracticeFullAnswerSourceTouchPipelineTest`

---

## 2026-07-03 — 答题页 Phase 34：有输入退出交卷确认 + 批量批改

**问题:** 仅输入未点提交时退出练习不弹交卷确认、不自动批改。

**修复:**
- `PracticeSessionInputPipeline` — 会话内是否有输入
- `PracticeSubmitFlow.resolve(answered, hasInput)` — 有输入即弹窗
- `PracticeSessionGradePipeline` + `gradeSessionOnSubmit()` — 交卷确认后批量 reveal

**测试:** `PracticeSubmitFlowTest` / `PracticeSessionGradePipelineTest`

---

**问题:** 同源同轮仅 1 题且 pending（有输入未批改）时 step1 误返 `AtLastUnanswered` / `AtFirstUnanswered`，未进入 step2。

**修复:** step1 无法移动时 fall through → step2 同源其他轮 → step4 跨词条；`canNavigate*` 移除误阻断的 `mustStayInRoundPool`。

---

**新增:** `PracticeFullAnswerIconNavDebugLog`（TAG `PracticeIconNav`）

**埋点:** `PracticeViewModel` 单击入口；`NavigationController` strategy / roundPool 快照 / 四步决策链 / skipSource 阻断 / navigateTo index 变更

**文档:** `.ai/practice_session_navigation_spec.md` §3.4

---

## 2026-07-03 — 答题页 Phase 29：底栏箭头职责澄清 + 导航规格文档

**问题:** 多轮/单轮全答底栏 ←/→ 行为混乱；轮次完成误用 `showResult`；单轮题库被当作整库轮次池。

**规格文档:** `.ai/practice_session_navigation_spec.md`（底栏单击/双击 + 横滑历史，含回归清单）

**根因:**
1. 「原子/非原子」策略与产品语义不符 → 改为 **多轮 / 单轮**（`FullAnswerMultiRoundSessionPipeline`）
2. 轮次 pending/完成应看 **是否有输入**（全答）或 **是否答对**（须全对），非 `showResult`
3. 单轮全答应走 **全局未作答题**，不应锁在轮次池

**新增:**
- `FullAnswerMultiRoundSessionPipeline` — 是否含第 2 轮及以上
- `PracticeFullAnswerRoundSlotPendingPipeline` — 轮次槽 pending/完成

**调整:**
- `FullAnswerIconNavigationStrategyPipeline` — `MULTI_ROUND_POOL_FIRST` / `GLOBAL_UNANSWERED_FIRST`
- `PracticeFullAnswerRound*Pipeline` — 统一 slot pending 判定
- `NavigationController` — 多轮：轮次池 → 跨词条；单轮/普通：全局未答；全答双击一律跨词条

**测试:** `FullAnswerMultiRoundSessionPipelineTest` / `PracticeFullAnswerRoundSlotPendingPipelineTest` + 更新策略/跨池测试

---

## 2026-07-03 — 答题页 Phase 28：全答填空底栏箭头轮次池/题库导航

**问题:** 全答多轮填空单击 ←/→ 误弹「已是最后的未答题 / 已是最前的未答题」，题库内仍有大量未作答题。

**根因:**
1. `PracticeFullAnswerRoundPoolPipeline` 按「同源 + 同轮」建池，第 N 轮通常仅一题 → 无法在同轮跨词条跳转
2. 非原子全答顺序导航只查 anchor 前/后，无环绕 → 当前题后无 pending 即误判边界
3. 全答底栏随机/顺序误用练习 `randomPractice`，未读 `fillFullAnswerRandomOrder`

**新增:**
- `PracticeFullAnswerIconNavOrderPipeline` — 全答 vs 练习随机开关
- `PracticeFullAnswerIconNavTargetPipeline` — 池内下一/上一（顺序环绕 / 随机）
- `PracticeFullAnswerIconUnansweredPipeline` — 非原子全答未作答题导航（顺序环绕）

**调整:**
- `PracticeFullAnswerRoundPoolPipeline` / `ExamFullAnswerRoundPoolPipeline` — 轮次池改为**同轮次跨词条**
- `PracticeFullAnswerIconNavigation` — 轮次池/词条池委托 NavTarget + 全答随机
- `NavigationController` — 非原子单击先词条池再未答池；注入 `fullAnswerRandomOrder`
- `PracticeViewModel` / `PracticeNavigationCoordinator` — 传递全答轮次顺序设置

**测试:** `PracticeFullAnswerRoundPoolPipelineTest` / `PracticeFullAnswerIconNavTargetPipelineTest` / `PracticeFullAnswerIconUnansweredPipelineTest`

---

## 2026-07-03 — 答题页 Phase 27：填空 IME 底栏锚定（Box Chrome）

**根因（截图复现）:** Phase 25 在 scroll Column 上使用 `imePadding()`，Column 流式布局中 weighted 子项 IME 内边距会挤压 siblings，导致底栏（← 提交 →）整体上移遮挡题目。

**方案:** Box 层叠 Chrome — 顶栏/底栏 `align` 锚定，`consumeWindowInsets(ime)` 拒绝 IME 推动；scroll 区仅末尾 `QuestionSessionImeScrollSpacer` 增加可滚空间。

**新增:**
- `QuestionSessionChromeLayout.kt` — Box 顶/中/底三层
- `QuestionSessionBottomNavMetrics.kt` / `QuestionSessionChromeInsetsPipeline.kt`
- `QuestionSessionImeScrollSpacer.kt` — 滚动末尾 IME 占位（非 layout padding）

**删除:** `QuestionSessionScrollImePadding.kt`

**调整:** `PracticeScreen` / `ExamScreenContent` → `QuestionSessionChromeLayout`

**测试:** `QuestionSessionChromeInsetsPipelineTest`

---

## 2026-07-03 — 答题页 Phase 25：填空 IME 底栏遮挡 + Cursor 风格答对色

**根因:**
- Phase 22 将 `imePadding()` 加在底栏导航 Column 上，键盘弹出时整行提交/翻题图标上移，叠在题目滚动区上
- 答对容器色 `#E8F5E9` 与错误红对比仍偏弱，未对齐 Cursor diff 新增行绿色

**新增（ui-common/design）:**
- `QuestionSessionScrollImePadding.kt` — IME 仅作用于中间滚动区（替代 Bottom 版）
- `AnswerCorrectHighlightColorPipeline` + `AnswerCorrectHighlightTokens` — Cursor/GitHub diff 新增行绿（`#DFF7DF` / `#1A7F37` 等）

**调整:**
- `QuestionSessionBodyScroll` — 内置 `questionSessionScrollImePadding()`；底栏 Column 不再 imePadding
- `PracticeScreen` / `ExamScreenContent` — 移除底栏 IME 修饰
- `AnswerChoiceCorrectColorTokens` / `SessionReadingSectionTokens` — 答对色委托 `AnswerCorrectHighlightTokens`

**测试:** `AnswerCorrectHighlightColorPipelineTest` + 更新既有颜色管道测试

---

## 2026-07-03 — AI 提问页 Phase 26：Gemini 风格 prompt + IME 修复

**根因:**
- `AiChatInputBar` 手动 `imePadding()` 导致点击输入时整栏上移、遮挡消息区
- 输入区为 OutlinedTextField + 发送 IconButton，未对齐 Gemini 底部 prompt sheet 设计

**参考:** Gemini App prompt bar（2025）— 底部 sheet、顶部分割阴影、圆角输入容器、圆形发送钮；对话中 assistant 全宽平铺

**新增（ui-common）:**
- `AiChatPromptDesignTokens` — sheet/field/send 尺寸 token
- `AiChatSendEnabledPipeline` / `AiChatBubbleLayoutPipeline` — 无状态管道
- `AiChatPromptField` / `AiChatPromptSendButton` / `AiChatPromptSheet` — Gemini 风格输入区

**调整:**
- `AiChatConversationLayout` — `Scaffold.bottomBar` 承载 prompt sheet（系统 IME 与底栏联动，消息区自动收缩）
- `AiChatBubble` / `AiChatTypingBubble` — Gemini 模式：assistant 无气泡全宽；user 圆角 pill
- 占位符 → `问问 AI…`

**测试:** `AiChatSendEnabledPipelineTest` / `AiChatBubbleLayoutPipelineTest`

---

## 2026-07-03 — AI 提问页 Phase 24：DeepSeek 式对话 UI

**需求:** AI 问答界面对齐 DeepSeek App 对话模式（气泡列表 + 底部输入栏 + 多轮追问）。

**新增（ui-common/model）:**
- `AiChatMessage` / `AiChatMessageRole` / `AiChatTurn` — 展示模型

**新增（ui-common/design）:**
- `AiChatTurnFlattenPipeline` — 多轮 → 气泡序列
- `AiChatSingleTurnPipeline` — 单轮问答气泡
- `AiChatBubbleColorPipeline` — 角色 → 容器/文字色
- `AiChatScrollTargetPipeline` — 自动滚到底部索引
- `AiChatSaveGatePipeline` — 返回键保存确认判定

**新增（ui-common/component）:**
- `AiChatBubble` / `AiChatTypingBubble` / `AiChatMessageList` / `AiChatInputBar` / `AiChatConversationLayout`

**新增（app/ai）:**
- `DeepSeekAskChatTurnMapPipeline` — DeepSeek 多轮 → ui-common 模型
- `AiAskFontMenu` / `AiAskSaveConfirmDialog` — 共用顶栏字号与保存对话框

**调整:**
- `DeepSeekAskViewModel` — 暴露 `chatTurns` / `errorMessage`；失败走 error 气泡而非拼接 `displayText`
- `DeepSeekAskScreen` / `SparkAskScreen` / `BaiduAskScreen` — 改用 `AiChatConversationLayout`（DeepSeek 完整多轮；Spark/Baidu 单轮气泡）
- `SparkAskViewModel` / `BaiduAskViewModel` — `restoreSaved()` 恢复历史展示

**测试:** `AiChatTurnFlattenPipelineTest` / `AiChatSingleTurnPipelineTest` / `AiChatScrollTargetPipelineTest` / `AiChatSaveGatePipelineTest`

---

## 2026-07-03 — 答题页 Phase 23：延时 0 串题 / 轮次池导航 / 批改卡顿 / 滑动冲突

**根因:**
- 延时为 0 时自动跳题与 `currentQuestionUi` 快照 index 错位，选项态串到下一题
- 练习全答原子模式误用**词条池**而非**轮次池**导航，未作答轮次单击越池
- 批改区与跳题同帧重组导致卡顿
- 纵向滚动时水平位移累计触发历史滑动

**新增:**
- `PracticeQuestionUiResolvePipeline` — index 对齐的 UI 态解析
- `PracticeFullAnswerRound*Pipeline` — 轮次池导航/跨池判定（对齐考试侧）
- `QuestionSessionHistorySwipePipeline` — 水平主导才触发历史滑动
- `rememberPracticeResultDisplayReady` — 批改区晚一帧展示

**调整:**
- `PracticeAutoAdvanceController` / `ExamAutoAdvanceTimer` — delay≤0 时 `yield()` 再跳题
- `NavigationController` — 原子全答单击轮次池内循环；轮次全部作答后才允许单击跨词条
- `PracticeScreen` / `ExamScreenContent` — 方向感知滑动

**测试:** `PracticeQuestionUiResolvePipelineTest` / `PracticeFullAnswerRoundCrossSourcePipelineTest` / `QuestionSessionHistorySwipePipelineTest`

---

## 2026-07-03 — 答题页 Phase 22：填空焦点/批改色/历史记录 sheet

**根因:**
- 键盘弹出时 `adjustResize`/`adjustPan` 使整个题目区上移；焦点还会触发 scroll 区自动滚入视口
- 选项批改色 `tertiaryContainer` 与 `errorContainer` 对比度不足
- `ResultHistorySheet` 沿用 `AppScrollBottomSheet` + `heightIn(400dp)` 仅半屏

**新增（ui-common/design）:**
- `QuestionSessionScrollImePadding.kt` — IME 仅作用于中间滚动区（Phase 25 替代 Bottom 版）
- `AnswerCorrectHighlightColorPipeline.kt` — Cursor diff 新增行答对高亮色

**调整:**
- `AndroidManifest` — `windowSoftInputMode=adjustNothing`，题目区顶锚固定；**滚动区** `imePadding` 避让键盘（底栏固定不 overlay 题目）
- `SessionReadingSectionTokens` — 答对色委托 `AnswerCorrectHighlightTokens`（Cursor 新增行绿）
- `AnswerChoiceColorPipeline` — 批改选项改用独立绿/红容器色
- `ResultHistorySheet` — 改用 `AppLazyBottomSheet` + `LazyColumn(fillMaxSize)`

**测试:** `AnswerChoiceCorrectColorPipelineTest` + 更新 `SessionReadingSectionColorPipelineTest`

**验证:** `:app:compileDebugKotlin` + 单元测试

---

## 2026-07-03 — 答题页 Phase 21：滚动/弹层性能 + 收藏状态修复

**根因:**
- 整页 `verticalScroll` 包裹顶栏/底栏/弹层，滑动时全树重组导致卡顿
- `FavoriteViewModel.ensureFullListLoaded()` 未在答题页调用，收藏图标永不变色
- 答题卡 `AnswerCardListDialogShell` 外层 `verticalScroll` + 内层 `LazyColumn` 嵌套滚动，且 `heightIn(520dp)` 仅占用半屏
- 排版设置走 `AppScrollBottomSheet` 对小内容多余

**新增（ui-common/design）:**
- `QuestionSessionBodyScroll.kt` — 中间可滚动区（顶栏/底栏固定）
- `AppLazyBottomSheet.kt` — LazyColumn 专用底 sheet（无外层 scroll）
- `AppStaticBottomSheet.kt` — 小内容底 sheet

**新增（core/util）:**
- `FavoriteSessionPipeline.kt` — 无状态收藏判定

**调整:**
- `PracticeScreen` / `ExamScreenContent` — 拆分 scroll 区域；弹层移出 scroll 树；答题卡数据按需构建
- `ExamScreen` / `PracticeScreen` — 进入时 `ensureFullListLoaded()`
- `PracticeExamTopBar` — 收藏星标 `primary` 着色
- `AnswerCardListDialogShell` — 改用 `AppLazyBottomSheet`，全高 92%
- `QuestionTypographySheet` — 改用 `AppStaticBottomSheet`

**测试:** `FavoriteSessionPipelineTest`

**验证:** `:app:compileDebugKotlin` + 单元测试

---

## 2026-06-28 — 答题页 Phase 20：原子全答箭头单击/双击职责回归

**根因:** Phase 18 将「轮次池内跳转」与「跨词条」对调后，未区分原子/非原子全答，导致原子题库单击直接跨词条。

**新增（core/util）:**
- `AtomicFullAnswerSessionPipeline.kt` — 会话含衍生轮次题（负 id + round）
- `FullAnswerIconNavigationStrategyPipeline.kt` — `ATOMIC_ROUND_POOL_FIRST` vs `NON_ATOMIC_UNANSWERED_FIRST`

**策略:**
| 类型 | 单击 | 双击 |
|------|------|------|
| 原子全答 | 本轮轮次池内 | 跨词条 |
| 非原子全答 | 全局未答 → 跨词条 | 轮次池内 |

**调整:**
- `NavigationController` / `ExamNavigationCoordinator` — 按 strategy 分支 icon 单击/双击
- `PracticeIconUnansweredNavigationPipeline` / `ExamIconUnansweredNavigationPipeline` — 委托 core strategy

**测试:** `FullAnswerIconNavigationStrategyPipelineTest` + 更新 `PracticeIconUnansweredNavigationPipelineTest`

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 答题页 Phase 19：笔记/AI 全屏页系统栏 inset

**根因:** `MainActivity.enableEdgeToEdge()` 后，笔记/AI 全屏页 `Box(fillMaxSize)` 未处理 `WindowInsets`，右上角菜单被状态栏遮挡。

**新增:**
- `ArtifactFullscreenShell.kt` — 委托 `ScreenSafeScaffold`（`WindowInsets.safeDrawing`）+ 顶栏/底栏 action 槽

**接入（8 页）:**
- `NoteScreen` / `DeepSeekScreen` / `SparkScreen` / `BaiduScreen` / `ExplanationScreen`
- `DeepSeekAskScreen` / `SparkAskScreen` / `BaiduAskScreen`

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 答题页 Phase 18：操作行对齐 + 护眼字色 + 箭头单击未答

**操作行（复制 / 重答）：**
- `QuestionSessionActionRow.kt` — 三栏：左「重答该题」/ 中复制 / 右「重答错题」
- `QuestionCopyActionRow.kt` — 薄封装委托 `QuestionSessionActionRow`
- `PracticeResultSection.kt` — 移除独立重答按钮行
- `PracticeScreen` / `ExamScreenContent` — 接入统一操作行
- `ExamQuestionRetryPipeline.kt` — 整题重答 / 保留已对空；`ExamViewModel.retryCurrentQuestion`

**护眼字色：**
- `SessionReadingSectionTokens.kt` — 统一暖灰正文色；正误字色降饱和；新增 `incorrectHintLight/Dark`
- `AnswerFeedbackColors.incorrectHintText` — 填空括号提示弃用 `MaterialTheme.error`

**箭头单击未答（非原子/全答库回归）：**
- `PracticeIconUnansweredNavigationPipeline.kt` — 单击：全局 pending → 跨词条 fallback
- `ExamIconUnansweredNavigationPipeline.kt` — 单击：全局未答 → `skipToAdjacentSource`
- `NavigationController` — 轮次池跳转移至 `*ViaIconDoubleClick`
- `ExamNavigationCoordinator` — 新增 `prev/nextQuestionViaIcon` + 双击轮次池

**测试:** `PracticeIconUnansweredNavigationPipelineTest`

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 答题页 Phase 17：正误结果区可折叠

**新增（ui-common/design）:**
- `ReadingCollapsibleSection.kt` — 会话阅读区共用折叠壳：点击切换展开/收起，`resetKey` 换题重置为收起，展开态底部 `KeyboardArrowUp` 折叠
- `AnswerResultPreviewPipeline.kt` — 无状态单行预览：`resolveAnswerResultPreviewLine`（空白归一 + 96 字截断）

**新增（feature-exam）:**
- `ExamAnswerResultSummaryPipeline.kt` — 无状态：`resolveExamAnswerResultWrongToken`（Composable 侧再套 `answer_wrong_format`）

**调整:**
- `PracticeResultSection.kt` — 正误结果区委托 `ReadingCollapsibleSection`；收起=预览行，展开=完整 `FillAnswerResultText` / `RichText` / `TextResponseAnswerContent`
- `AnswerResultRow.kt` — 同上；正文提取 `ExamAnswerResultBody`

**测试:** `AnswerResultPreviewPipelineTest`

**验证:** 全模块 `compileDebugKotlin` + 单元测试

---

## 2026-06-28 — 答题页 Phase 16：顶栏 SpaceEvenly + 会话区护眼色

**顶栏:**
- `PracticeExamTopBarShell` — `SpaceEvenly` 六槽均分整行：`← ✨ 计时 ★ 📝 ⋮`
- `PracticeExamTopBar` — 扁平 6 槽布局

**会话阅读色（护眼 + 区块区分）:**
- `SessionReadingSectionTokens.kt` — 暖色低饱和 container/content 常量（浅/深）
- `SessionReadingSectionColorPipeline.kt` — 解析/笔记/DeepSeek/Spark/Baidu 分区色
- `SessionReadingAnswerFeedbackPipeline.kt` — 正误结果区 muted 绿/陶土红
- `AnalysisSectionColorPipeline` / `AnswerFeedbackColorPipeline` — 委托会话阅读管道

**区块间距:**
- `ExamAnalysisSection` / 考试 `ExamAnalysisSection` / `PracticeResultSection` / `AnswerResultRow` — `AppSpacing.xs` 垂直间距 + `AppSpacing.sm` 内边距

**测试:** `SessionReadingSectionColorPipelineTest`

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 答题顶栏 Phase 15：紧凑高度 + 计时居中 + AI 左邻

**新增:**
- `PracticeExamTopBarMetrics.kt` — 顶栏 48dp / IconButton 36dp 常量
- `PracticeExamTopBarShell.kt` — 三栏布局：左区（退出 + AI 贴计时左侧）/ 居中计时 / 右区 actions

**调整:**
- `PracticeExamTopBar` — 弃用 `CenterAlignedTopAppBar`；AI 移入 `leadingOfTimer`
- `AppTopBarIconButton` — 可选 `size` 参数（答题顶栏 36dp）

**布局:** `[←] … [✨][05:32] [★][📝][⋮]`（计时屏幕正中，AI 紧贴其左）

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 答题顶栏 Phase 14：计时器专用 + 笔记外置

**顶栏 title:**
- `PracticeExamSessionTitlePipeline` → `PracticeExamTimerPipeline` — 仅格式化 `MM:SS` 计时
- 移除题型 · 进度/总题（由 `QuestionSessionHeader` / `ExamHeader` 承担）

**PracticeExamTopBar 菜单精简:**
- 移除三点菜单「共几题 / 题目列表」
- 笔记 `StickyNote2` 移至顶栏 actions（★ 与 AI 之间）；有笔记时 primary 高亮
- 三点菜单保留：排版设置、编辑题目

**测试:** `PracticeExamTimerPipelineTest` 替代 `PracticeExamSessionTitlePipelineTest`

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — UX 三方向审查 Phase 13：护眼 surface + 顶栏紧凑 + 抽屉规范

**护眼阅读（主题）:**
- `Color.kt` — `ReadingSurface` (#FBF8F2) / `ReadingOnSurface` (#1A1A1A)；浅色 `surface` 改暖米白
- `Theme.kt` — Card/顶栏自动跟随护眼 surface

**答题顶栏（方案 C + CenterAligned）:**
- `PracticeExamSessionTitlePipeline.kt` — 无状态：`题型 · 进度 · 计时` 紧凑 title
- `AppCenterAlignedTopBar.kt` — 居中 TopBar + 左侧退出
- `PracticeExamTopBar` — 委托居中顶栏；新增 `onRequestExit` / 会话 title 参数
- `PracticeScreen` / `ExamScreenContent` — 提取 `requestSessionExit`；BackHandler 与顶栏共用

**主页抽屉:**
- `HomeNavigationDrawer` — 恢复 M3 默认 scrim 动画；删除手动遮罩
- 删除 `HomeDrawerContentArea.kt`
- `QuestionBankDrawerWidthPipeline.kt` — `min(85%屏宽, 屏宽-56dp, 320dp)`
- `QuestionBankDrawerHeader.kt` — 56dp Row 替代嵌套 `AppTopBar`
- `QuestionBankDrawerRows` — 搜索框 56dp；题目行 supporting 改「第 N 题」

**测试:** `PracticeExamSessionTitlePipelineTest` / `QuestionBankDrawerWidthPipelineTest`

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 设计语言 Phase 12：AI 子菜单图标 + 设置步进器 a11y 管道

**新增（ui-common/design）:**
- `PracticeExamAiMenuAction.kt` — AI 下拉动作枚举（DeepSeek / SparkAsk）
- `PracticeExamAiMenuIconPipeline.kt` — 无状态：`iconForPracticeExamAiMenuAction()` → Psychology / Forum
- `PracticeExamAiDropdown.kt` — AI 解析下拉（两项均带 leadingIcon）

**新增（app/settings/ui）:**
- `SettingsStepperAccessibilityPipeline.kt` — 无状态：步进器 label ↔ contentDescription 对齐
- `SettingsStepperAccessibilityPipelineTest.kt` — 管道单元测试

**迁移:**
- `PracticeExamTopBar` — AI 菜单委托 `PracticeExamAiDropdown`
- `SettingsStepperRow` — 必填 `contentDescription` → `CapsuleStepperInput`
- `SettingsScoreRangeStepperRow` — `minContentDescription` / `maxContentDescription`
- `SettingsAnswerSettingsCard` / `SettingsMemoryCardSection` / `SettingsFillPanel` — 调用管道生成 a11y 文案
- `strings.xml` — `settings_stepper_score_min` / `settings_stepper_score_max`

**设计语言统一计划:** Phase 1–12 全部完成；可选后续项已闭合。

**验证:** 全模块 `compileDebugKotlin` + `SettingsStepperAccessibilityPipelineTest`

---

## 2026-06-28 — 设计语言 Phase 11：首页空态 + Sheet 列表高度 + 步进器 a11y（计划收尾）

**新增（app/home）:**
- `HomeLibraryEmptyPipeline.kt` — 无状态：`resolveHomeLibraryEmpty()` → `NO_QUIZ_FILES` / `ROOT_EMPTY_WITH_FOLDERS`
- `HomeEmptyLibraryPanel.kt` — 根目录无题库时 `AppEmptyState` 引导（导入 / 进文件夹）

**新增（ui-common/design）:**
- `AppScrollBottomSheetDefaults.kt` — Sheet 内 `LazyColumn` 统一 `listMaxHeight = 400.dp`

**迁移:**
- `HomeScreen` — `homeContentReady` 且根目录无文件/文件夹时展示 `HomeEmptyLibraryPanel`
- `ResultHistorySheet` — 历史列表 `heightIn(max = AppScrollBottomSheetDefaults.listMaxHeight)`
- `CapsuleStepperInput` — 可选 `contentDescription` + `semantics`
- `QuestionTypographyStepperRow` — 步进器传入 label 作 contentDescription
- `strings.xml` — `home_empty_no_quiz` / `home_empty_root`

**设计语言统一计划:** Phase 1–11 全部完成；分析报告 83→90 缺口已闭合。`Color.kt`/`Theme.kt` 为应用级主题令牌；`PracticeExamTopBar` AI 子菜单图标为后续可选优化。

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 答题页三点菜单重构（方案 C）

**问题:** MoreVert 9 项混杂、图标不一致、排版操作点击成本高。

**新增（ui-common/design）:**
- `QuestionTypographyStepPipeline.kt` — 无状态：字体/行距/字间距 step ↔ 值转换
- `QuestionTypographyStepperRow.kt` — 排版 Sheet 单行步进器
- `QuestionTypographySheet.kt` — `AppScrollBottomSheet` 排版设置（字体/行距/字间距）

**PracticeExamTopBar 重构:**
- 移除 `settingsMenuContent` 插槽；菜单固定 4 项（全 leadingIcon）
- 导航：题目列表 + 笔记（StickyNote2）
- `HorizontalDivider` 分隔
- 功能：排版设置…（FormatSize）+ 编辑题目（Edit）

**迁移:**
- `PracticeScreen` / `ExamScreenContent` — 排版改 BottomSheet；删除 `ExamFontSettingsMenu`
- `PracticeFontController` / `ExamFontController` — 新增 `applyFontSize/LineSpacing/LetterSpacing`
- `ExamDialogState` — `showTypographySheet`

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 练习/考试答题界面 UI 对齐

**新增（ui-common/design）:**
- `QuestionSessionHeader.kt` — 进度条 + 题型/进度/答题卡入口 Card（练习/考试共用）
- `QuestionCopyActionRow.kt` — 题目复制 IconButton 行
- `AppTopBarIconButton.kt` — 40dp 紧凑顶栏 IconButton

**布局对齐:**
- `PracticeScreen` — 拆分为 Header Card + 题目 AppCard + 复制行（与考试一致）
- `ExamScreenContent` — 复制行改 `QuestionCopyActionRow`
- `ExamHeader` — 薄封装委托 `QuestionSessionHeader`

**顶栏/Header 融合:**
- `QuestionCardHeaderRow` — 答题卡入口改为 36dp 列表 IconButton（替代宽 TextButton）
- `PracticeExamTopBar` — actions 改 `AppTopBarIconButton`

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 设计语言 Phase 10：选项高亮色管道 + 会话空态分流

**新增管道（ui-common/design）:**
- `AnswerChoiceTonePipeline.kt` — 无状态 `resolveAnswerChoiceTone(showResult, isSelected, isCorrectOption)`
- `AnswerChoiceColorPipeline.kt` — `answerChoicePalette()` → surface/secondaryContainer/tertiaryContainer/errorContainer

**迁移:**
- `PracticeBasicComponents` / `ExamOptionsList` — 选项行背景色改走管道（移除 alpha 硬编码）
- `AnswerCardCell.answerCardStatusColors()` — 正误/已选改走同一 palette（未答保持 Transparent）
- `PracticeScreen` / `ExamScreenContent` — 加载中 `AppLoadingContent`；已加载无题 `AppEmptyState`

**设计语言统一计划:** Phase 1–11 收尾；剩余 `Color.kt`/`Theme.kt` 为应用级主题令牌，非 UI 散落硬编码。

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 设计语言 Phase 9：内联填空编辑态语义色管道

**新增:**
- `InlineBlankEditColorPipeline.kt` — `inlineBlankEditColors()` → 填空编辑下划线文字色 + 自定义光标色（`primary`）

**迁移:**
- 删除 `InlineBlankTokenizer` 硬编码 `EditingBlue`
- `InlineBlankVisualTransformation` / `buildInlineBlankTransformedText` — 增加 `blankEditColor` 参数（无状态数据流）
- `PracticeBasicComponents` / `ExamBasicRenderers` — 编辑态走管道

**验证:** `:ui-common:compileDebugKotlin`、`:feature-practice:compileDebugKotlin`、`:feature-exam:compileDebugKotlin`

---

## 2026-06-28 — 设计语言 Phase 8：遗留空状态 + 设置 FilterChip 统一

**扩展 `AppEmptyState.kt`:**
- `appEmptyStateTextStyle(compact)` — 共享空状态文案样式管道
- `AppEmptyStateInline` — 列表/Sheet 内嵌空状态（`bodyMedium` + `onSurfaceVariant`）

**设置交互:**
- `SettingsFillTagClearChip.kt` — 标签清除 `FilterChip`（替代 `AssistChip`，与同区标签 Chip 一致）
- `SettingsFillPanel` — 委托 `SettingsFillTagClearChip`

**遗留页空状态迁移:**
- `QuestionScreen` — 详情/浏览模式 → `AppEmptyState` + `R.string.no_questions`
- `HistoryScreen` — → `AppEmptyState`
- `SettingsExportBottomSheet` / `QuizFileBrowser` / `ResultHistorySheet` — → `AppEmptyStateInline`

**验证:** `:ui-common:compileDebugKotlin`、`:app:compileDebugKotlin`

---

## 2026-06-28 — 设计语言 Phase 7：空状态 + 文件卡主题色 + 首页 Sheet

**新增（ui-common / app）:**
- `AppEmptyState.kt` — 居中空状态文案（`onSurfaceVariant` + `bodyLarge`）
- `FileCardTonePipeline.kt` — 无状态：题型/混合/占位 → `FileCardTone`
- `FileCardColorPipeline.kt` — `fileCardPalette(tone)` → M3 container/outline 色
- `HomeStartQuizSheet.kt` — 首页「开始练习/考试」BottomSheet（AppSpacing，无固定 44dp 高度）

**迁移:**
- `OptimizedFileCard` — 移除 20+ 处 hex 调色板，改走 Tone + Color 管道（支持深色模式）
- `ScopedQuestionLibraryScreen` / `LibraryQuestionDetailScreen` / `ResultHistorySheet` → `AppEmptyState`
- `HomeActionOverlays` — Sheet 委托 `HomeStartQuizSheet`；保留 `persistHomeSelection` 于回调层

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 设计语言 Phase 6：答题反馈 + 文件统计颜色管道

**新增管道（ui-common / app）:**
- `AnswerFeedbackColorPipeline.kt` — `answerFeedbackColors()`：结果区背景、正误文字、填空字段背景
- `FileStatColorPipeline.kt` — `fileStatPalette()`：首页文件卡统计色（primary/error/secondary/tertiary）

**迁移（移除硬编码）:**
- `AnswerResultRow` / `PracticeResultSection` — `0xFFD0E8FF` → `primaryContainer`
- `PracticeBasicComponents` — 填空正误背景、FillAnswerResultText、`buildResultQuestionAnnotatedString` 走管道
- `ExamBasicRenderers` — 内联填空结果渲染走管道；删除 `CorrectGreen`
- `InlineBlankTokenizer` — `appendResultBlankText` 增加 `correctColor` 参数
- `OptimizedFileCard` — `Color.Red` / `Color.Cyan` → 主题色
- `RichText` — 公式溢出警告色 → `tertiary`

**布局:**
- `ExamScreenContent` — `ExamQuestionBody` 外包 `AppCard`（与练习页题目 Card 对齐）

**验证:** 全模块 `compileDebugKotlin`

---

## 2026-06-28 — 全应用设计语言统一（Design System Phase 1–2）

**目标:** 按审查报告 Top 30 建立共享设计原语并迁移关键页面；原则：文件单一功能、短小、无状态、单一数据流、面向管道。

**新增 `ui-common/design/`（共享原语）:**
- `AppSpacing.kt` — xs/sm/md/lg/xl 间距常量
- `AppCard.kt` — ElevatedCard 12dp 圆角、1dp elevation
- `AppTopBar.kt` — 标准 M3 TopAppBar（可选返回 + actions）
- `AppContentText.kt` — 正文（LocalFontSize/LocalFontFamily）
- `AppScrollBottomSheet.kt` — 可滚动 ModalBottomSheet 壳

**页面迁移（P1–P20 优先项）:**
- `SettingsTopBar` / `SettingsCardGroup` — 委托 `AppTopBar` / `AppCard`
- `ResultScreen` — AppTopBar + AppCard + AppSpacing；固定 Typography；M3 默认 Button
- `WrongBookScreen` / `FavoriteScreen` — 详情页 `LibraryQuestionDetailScreen`（TopAppBar + Card 列表 + bottomBar）
- `ScopedQuestionLibraryScreen` — 根/文件夹视图统一 AppTopBar
- `QuestionBankDrawer` — 自定义 Row 标题 → AppTopBar（关闭 action）
- `PracticeScreen` — 题目区 AppCard；题型/进度 bodyMedium；解析 Dialog → AppScrollBottomSheet
- `AnswerCardListDialogShell` — Dialog → AppScrollBottomSheet（练习/考试答题卡）
- `FillSettingsScreen` — 帮助文本折叠（`showDetailedHelp` 开关）；bottom padding 16dp

**验证:** `:ui-common:compileDebugKotlin`、`:app:compileDebugKotlin`

---

## 2026-06-28 — 设计语言统一 Phase 3（P21–P30 收尾）

**目标:** 完成审查报告剩余项：历史记录 BottomSheet、Loading 抽取、TopBar 统一、间距收尾。

**新增/扩展:**
- `AppTopBar` — 支持 `scrollBehavior` + 自定义 `navigation`（Home 抽屉菜单）
- `AppLoadingOverlay.kt` — 全屏遮罩 + AppCard 内容区
- `PracticeExamTopBar.kt` — 练习/考试共用顶栏（AppTopBar + AssistChip 题数）
- `ResultHistoryLinePipeline.kt` — 无状态历史行格式化（修正正确率 ×100）
- `ResultHistorySheet.kt` — 历史记录 ModalBottomSheet

**迁移/删除:**
- `ResultScreen` — 内嵌 LazyColumn →「查看历史记录」+ BottomSheet
- `HomeTopBar` — 委托 AppTopBar（固定 titleLarge）
- `PracticeScreen` / `ExamScreenContent` — `PracticeExamTopBar` 替代重复 `ExamTopBar`
- 删除 `app/.../ExamTopBar.kt`、`feature-exam/.../ExamTopBar.kt`
- `SettingsImportProgressOverlay` — 薄封装 `AppLoadingOverlay`
- `PracticeScreen` — Spacer 统一 AppSpacing

**验证:** `:app:compileDebugKotlin`、`:feature-exam:compileDebugKotlin`、`ResultHistoryLinePipelineTest`

---

## 2026-06-28 — 设计语言 Phase 4：ExamHeader + 全局 Loading

**ExamHeader（考试页）:**
- `ExamHeader.kt` — 外包 `AppCard`；题型/进度 `bodyMedium`；支持 `extraContent` 槽（全答轮次标签）
- `ExamScreenContent` — 轮次标签移入 Header Card；页面 padding 统一 `AppSpacing.md`

**全局 Loading 复用:**
- `AppLoadingIndicator.kt` — 40dp 主色 spinner + `AppLoadingContent`（居中 + 可选文案）
- `HomeActionOverlays` — 自定义 Box → `AppLoadingOverlay`
- `PracticeScreen` / `ExamScreenContent` — 加载态 → `AppLoadingContent`
- `SettingsImportProgressOverlay` / `QuestionEditDialog` / `PracticeChatGptDialog` / 题库抽屉 — `AppLoadingIndicator`

**验证:** `:ui-common:compileDebugKotlin`、`:app:compileDebugKotlin`、`:feature-exam:compileDebugKotlin`、`:feature-practice:compileDebugKotlin`

---

## 2026-06-28 — 设计语言 Phase 5：M3 合规 + 深色模式颜色管道

**P0 — 硬编码颜色:**
- `AnalysisSectionTone` + `AnalysisSectionColorPipeline` — 解析/笔记/AI 区块主题色
- `ResultStatColorPipeline` — 结果页统计/折线图轴色
- 练习/考试全部分析区块改走管道（移除 9 处硬编码）

**P0 — TopAppBar M3:**
- `PracticeExamTopBar` — actions 仅 Star + AI + MoreVert；题数/笔记移入 MoreVert
- `QuestionCardHeaderRow` — 题目 Card 顶行答题卡入口

**P1 — 设置/Card 打磨:**
- 题目 AppCard 进度条顶置全宽；记忆模式 Switch 标签区分；Section/Card/Slider 间距；导入 Sheet 图标；导出 Sheet 最大高度

**P2:** `AppScrollBottomSheet` 85% 高度上限

**验证:** 全模块 `compileDebugKotlin` 通过

---

## 2026-06-28 — 设置页遗留清理 + 导入 Snackbar 管道

**目标:** 删除 M3 重构后无引用的旧 UI；Side effect 移出 Composable，管道化导入反馈。

**删除（零引用遗留）:**
- `SettingsFontSection` / `SettingsExamPanel` / `SettingsPracticePanel` / `SettingsBasicPanel`
- `SettingsSoundDarkPanel` / `SettingsImportExportPanel` / `ExportSourceSelectionDialog`
- `SettingsLoadingOverlay` / `SettingsMemoryPanel` / `SettingsImportSection` / `SettingsExportSection`
- `SettingsFillPanel` 废弃 wrapper

**新增/迁移（单一职责）:**
- `SettingsImportProgressOverlay.kt` — 自 `SettingsImportSection` 拆出
- `SettingsImportSnackbarPipeline.kt` — 无状态 `resolveImportSnackbarResult` + `ImportSnackbarMessages`
- `SettingsImportSnackbarMessages.kt` — Context → 文案映射（UI 边界）
- `SettingsExportRequestPipeline.buildExportOutputName` — 导出文件名格式化

**验证:** `:app:compileDebugKotlin`、`SettingsImportSnackbarPipelineTest`、`SettingsExportRequestPipelineTest`

---

## 2026-06-28 — 设置页重构续：记忆模式 + 主题色 + 导出管道

**补全项（审查报告 P11/P12 及 UX 优化）:**
- `SettingsMemoryCardSection` — 记忆模式接入答题 Card；SegmentedButton 替代 RadioButton；`setMemoryMode` 同步练习/考试
- `Color.kt` / `Theme.kt` — 浅色 background/surface/surfaceVariant/primary 按 M3 推荐值
- `SettingsExportRequestPipeline` — 单文件导出跳过 BottomSheet

**验证:** `:app:compileDebugKotlin`、`SettingsExportRequestPipelineTest`

---

## 2026-06-28 — 设置页 M3 UI/UX 全面重构

**目标:** 按 UI/UX 审查报告 Top20 项落地：TopAppBar、ElevatedCard 分组、ListItem 标准行、SegmentedButton、Slider 延迟/字号、填空题独立子页、数据管理 Card + BottomSheet。

**新增（settings/ui/ 单一职责 / 无状态 / 管道）:**
- `SettingsSectionHeader` / `SettingsCardGroup` / `SettingsUserText` — 视觉分组与统一二级文本
- `SettingsListSwitchRow` / `SettingsListSliderRow` / `SettingsSegmentedChoiceRow` — M3 标准设置行
- `SettingsExpandableCardSection` — AnimatedVisibility 展开/折叠
- `SettingsAppearanceCard` / `SettingsAnswerSettingsCard` / `SettingsDataGroupCard` — 外观/答题/数据 Card
- `SettingsBottomSheets` — 导出选文件 + 导入题库入口 ModalBottomSheet
- `SettingsTopBar` — 固定 typography 的 TopAppBar
- `FillSettingsScreen` — 填空题详细设置子页（路由 `settings/fill`）
- `SettingsStepperDisplay.kt` — 公共 `formatCountStepperDisplay`

**修改:**
- `SettingsScreen` — Scaffold + 三区 Card 布局；移除 7 按钮平铺；导入题库合并 BottomSheet 入口
- `SettingsFillPanel` — 提取 `SettingsFillPanelContent`；帮助文本统一走 `SettingsHelpText`
- `AppNavHost` — 注册 `settings/fill` 路由
- `strings.xml` — 区域标题、短标签、BottomSheet 文案

**验证:** `:app:compileDebugKotlin`

---

## 2026-06-28 — 分值范围双步进器等宽修复

**问题:** `SettingsScoreRangeStepperRow` 左右胶囊步进器宽度不一致，右侧被父级 `Row` 挤压缩小。

**修改:**
- `CapsuleStepperDefaults.kt` — 统一 `WIDTH_DP=130` / `HEIGHT=40.dp`
- `CapsuleStepperInput` — `.width()` 改 `.requiredWidth()`，防止父布局压缩
- `SettingsScoreRangeStepperRow` — 标签上置、步进器行右对齐；两侧均用 `CapsuleStepperDefaults.WIDTH_DP`

**验证:** `:ui-common:compileDebugKotlin`、`:app:compileDebugKotlin`

---

## 2026-06-28 — 设置页分值范围双胶囊步进器

**问题:** Fill 设置中「答案级别分值范围」仍使用 `RangeSlider`。

**修改（单一功能 / 无状态 / 管道）:**
- `StepperScoreRangePipeline.kt` — `normalize` / `withMin` / `withMax`，保证 min≤max 且 1..10
- `SettingsScoreRangeStepperRow.kt` — 标签 + `min ~ max` 双 `CapsuleStepperInput`
- `SettingsFillPanel` — 移除 `RangeSlider`，改挂 `SettingsScoreRangeStepperRow`

**验证:** `:app:compileDebugKotlin`、`StepperScoreRangePipelineTest`

---

## 2026-06-28 — 设置页胶囊步进器（M3 一体化输入）

**目标:** 设置页数值调节由 Slider 改为 M3 胶囊步进器 + 文本输入，含位移动效与编辑态微交互。

**新增（ui-common / 单一职责 / 管道）:**
- `StepperInputParsePipeline.kt` — 数字过滤与 clamp，无状态
- `StepperAnimatedValue.kt` — 加减时上下滑入/淡出
- `CapsuleStepperInput.kt` — 全圆角胶囊、`BasicTextField` 沉浸式输入、编辑态背景动画
- `SettingsStepperRow.kt` — 标签 + 步进器行布局（app settings ui）

**替换 Slider 为步进器:**
- `SettingsFontSection` — 字号、题量（0=∞）、答题延迟
- `SettingsFillPanel` — 填空题量、**答案级别分值范围**（双步进器 `min ~ max`）；RangeSlider 已移除
- `SettingsBasicPanel` / `SettingsPracticePanel` / `SettingsExamPanel` / `SettingsMemoryPanel`

**验证:** `:app:compileDebugKotlin`、`:ui-common:compileDebugKotlin`、`StepperInputParsePipelineTest`

---

## 2026-06-28 — 抽屉遮罩收起（二次）、返回主页流畅性

**问题:**
1. 仅设 `gesturesEnabled=true` 后，点击抽屉外遮罩仍无法收起
2. 从错题库/收藏库/设置/记录页返回主页有卡顿

**根因:**
1. M3 内置 scrim 与主页 `pointerInput`/拖拽层叠，遮罩点击未可靠触发 `close()`
2. 子页 `navigate()` 未 `saveState/restoreState`；pop 回主页时整页重绘 + 转场动画与拖拽/手势 Modifier 同帧挂载

**修改（单一功能 / 无状态 / 管道）:**
- `HomeDrawerContentArea.kt`：抽屉打开时在内容区叠可点击 scrim（`DrawerDefaults.scrimColor`），单一 dismiss 出口
- `HomeNavigationDrawer.kt`：内置 scrim 透明 + `BackHandler`；内容区走 `HomeDrawerContentArea`
- `AppNavHomePipeline.kt`：`navigateFromHome`（save/restore state）+ `popBackToHome`
- `AppNavHost.kt`：主页子路由改走 `AppNavHomePipeline`（`launchSingleTop` + `restoreState`）
- `HomeResumeDeferGate.kt`：首帧后再挂拖拽/长按 Modifier；抽屉打开时跳过

**验证:** `:app:compileDebugKotlin`

---

## 2026-06-28 — 抽屉遮罩收起、答题卡滚动流畅性

**问题:**
1. 主页抽屉展开后，点击抽屉外区域无法收缩
2. 答题页/考试页顶栏答题卡弹窗上下滑动卡顿、掉帧

**根因:**
1. `HomeScreen` 的 `ModalNavigationDrawer` 设 `gesturesEnabled = false`，禁用了遮罩点击收起
2. `AlertDialog` 自带滚动容器 + 内部 `LazyColumn`/`LazyVerticalGrid` 嵌套懒加载，滚动争抢导致掉帧；`answerCardStatusColors()` 与展开箭头动画在每格重复计算

**修改（单一功能 / 无状态 / 管道）:**
- `HomeNavigationDrawer.kt`：薄封装，`gesturesEnabled = true`，遮罩点击走 `drawerState.close()`
- `AnswerCardListDialogShell.kt`：`Dialog` + `Surface` 单一滚动根，替代 `AlertDialog`
- `PracticeQuestionListDialog` / `QuestionListDialog` 改挂 `AnswerCardListDialogShell`
- `AnswerCardGrid.kt`：`LazyVerticalGrid` → 静态 5 列 Row，消除外层 `LazyColumn` 嵌套懒滚
- `AnswerCardCompactEntryGrid.kt`：`contentType` + 顶层 hoist `statusColors`
- `AnswerCardCell.kt`：`answerCardStatusColors()` 加 `remember(scheme)`
- `AnswerCardExpandIndicator.kt`：滚动列表内去掉 `animateFloatAsState`，静态旋转

**验证:** `:ui-common:compileDebugKotlin`、`:app:compileDebugKotlin`、`:feature-exam:compileDebugKotlin`、`:feature-practice:compileDebugKotlin`

---

## 2026-06-28 — 抽屉搜索折叠/状态栏 insets 二次修复

**根因:**
1. 搜索折叠：`toggleFile` 更新了 `_searchCollapsedFiles`，但 Compose 未 `collectAsState`，UI 不重组，点击题库行无收缩效果
2. 状态栏：`statusBarsPadding()` 单独加在 scroll Column 上不可靠；设置页 overlay `Box(fillMaxSize)` 与内容并列，未统一 inset

**修改（单一数据流 / 管道）:**
- `QuestionBankDrawerExpansion.kt`：无状态 snapshot + `isFileExpanded`/`isFolderExpanded` 纯判定
- `QuestionBankDrawerViewModel.expansionSnapshot`：`combine` 五路 StateFlow → 单一出口；Drawer `collectAsState` 驱动重组
- `ScreenSafeScaffold.kt`：`WindowInsets.safeDrawing` + Material3 `Scaffold` 统一安全区
- `PracticeScreen` / `ExamScreenContent` / `SettingsScreen` 改用 `ScreenSafeScaffold`；抽屉 Column 加 `statusBarsPadding()`
- 单元测试 `QuestionBankDrawerExpansionTest`

**验证:** `:app:compileDebugKotlin`、`:feature-exam:compileDebugKotlin`、`QuestionBankDrawerExpansionTest`

---

## 2026-06-28 — 抽屉搜索折叠、状态栏安全区、Markdown 公式修复

**问题:**
1. 主页抽屉搜索模式下，点击题库行无法收缩已展开的搜索结果
2. 答题页（练习/考试）与设置页内容侵入系统状态栏，遮挡顶栏图标
3. 题库解析内容含 `* $8487.05 \text{ N}$**（$866.03 \text{ kgf}$）` 及孤立 ``` 行时 RichText 渲染异常

**修改（按单一功能/无状态/管道原则）:**
- `QuestionBankDrawerViewModel`：搜索模式默认展开，用 `searchCollapsedFiles/Folders` 记录用户手动折叠；`isFileExpanded`/`isFolderExpanded`/`toggleFile`/`toggleFolder` 统一分支
- `QuestionBankDrawer`：展开态改读 ViewModel 判定，不再 `isSearchMode || expanded*` 硬编码
- `ScreenSafeInsets.kt`（ui-common）：`Modifier.screenTopSafePadding()` 单一职责
- `MainActivity`：`enableEdgeToEdge()`；`PracticeScreen`/`ExamScreenContent`/`SettingsScreen` 根布局加 `screenTopSafePadding()`
- `MarkdownFormatNormalizer`：去除 `$...$**` 孤立粗体标记；过滤孤立 code fence 行
- `MarkdownFormatNormalizerTest`：新增 N/kgf 场景用例

**说明:** logcat 中 `Finsky ... Failed getting com.example.testapp` 来自 Google Play 商店查询 sideload 包，与应用代码无关，可忽略。

**验证:** `:app:testDebugUnitTest`（MarkdownFormatNormalizerTest）、`:app:compileDebugKotlin`、`:feature-exam:compileDebugKotlin`

---

## 2026-06-28 — 考试答题页箭头/滑动导航职责分离

**问题:** 考试答题页非原子题库题型（非全答模式）点击左右箭头图标会浏览历史（查看已答题目），而非仅跳转未作答题；同时滑动屏幕也跳转未作答题，与箭头功能重叠。

**根因:**
- `ExamNavigationCoordinator.prevQuestion()` 在无未答题导航目标时回退到 `currentIndex - 1`，导致箭头点击可浏览已答历史
- `canNavigateToPrevUnanswered()` 含 `|| (!randomExamEnabled() && state.currentIndex > 0)` 旁路，使箭头始终启用以触发回退
- `ExamScreenContent` 滑动处理器调用 `prevQuestion()`/`nextQuestion()`，与箭头功能重复

**修改（按单一功能/无状态/管道原则）:**
- `ExamNavigationCoordinator`：移除 `prevQuestion()` 的 `currentIndex - 1` 回退；移除 `canNavigateToPrevUnanswered()` 的 `currentIndex > 0` 旁路；新增 `prevQuestionSequential()` / `nextQuestionSequential()` / `canGoPrevSequential()` / `canGoNextSequential()` — 纯顺序浏览供滑动使用
- `ExamViewModel`：暴露 4 个顺序导航方法转发（单行委托，无状态）
- `ExamScreenContent`：非复盘模式滑动改用 `prevQuestionSequential()` / `nextQuestionSequential()`，末尾左滑仍走 `ExamEdgeSwipePipeline`；箭头保持 `prevQuestion()` / `nextQuestion()`（现仅未答题导航）

**验证:** `:feature-exam:compileDebugKotlin` `:app:compileDebugKotlin`

---

## 2026-06-28 — 考试非原子题库题型箭头变灰修复

**问题:** 考试答题页非原子题库题型（单选/多选/判断等）左右箭头图标均变灰。

**根因:**
- `ExamNavigationCoordinator.fullAnswerModeActive` 与 UI 的 `isFullAnswerMode` 判定不一致：前者仅读 `activeFillConfig`，后者虽加了 inline-blank 校验但未回灌协调器；非填空题型下 `mustStayInRoundPool` 误阻断 `canNavigate*`
- `ExamScreenContent` 导航栏 `visible = !showResult` 与 `enabledPrev/Next` 缺少 `|| showResult` 回退，与实践页不一致

**修改:**
- `ExamFullAnswerModeActivePipeline`：无状态统一全答生效判定（FULL_ANSWER + 含 inline-blank）
- `ExamViewModel`：`fullAnswerModeActiveNow()` 单一出口供协调器与 `isFullAnswerMode` 共用
- `ExamScreenContent` 导航栏：`visible = true`、`onSubmit` 按 `!showResult` 条件显隐、`enabledPrev/Next` 增加 `|| showResult` 回退

**验证:** `:feature-exam:compileDebugKotlin` `:app:compileDebugKotlin`

---
## 2026-06-28 — 答题详情 / 错题库·收藏库入口卡顿优化

**问题:** 练习结束点「答题详情」进入复盘页掉帧；主页底部「错题库」「收藏库」首屏卡顿。

**根因:**
- 复盘：`SessionReviewPresentation.prepare` 主线程处理全量题目；复盘 UI 在 `reviewReady` 前即重组
- 错题库：`getAll()` combine 全表 `questions`；列表页对全量错题做 `groupBy` + `buildFileStatisticsForQuestions`
- 收藏库：init 即 JSON 反序列化全量收藏

**修改:**
- `PracticeReviewReusePipeline` / `PracticeReviewPresentationPipeline`：复用会话 + Default 线程准备复盘
- `reviewReady` Flow + `PracticeScreen` 加载占位
- `ScopedLibraryCatalogPipeline` + DAO 聚合查询：`observeLibraryCatalog()`
- `WrongBookRepositoryImpl.getAll` 改为按错题 ID 批量查题；列表页仅订阅 catalog
- `WrongBookScreen` / `FavoriteScreen`：catalog 驱动列表；详情页 `ensureFullListLoaded()` 按需加载

**验证:** `:data:compileDebugKotlin`、`:feature-practice:compileDebugKotlin`、`:app:compileDebugKotlin`

---

## 2026-06-28 — 原子题库：上下题导航卡顿优化

**问题:** 原子题库（大量衍生填空变体）答题界面单击/滑动切换上下题时掉帧卡顿。

**根因:** 每次 `currentIndex` 变更同步 `saveProgress()`，主线程遍历全量 `questionsWithState` 构建 `questionStateMap`；`PracticeScreen` 订阅完整 `sessionState` 与全列表 Flow，切题触发大范围重组；切题时 `LaunchedEffect(question)` 重复写入已存在的解析字段再次落盘。

**修改:**
- `NavigationSaveScheduler`（core）：导航专用防抖保存（350ms 合并）
- `PracticeProgressMapPipeline`：Default 线程合并 `questionStateMap`
- `PracticeProgressLifecycleCoordinator`：`scheduleNavigationSave` / `flushAndSave` 分流
- `NavigationController` / `ExamNavigationCoordinator`：仅索引变更走防抖保存
- `PracticeCurrentQuestionUiPipeline` + `currentQuestionUi` Flow：当前题窄订阅
- `PracticeScreen`：`key(currentIndex)`、切题滚回顶部、会话计数窄 Flow
- `PracticeStateUpdater`：解析字段无变化跳过写入

**验证:** `:feature-practice:compileDebugKotlin`、`:feature-exam:compileDebugKotlin`、`:app:compileDebugKotlin`

---

## 2026-06-28 — 考试答题详情：滑动与图标对齐练习复盘

**问题:** 交卷后「答题详情」页左右滑动、左右图标与练习答题详情不一致。

**根因:** 复盘滑动误用 `nextQuestion`/`prevQuestion`（displayOrder 步进）；未构建 `reviewAnsweredSwipeOrder`；导航控件与答题态混用。

**修改:**
- `ExamReviewSwipePipeline`：右滑更旧 / 左滑更新（委托 `ReviewAnsweredSwipePipeline`）
- `ExamViewModel`：`reviewAnsweredSwipeOrder` + `browseReviewAnsweredOlder/Newer`；图标仍走 `ReviewBrowseSession`
- `ExamScreenContent`：复盘滑动与 toast；独立 `QuestionNavigationControls`（与练习一致）

**验证:** `:feature-exam:compileDebugKotlin`

---

## 2026-06-28 — 考试全答：单击限轮次池内未作答题（纠正）

**问题:** 第179条「第1轮/共2轮」时，轮次池内仍有未作答题，单击右图标却跳到下一词条。

**根因:** 轮次完成误用 `hasAnswerContent`（已选题未提交即视为完成）；单击导航未按未作答槽位筛选，且池内仍有未作答时未阻断跨词条 fallback。

**修改:**
- `ExamFullAnswerRoundUnansweredPipeline`：`!showResult` 判定未作答；`allSlotsAnsweredInPool`
- `ExamFullAnswerRoundNavigablePipeline`：可导航目标 = 未作答 + 须全对答错
- `ExamFullAnswerIconNavigation`：在 navigable 列表内上下题
- `ExamNavigationCoordinator`：`mustStayInRoundPool` 阻断跨池；随机模式同步

**验证:** `:feature-exam:compileDebugKotlin`

---

## 2026-06-28 — 考试全答：单击限轮次池、双击跳词条

**问题:** 全答+须全对时，如第179条「第1轮/共2轮」，单击右图标跳到下一词条，未在当前轮次池内继续作答直至全对。

**根因:** 考试无轮次池导航；`resolveSequentialNextIndex` 在轮次未完成时落入全局 pending，可跨词条跳到下一轮次题；`sourceId==questionId` 早退致单槽判定错误。

**修改:**
- `ExamFullAnswerRoundPoolPipeline` / `ExamFullAnswerRoundCompletePipeline`：轮次题池索引与完成判定
- `ExamFullAnswerIconNavigation`：单击在轮次池内上下题
- `ExamFullAnswerIconRetryPipeline` / `ExamFullAnswerReopenPipeline`：须全对答错重开
- `ExamNavigationCoordinator`：`tryNavigateWithinRoundPool` 优先于跨词条导航
- `ExamFullAnswerNavigation`：同源 pending 统一判定；未完成轮次禁止全局 fallback

**验证:** `:feature-exam:compileDebugKotlin`

---

## 2026-06-28 — 全答单槽词条答错后右图标误跳下一词条

**问题:** 全答+须全对时，词条仅一题，批改答错后点右图标直接跳到下一词条，未对错题重开直至全对。

**根因:** `sourceId == questionId`（单槽无变体轮次）时 `isCurrentSourceComplete` 恒 true、`sourceIndices` 空、轮次池 size≤1 直接放弃，落入全局未答导航。

**修改:**
- `PracticeAnswerHandler`：单槽与多轮次统一按同源 pending 判定完成/候选
- `PracticeFullAnswerIconNavigation`：单槽未完成时留在当前索引；启用右/左图标
- `PracticeFullAnswerIconRetryPipeline`：须全对且答错时图标导航优先 `reopenQuestionForFullAnswerRetry`
- `NavigationController.navigateUnansweredNext`：同索引也触发错题重开

**验证:** `:feature-practice:compileDebugKotlin`

---

## 2026-06-28 — savedSourcesFullyAnswered：源题全批改后强制新轮次

**问题（日志）:** `priorComplete=false` 但 `answeredSources=[1,2,3]`、`canReuse=true`、`restoreFromMap=true`，重开仍复用 `[1,2,3]` 并恢复作答。

**根因:** `priorComplete` 按变体槽位（`fixedQuestionOrder` 9 项）判定；源题 1–3 已在 map 中批改完成，但 `finished`/全变体 `showResult` 未齐，复用与恢复逻辑仍走「续答」。

**修改:**
- `ExamSourceQuestionPipeline` / `PracticeSourceQuestionPipeline`：`savedSourcesFullyAnswered`
- `startNewRound = priorComplete || savedSourcesDone`（`ExamLoadDelegate` / `PracticeProgressLifecycleCoordinator`）
- `ExamRoundReusePipeline` / `PracticeRoundReusePipeline`：源题全批改后 `canReuse=false`
- `ExamSessionRestorePipeline` / `PracticeSessionRestorePipeline`：同上时不 `restoreFromMap`
- 日志增加 `savedSourcesDone`

**验证:** `:feature-exam:compileDebugKotlin` `:feature-practice:compileDebugKotlin`

---

## 2026-06-28 — 考试/练习多轮出题与作答态误恢复修复 + 排查日志

**问题:** 第一轮答完再开，第二轮仍出相同题目且带上一轮已答/未答状态；练习同样。

**根因:**
- 仅依赖 `finished` 判定新轮次；交卷后 `finished` 未写入时仍 `canReuseSavedOrder=true`
- `shouldRestoreAnswersFromMap` 在 `selectedOptions` 非空时回填累积 map → 新轮次显示旧作答
- 练习 `loadQuestions` 无条件从 `questionStateMap` 初始化题面；`loadProgress` 二次合并

**修改:**
- `ExamRoundCompletePipeline` / `PracticeRoundCompletePipeline`：全题已选且已批改即视为轮次结束
- `ExamRoundReusePipeline` / `PracticeRoundReusePipeline`：轮次结束后禁止复用题序
- `PracticeQuestionOrderPipeline` / `PracticeNewRoundProgressPipeline`：对齐考试新轮次出题
- `PracticeSessionRestorePipeline`：新轮次空白题面；复盘/中途才恢复
- `ExamSessionRestorePipeline`：轮次结束后不回填；复盘 `reviewMode` 例外
- `PracticeProgressLifecycleCoordinator`：单管道加载 + 累积 map 保存（extras）
- `SessionProgressManagerImpl`：练习侧同样支持 extras 注入 map
- 日志：`ExamRoundLoadLog`（tag `ExamRoundLoad`）、`PracticeRoundLoadLog`（tag `PracticeRoundLoad`）

**验证:** `:feature-exam:compileDebugKotlin` `:feature-practice:compileDebugKotlin` `:app:compileDebugKotlin`

---

## 2026-06-28 — 考试多轮出题累积与导航修复

**问题:** 第二轮出题正常，第三轮又回到第一轮题目；第三轮左右导航图标点击无反应。

**根因（日志印证）:** `mapSize=9` 为变体 ID，`orderedIds=[1,2,3]` 为源题 ID；已答判定未做 `extractSourceQuestionId` 归一化，导致第二轮仍抽 [1,2,3]；`restoreFromMap=true` 因 `selectedOptions` 非空。

**补充修复:**
- `ExamSourceQuestionPipeline` / `PracticeSourceQuestionPipeline`：map/fixedOrder → 源题 ID
- `ExamRoundCompletePipeline`：按 `fixedQuestionOrder` 变体在 map 中全部 showResult 判定轮次结束
- `ExamSessionRestorePipeline`：`selectedOptions` 为空（新轮次）不回填
- 日志增加 `answeredSources` / `lastRoundSources`

**问题:** 设置页调节「考试题数」后，答完一轮再开同题库考试仍出上一轮相同题目，未从未作答题池按随机/顺序重新出题。

**根因:** `ExamLoadDelegate` 在 `finished=true` 时仍复用 `fixedQuestionOrder`；题数变更时未校验已保存题序长度；新轮次 `loadProgress` 误从 `questionStateMap` 回填作答。

**修改:**
- `ExamQuestionCountPolicy`：题数截断与「全部」时复用门槛（对齐练习）
- `ExamRoundReusePipeline` / `ExamSavedOrderMatchPipeline`：`finished` 或题数不足不复用
- `ExamQuestionOrderPipeline`：新轮次未答池优先 + 随机/顺序 + `buildNewRoundProgress`
- `ExamSessionRestorePipeline`：仅 `finished` 复盘或进行中有作答时回填 map
- `ExamLoadDelegate`：上一轮结束开新轮（新 seed）；`loadReviewSession` 保留 `preserveFinishedProgress`
- `ExamViewModel.loadProgress`：按恢复管道决定是否合并 `questionStateMap`

**验证:** `:feature-exam:compileDebugKotlin` `:app:compileDebugKotlin`

---

## 2026-06-28 — 考试末题索引与全库 pending 错位修复

**问题:** 答题卡最后一题索引处，题库仍有未作答题时，右箭头变灰、答完即弹交卷确认；应仅在全库仅剩最后一题待答时出现。

**根因:** `ExamScreenContent` 用 `currentIndex < size-1` 判定下一题/交卷，未按全库 pending 状态。

**修改:**
- `ExamPendingQuestionPipeline`：未作答判定
- `ExamUnansweredNavigation` / `ExamSequentialNextPipeline`：随机全库 pending、顺序后→前→下一格
- `ExamPostAnswerAdvancePipeline` / `ExamEdgeSwipePipeline`：作答后/边缘左滑管道
- `ExamNavigationCoordinator`：`hasPending` / `canNavigate*` / 顺序 `next/prev` 走 pending 池
- `ExamViewModel`：对外 `hasPendingQuestions` / `canNavigateToNext/PrevUnanswered`
- `ExamScreenContent`：底栏箭头、单选自动跳转、左滑、返回键均走 pending 管道

**验证:** `:feature-exam:compileDebugKotlin` `:app:compileDebugKotlin`

---

## 2026-06-28 — 提交批改流畅度 + 重答后音效判题

**问题:** 点击「提交答案」批改区仍卡顿；重答后答对仍播放错误音效。

**修改:**
- `revealShowResult`：立即展示批改区，持久化/快照异步一次
- `PracticeAutoAdvanceController.advanceOnly`：已 reveal 时不再重复 `updateShowResult`
- `PracticeSubmitSideEffectsPipeline`：音效/回调/错题本延后到 reveal 后首帧
- `PracticeAnswerCorrectnessPipeline`：与 `buildPracticeAnswerResult` 统一判题（含填空 `resolveFillCorrectAnswer`）

**验证:** `:app:compileDebugKotlin`

---

## 2026-06-28 — 复盘右滑仅浏览已答历史（不含未答题）

**问题:** 随机模式交卷后「答题详情」右滑会进入未作答题；应仅按作答时间倒序浏览已答。

**根因:** 复盘 `browseAnsweredHistory*` 误用 `ReviewBrowseSession.displayOrder`（含未答置后），与练习中右滑语义不一致。

**修改:**
- `ReviewAnsweredSwipePipeline`：`buildOrder` + `resolveOlder/NewerIndex`（仅已答时间倒序）
- `PracticeViewModel`：复盘右滑走 `reviewAnsweredSwipeOrder`；箭头仍用 `reviewBrowseSession` 全量顺序

**验证:** `:app:compileDebugKotlin`

---

## 2026-06-28 — DeepSeek 提问多轮答案追加持久化

**问题:** 首次提问保存后再次提问，新答案覆盖旧内容，未接在第一次答案后持久化。

**修改:**
- `DeepSeekAskPersistFormatPipeline`：结构化编解码 + `---` 分隔兼容旧数据
- `DeepSeekAskDisplayPipeline`：多轮 assistant 纵向拼接展示
- `DeepSeekAskSavePipeline`：落库展示文本（非仅最后一轮）
- `DeepSeekAskViewModel`：`turns` 驱动 `displayText`；`loadSaved` 恢复多轮上下文；`saveAndWait` 返回会话展示文本

**验证:** `:app:compileDebugKotlin`

---

## 2026-06-28 — DeepSeek 提问保存与答题页 AI 展示联动

**问题:** 提问页退出保存后内容未持久化；顶栏 AI 图标不变色；答题区不显示已保存问答。

**根因:** `AppNavHost` 将 `deepseek_ask` 保存到笔记（`appendNoteSuspend`），而加载/图标/展示区读 `question_analysis` + `analysisList`。

**修改:**
- `DeepSeekAskPersistPipeline`：提问结果与解析共用 `question_analysis`；兼容 `question_ask` 与旧笔记前缀
- `DeepSeekAskViewModel.saveAndWait`：写入 `SaveQuestionAnalysisUseCase`；`getSavedAnswer` 统一加载
- `DeepSeekAskScreen`：保存时先 `saveAndWait` 再 `onSave`
- `AppNavHost`：`deepseek_ask` 的 `onSave` 改为 `updateAnalysis`（对齐 `DeepSeekScreen` / `AppNavRoutes`）

**验证:** `:app:compileDebugKotlin`

---

## 2026-06-28 — DeepSeek 提问页多轮对话（对齐 App 端）

**需求:** 首次回答不满意时可「再次提问」，模型需知晓先前回答并与题目正确答案对照修正。

**依据:** [DeepSeek Multi-round Conversation](https://api-docs.deepseek.com/guides/multi_round_chat) — API 无状态，每轮请求须携带完整 `messages`（含 prior assistant）。

**修改:**
- `DeepSeekChatTurn` / `DeepSeekMultiTurnMessagesPipeline`：system + 历史 user/assistant + 新 user
- `DeepSeekAskFollowUpPipeline`：再次提问时自动注入「先前回答需修正」follow-up；用户改题则用新内容
- `DeepSeekAskViewModel`：内存维护 turns；`restoreSession` 从持久化恢复首轮上下文
- `DeepSeekApiService.chat(messages)` + `thinking: disabled`（V4-Flash 快速问答）
- `DeepSeekChatConfig.SYSTEM_PROMPT` 补充多轮纠错指引

**验证:** `:app:compileDebugKotlin`

---

## 2026-06-28 — 提交批改即时显示 + 随机模式重进历史按作答时间

**问题:** 点击「提交答案」到批改区显示有卡顿；随机模式退出再进入后右滑历史不是最近作答顺序。

**修改:**
- `PracticeSubmitRevealPipeline`：提交后立即 `updateShowResult`，`autoAdvance` 仅 delay + 跳转（`revealResultFirst=false`）
- `PracticeProgressRestorePipeline`：进度恢复合并 `textAnswer`/`answerTime` 并补偿缺失时间戳
- `PracticeAnsweredHistorySeedPipeline`：重进时从持久化状态重建 `answeredHistorySnapshots`
- `PracticeProgressLifecycleCoordinator.loadProgress` 完成后 `onProgressRestored` → VM 调用 seed
- `NavigationHistory.seedRandomNavigationHistory` 排序改为 `AnsweredBrowseOrder` 时间倒序

**验证:** `:app:compileDebugKotlin`

---

## 2026-06-28 — 随机模式：按「是否仍有未答题」判定结束，非答题卡末格

**问题:** 随机模式下位于答题卡最后一格时，右箭头变灰、答完弹出交卷窗；但题库仍有未作答题。

**修改:**
- `PracticeUnansweredNavigation`：随机模式 `hasNext/Prev` 与跳转池改为「除当前格外的全部 pending」
- `PracticePostAnswerAdvancePipeline`：批改后 `hasPendingQuestions` → 继续 `nextQuestion` / 否则结束
- `PracticeViewModel.hasPendingQuestions()` 暴露 pending 判定
- `PracticeScreen`：自动跳转改用管道，移除 `answeredIndex >= total - 1` 判断

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — 练习批改后重答：整题清空 / 错题保留答对空

**问题:** 「重答该题」「重答错题」仅 `updateShowResult(false)`，未清空或部分清空作答。

**修改:**
- `PracticeQuestionRetryPipeline`：`reopenCurrent` 清空选项与填空；`reopenWrongBlanks` 用 `retainCorrectFillAnswerParts` 保留答对空
- `PracticeViewModel.retryCurrentQuestion` / `retryWrongBlanks`：写回状态并 `saveProgress`
- `PracticeScreen`：两按钮改调上述 VM 方法

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — 练习字体持久化 + 提交图标双击交卷确认

**问题:** 练习界面行距/字距未从 DataStore 加载且变更未即时写入；提交答案图标无双击交卷入口。

**修改:**
- `FontSettingsRepository` + Impl：新增 `practiceLineSpacing` / `practiceLetterSpacing`
- `PracticeFontController`：对齐 `ExamFontController`，启动 `loadFromStore()`，菜单变更即时持久化
- `PracticeSubmitFlow` + `QuestionNavigationControls.onSubmitDoubleClick`：双击提交图标弹出交卷确认（未作答则直接退出）
- `PracticeScreen`：接入上述控制器与双击逻辑

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — DeepSeek V4：MODEL 与 API_URL 常量

**修改:**
- `DeepSeekChatConfig.API_URL`：`https://api.deepseek.com/v1/chat/completions`
- `DeepSeekChatConfig.MODEL`：`deepseek-chat` → `deepseek-v4-flash`（官方 V4，2026-07-24 前兼容别名将弃用）
- `DeepSeekApiService` 使用 `API_URL` 常量

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — DeepSeek API 输出对齐 Web/App 风格

**需求:** API 调用增加 system 提示词与对话参数，使解析/问答风格接近 DeepSeek Web/App。

**修改:**
- `DeepSeekChatConfig`：`SYSTEM_PROMPT`、`temperature=0.7`、`presence_penalty=0.3`
- `DeepSeekChatMessages`：system + user 消息管道
- `DeepSeekApiService`：`analyze`/`ask` 统一走 `complete()` 单一路径

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — 全答答题卡分题号：以词条列为中心向两侧排列

**需求:** 展开后分题号应以该词条题号所在列为中心，向左右对称排布。

**修改:**
- `AnswerCardRoundLinePlacement`：纯函数计算 `startColumn = anchor - (n-1)/2`（边界 clamp）；超 5 个按行 chunked 且每行仍居中
- `AnswerCardEntryGridLines.RoundLine`：增加 `anchorColumn`、`startColumn`
- `AnswerCardCompactEntryGrid`：分题号行按 5 列网格空位 Spacer + 居中格渲染

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — 全答答题卡分题号：5 列换行 + primary 字色

**问题:** 分题号溢出时未从行首重排；分题号与普通词条不易区分。

**修改:**
- `AnswerCardEntryGridLines`：分题号按 5 列 `chunked`，每行自左对齐（与词条行同网格）
- `AnswerCardRoundCell`：分题号 `primary` 字色；当前题保留 primary 边框

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — 全答答题卡：单击展开 / 双击跳转 + 分行分题号

**需求:** 词条格单击展开多轮、双击进入作答；题号底部 M3 ExpandMore 指示；展开后分题号独占一行。

**修改:**
- `AnswerCardEntryCell`：`combinedClickable` 单击切换展开、双击 `onSelect`
- `AnswerCardExpandIndicator`：12dp ExpandMore + 180° 旋转（M3 accordion 惯例）
- `AnswerCardEntryGridLines`：词条 5 列行 + 展开后整行 `RoundLine`
- `AnswerCardCompactEntryGrid`：LazyColumn 分行布局；分题号双击跳转

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — 全答答题卡紧凑折叠（词条 1、2、3 + 点击展开轮次）

**需求:** 全答答题卡恢复最初 5 列紧密网格；默认每词条一格 `1` `2` `3`…；点多轮词条才在同网格内展开 `1①` `1②`…

**修改:**
- `AnswerCardEntryCompactLayout`：折叠态词条序号 + 轮次圈号标签；聚合作答状态
- `AnswerCardCompactEntryGrid` + `AnswerCardCell`：5 列网格；单轮直跳，多轮点击展开/再点收起
- `AnswerCardDialogContent`：全答词条模式走紧凑网格，非全答仍按题型折叠
- `AnswerCardGrid` 复用 `AnswerCardCell`

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — 全答原子题库答题卡：词条分组 + 标签格式

**需求:** 练习/考试全答模式下，原子题库答题卡显示「词条号-(出题号)」，按词条折叠展开（对齐考试能力并统一格式）。

**修改:**
- `AnswerCardDisplayInfoPipeline`：全答多轮标签 `4(1)`；词条折叠标题为纯数字 `4`

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-28 — 全答双击跳未作答词条（单题池修复）

**问题:** 原子题库全答模式下，双击左/右能解析到未作答词条（log `picked=96/76`），却弹出「前/后面没有未作答的词条了」；仅第一次跳转成功，后续失败。

**根因:** `sourceIndices` 对无多轮衍生题的单题词条返回空池；`isSourceIncomplete` 用 `isPendingAt(entry)` 判 true，但 `resolveFirstPendingInSource` 在空池上 `firstOrNull` 恒为 null → `skipToUnansweredSource` 误判无目标。`canSkipToUnansweredSource` 只查 entry 不查 target，按钮可点但必 Toast。

**修改:**
- `PracticeFullAnswerUnansweredSourceNavigation.resolveFirstPendingInSource`：空池时回退 `sourceEntryIndex.takeIf(pending)`，与 `isSourceIncomplete` 对齐
- `NavigationController.canSkipToUnansweredSource`：同时要求 `resolveFirstPendingInSource != null`
- 失败时 log `skipToUnansweredSource | target=null`

**验证:** `:app:compileDebugKotlin` SUCCESS；Logcat `adb logcat -s PracticeHistorySwipe`

---

## 2026-06-27 — 全答历史滑动（三次修复 + 诊断日志）

**问题:** 左滑池末未跨词条；右滑在 idx 3/15 间来回；池重排破坏全局时间序。

**修改:**
- 取消 `applySourcePoolPriority` 重排；`orderedIndices` 严格全局作答时间倒序
- `resolveOlder/NewerTargetIndex`：池内优先步进，池边界沿 **globalPos±1** 跨词条（按时间，非词条题号）
- `Active.anchorPoolIndices` 冻结锚点轮次池
- 诊断日志 tag **`PracticeHistorySwipe`**：`buildOrdered` / `resolveOlder` / `resolveNewer` / `swipeRight` / `swipeLeft` / VM / UI

**Logcat 过滤:** `adb logcat -s PracticeHistorySwipe`

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 全答已答历史滑动（二次修复）

**问题:** 左滑无反应；轮次池边界跨词条误用词条顺序而非作答时间；首次右滑可能跳到更新的题。

**根因:**
- 进入历史用 `first != current` 而非「当前 position + 1（更旧）」
- 历史列表随 `currentIndex` 重建，position 错位
- 浏览历史时 `applyAnsweredHistorySnapshot` 跳过快照，UI 无变化

**修改:**
- `Active.orderedIndices`：进入历史时冻结；position 0 = 最新作答
- `PracticeAnsweredBrowseNavigation.resolveOlder/NewerHistoryPosition`
- 全答 `applySourcePoolPriority` 以 `anchorIndex`（origin）为锚；池内/池外均保持时间倒序
- 跨词条 = 冻结列表中 position ±1，不按词条题号顺序
- 历史浏览 `preferSnapshot = true` 强制显示快照
- Idle 态左滑：当前题在列表且非最新时可直接浏览更新作答

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 全答模式：已答历史滑动手势修复

**问题:** 全答模式左滑无反应；轮次池首/末题无法滑到相邻词条历史。

**修改:**
- `PracticeFullAnswerHistoryNavigation.kt`：当前词条轮次池优先排序；池首/池末跨词条历史
- `NavigationHistory.navigateToNextAnsweredInHistory`：左滑先恢复 overlay；`historyPosition<=0` 时恢复现场或池末跨下一词条
- `NavigationHistory.navigateToPreviousAnsweredQuestion`：池首右滑至尽头时跨上一词条
- 修正历史快照 `isQuestionAnswered` 回调（不再误用 `isQuestionPendingForCurrentMode`）

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 练习：批改态取消自动跳转 + 全答单击轮次池

**问题:** 自动批改后滑动/点击解析等区域未停止自动跳转；全答模式单击左/右在轮次池未遍历完时误跳下一词条。

**修改:**
- 批改态交互取消 `autoAdvance`：答题结果区 `onInteraction`、解析/笔记/AI 区 `onInteraction`、横向滑动起始即取消
- `PracticeFullAnswerIconNavigation.kt`：全答**单击**在当前词条轮次池内跳转（含已答轮次）；题池全部答完才回落 `PracticeUnansweredNavigation` 跨词条
- 全答**双击**仍走 `PracticeFullAnswerNavigation` 跳相邻词条；`nextQuestion()` 自动跳转逻辑不变（仅在计时未被取消时执行）

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 练习底栏：批改态可见 + 仅未答题导航

**问题:** 自动批改后底栏隐藏；历史浏览中点击左/右仍走历史；上一题未按未答题池跳转。

**修改:**
- 批改态（`showResult`）也显示左/右图标，隐藏提交按钮；用户交互（滑动/点击/解析区）取消自动跳转，由图标主动导航
- `PracticeUnansweredNavigation`：以当前题为锚，在题号更小/更大的未答题池中导航
- 顺序：上一题取最大小号未答题；随机：池内随机
- 历史浏览中点击图标：`exitAnsweredHistoryBrowsing` 后只跳未答题，不再走历史
- 边界 Toast：「已是最前的未答题」/「已是最后的未答题」
- 滑动手势仍专用于答题历史；`nextQuestion()` 保留自动跳转完整逻辑

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 练习全答双击跳词条

**问题:** 练习全答模式下底栏左/右图标无双击跳相邻词条，与考试不一致。

**修改:**
- `PracticeFullAnswerNavigation.kt`：`resolveSkipToAdjacentSourceIndex`（对齐考试）
- `NavigationController` / `PracticeViewModel`：`canSkipToAdjacentSource` / `skipToAdjacentSource`
- `PracticeScreen`：`onPrevDoubleClick` / `onNextDoubleClick`；随机练习时跳随机词条

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 练习题数设置变更即时生效

**问题:** 设置页「练习题数」修改后，练习仍按旧题数出题；`LaunchedEffect` 未监听 `practiceCount`。

**修改:**
- `PracticeScreen`：`LaunchedEffect` 增加 `practiceCount` / `randomPractice` 依赖，变更时 `reloadForFillConfig(count)`
- `PracticeProgressLifecycleCoordinator`：题数增大时不再复用过短的 `fixedQuestionOrder`；`reloadForFillConfig` 同步 `lastQuestionCount`
- `PracticeViewModel.reloadForFillConfig`：无参时沿用上次题数，避免误传 `0` 变成「全部」
- **补充:** `PracticeQuestionCountPolicy` — 「全部」(0) 时仅当已保存题数 ≥ 全库题数才复用 `fixedQuestionOrder`

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 练习底栏对齐考试 + 滑动手势拆分

**问题:** 练习底栏为文字按钮，与考试图标底栏不一致；左滑与「下一题」混用，答题历史浏览与顺序/随机导航未分离。

**修改:**
- `PracticeScreen`：复用 `QuestionNavigationControls`（左上一题 / 中提交答案 / 右下一题），移除 `PracticeSubmitControls`
- `NavigationController`：`prevQuestion` 仅顺序/随机上一题；历史浏览拆为 `browseAnsweredHistoryOlder/Newer`
- `NavigationHistory.navigateToNextAnsweredInHistory`：历史内左滑前进；边界返回 `AtLatestAnswered`
- 右滑：浏览更早作答历史；至最前题 Toast「已是最前作答题」
- 左滑：历史内下一题，至最近作答题 Toast「已是最后作答题」
- 历史浏览中（`showResult` 时）底栏仍显示左/右图标，可退出历史会话；中间提交隐藏
- 文案 `answered_history_at_latest` / `answered_history_at_oldest`

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 统一已答浏览管道 AnsweredBrowseOrder

**问题:** 复盘与练习右滑历史各自排序/重排，复盘 `prevQuestion` 仍走 `NavigationHistory` 快照叠加，数据流不统一。

**修改:**
- `AnsweredBrowseOrder.kt`（domain）：`buildAnsweredIndicesByTimeDesc` / `buildReviewDisplayOrder` 共用排序
- `ReviewBrowseSession.kt`：无状态 `displayOrder` + `position` 步进
- `SessionReviewPresentation.kt`：返回 `ReviewPresentation`，**不重排**列表，仅已答强制 `showResult`
- `PracticeAnsweredBrowseNavigation.kt`：练习右滑与 domain 衔接；`navigateReadOnly` 仅改 `currentIndex`
- `NavigationHistory.buildPreviousAnsweredIndices` → 委托 `PracticeAnsweredBrowseNavigation`
- `PracticeViewModel` / `ExamViewModel`：`reviewBrowseSession` 拦截 `next/prev`；`canReviewBrowseBack/Forward`
- `PracticeScreen` / `ExamScreenContent`：复盘导航与滑动手势沿 `displayOrder`（入口=最近作答题）

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 复盘复用答题页 + progressId 持久化 + 练习轮次 UI

**问题:** 练习全答无「第 x 轮 / 共 y 轮」；live-VM 杀进程后复盘丢失；`AnswerReviewScreen` 另造页面未复用答题 UI。

**修改:**
- `FillAnswerRoundLabel.kt`（ui-common）：练习/考试共用轮次标签
- `SessionReviewPresentation.kt`：复盘排序 + 已答强制 `showResult`
- `SessionReviewTarget.kt`：从 `progressId` 解析题库/模式/scope
- 删除 `AnswerReviewScreen.kt`；`exam_review/{progressId}` / `practice_review/{progressId}` 复用 `ExamScreen`/`PracticeScreen`（`isReviewMode` 只读）
- `navToResult` 透传 `sessionProgressId`；冷启动经 `loadReviewSession` 从 `ExamProgress`/`PracticeProgress` 恢复
- VM：`enterReviewSession` + `currentProgressId`

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 答题详情复盘屏 + 全答模式 A1/A2-b（已 superseded 复盘路由部分）

**问题:** 结果页「答题详情」进入整库编辑器；全答模式在填空数=全部时无多轮；考试「全答对」与交卷后批改语义冲突。

**修改 (方案 B live-VM + A1 + A2-b):**
- `AnswerReviewOrder.kt`：无状态排序（已答按 `sessionAnswerTime` 倒序，未答置后）
- ~~`AnswerReviewScreen.kt`~~ → 已改为复用 `ExamScreen`/`PracticeScreen` 复盘模式
- A1：`FULL_ANSWER` 时 `fillBlankCount` 兜底为 1（Coordinator + ViewModel + 启动加载）
- A2-b：`ExamFillConfig.forExamSession()` 考试侧 `fullAnswerRequireCorrect=false`；设置页补充说明文案

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 考试全答模式：出题、完成条件与轮次导航

**问题:** 考试模式下「全答模式」未生效；未结合「全答完成条件」「全答轮次顺序」出题与导航。

**根因:**
- 交卷前 `showResult` 恒为 false，导航误把已作答轮次当作未完成
- 顺序考试 `nextQuestion()` 仅 `index+1`，未按同源轮次编排
- 变换后 `fixedQuestionOrder` 仅存源题 ID，未持久化变体顺序
- 设置变更不触发热重载

**修改:**
- 新增 `ExamFillConfigPipeline.kt`：无状态读取/变换管道（含 `fullAnswerRandomOrder`）
- 新增 `ExamFullAnswerNavigation.kt`：交卷前按作答内容、交卷后按批改+完成条件判定 pending
- `ExamNavigationCoordinator`：全答顺序/随机导航
- `ExamLoadDelegate`：变换后持久化变体 `fixedQuestionOrder`；`reloadForFillConfig()`
- `ExamScreen` / `ExamScreenContent`：监听 `fillConfigVersion` 热重载

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 练习页动态填空出题模式生效

**问题:** 设置页「填空题 → 动态填空出题模式」修改后不作用于练习答题；全答案模式导航失效。

**根因:**
- 练习加载路径未统一读取 Fill 配置并应用 `transformQuestionVariantsForFillSettings`
- `PracticeScreen` 在 `progressId` 未变时跳过重载，设置变更不触发题目重算
- `PracticeViewModel` 将 `fullAnswerModeActive` 硬编码为 `false`，`findNextSourceEntryIndices` 为空
- `SessionProgressManagerImpl` 的 `sessionId` 与 `|fill=` 签名格式不一致

**修改:**
- 新增 `PracticeFillConfigPipeline.kt`：无状态读取配置、签名、敏感判定、变换管道
- `PracticeProgressLifecycleCoordinator`：统一经管道加载/重载；`reloadForFillConfig()`；保存时写入 fill 签名
- `PracticeSpecialQuestionLoader`：错题/收藏路径同样经管道变换
- `PracticeScreen`：监听 fill 配置版本，同 progressId 时调用 `reloadForFillConfig()`
- `PracticeViewModel`：恢复 `fullAnswerModeActive` / `findNextSourceEntryIndices` 接线
- `PracticeAnswerHandler`：新增 `findNextSourceEntryIndices()`
- `SessionProgressManagerImpl`：修正 `sessionId` 为 `{progressId}_{seed}|fill={signature}`

**验证:** `:app:compileDebugKotlin` SUCCESS

---

## 2026-06-27 — 考试页字体持久化、字间距与交卷按钮

**问题:** 考试页字体大小/行距未从 DataStore 加载；缺少字间距调节；底栏无交卷入口。

**修改:**
- `ExamFontController`: 启动时 `loadFromStore()`；新增 `examLetterSpacing` 读写
- `FontSettingsRepository` / `FontSettingsRepositoryImpl`: 新增 `examLetterSpacing`
- `ExamFontSettingsMenu`: 提取三点菜单（含字间距增减）
- `ExamSubmitFlow` + `QuestionNavigationControls`: 上一题/交卷/下一题三按钮布局
- `ExamScreenContent`: 接线字体加载、字间距传递、交卷确认对话框

**验证:** `:app:compileDebugKotlin` SUCCESS

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
