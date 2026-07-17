# 项目重构方案与进度

## Phase 27: 自适应渐隐原子练习（P80 🟡）

| 批次 | 内容 | 状态 |
|------|------|------|
| P80a | 方案、ADR、Session Kind/Creator/Registry/Policy | ✅ |
| P80b | 双池抽题、四阶段渐隐、错答回退、独立 Room 状态 | ✅ |
| P80c | SQLite/DB 首页入口、结果页隔离、纯函数测试 | ✅ |
| P80d | JDK 21 全量编译、lint、单测、APK 冒烟 | ⏳ 环境网络阻塞 |

约束：不替换普通练习管道，不修改原子题库源数据；后续个性化间隔在 MVP 数据验证后另立 ADR。

## Phase 1: 统一状态模型 — 已完成
- UnifiedQuestionState + UnifiedSessionState 创建
- Old types 标记 @Deprecated (保留 JSON 反序列化兼容)

## Phase 2: SessionEngine 接线 — 已完成
- SessionProgressManagerImpl (save/load/clear/restore)
- SessionAnalysisLoaderImpl (analysis/spark/baidu/note)
- SessionMemoryModeImpl (memory round plan)
- SessionModule (Hilt binding)
- PracticeVM + ExamVM 已注入 SessionEngine
- 分析加载已委托、saveProgress/clearProgress 已委托
- loadProgress 公共恢复逻辑已抽离到 SessionProgressManager
- 答题交互/updateShowResult/updateAnalysis 已委托

## Phase 3: 红线 Coordinator 拆分 — 已完成
- PracticeSessionCoordinator → ProgressPersistence + AnalysisManager + QuestionLoader
- PracticeNavigationCoordinator → NavigationState + NavigationHistory + NavigationController
- @Deprecated bridge 类型已清理直接委托 NavigationState.kt

## Phase 4: UseCase Facade — 已完成
- PracticeUseCaseFacade (15→1 注入)
- ExamUseCaseFacade (22→1 注入)

## Phase 5: 模块迁移 — ~93% 完成

| 目标 | 状态 | 说明 |
|-----|------|------|
| feature-practice | **Content + components** | `PracticeScreenContent` / Effects / Overlays 在 feature；`PracticeScreen.kt` 薄包装留 `:app` |
| feature-exam | **Content + components** | `ExamScreenContent` / Effects 在 feature；`ExamScreen.kt` 薄包装 + `ExamAISyncEffects` 留 `:app` |
| :core session/ | 已创建 | SessionEngine + ProgressManager + AnalysisLoader 等 |
| :domain 模型/UseCase | 37 UseCase | 从 :app 迁移到 :domain |
| :ui-common | 12 文件 | AnswerCardGrid, FontStyleProvider 等 |
| util.* → :core | 6 文件 | core.util |
| LocalizedResult → :core | 已完成 | core.common.LocalizedResult |
| FontSettingsRepository | 接口 `:core`；Impl `:data` | ✅ P63 |
| **PracticeScreen wrapper** | **`:feature-practice`** | 音效 raw 由 `:app` Route 注入 |
| **ExamScreen wrapper** | **`:feature-exam`** | 收藏 Favorite 经 `:app` `rememberExamFavoriteBindings` |
| **:app/components** | **~26 文件** | 仅 Settings IO + Session/AI 路由/Hilt |

### 模块文件分布

| 模块 | 文件数 |
|------|-------|
| :app | ~184 |
| :domain | ~64 |
| :data | ~59 |
| feature-practice | ~35 |
| feature-exam | ~32 |
| :core | ~11 |
| :ui-common | ~12 |

### 代码分布趋势
- 重构前: app(84%) → data(10%) → domain(6%)
- P58 当前: **app(~18% 行 / ~12% 文件)** → feature-practice(~27%) → ui-common(~12%) → feature-exam(~15%) → data(~9%) → feature-settings(~5%) → core(~5%) → domain(~5%) → feature-ai(~3%)
- 目标: app(≤40%) → core(20%) → feature-*(25%) → ui-common(10%) → domain(5%) — **app 行占比已达标 ✅**

## Phase 16: Practice/Exam Screen 壳下沉（P67 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P16a | `PracticeScreen` → `:feature-practice`；音效 raw 由 Route 注入 | ✅ P67 |
| P16b | `ExamScreen` → `:feature-exam`；收藏经 `rememberExamFavoriteBindings` | ✅ P67 |
| P16c | 删除 `SessionCommandDispatch` 垫片 | ✅ P67 |

**验收：** `ArchitectureTest`（feature 互不依）+ `compileDebugKotlin` ✅

## Phase 17: AI 回调导航 + Settings IO 收尾（P68 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P17a | AI/Note/Explanation `NavController` → `onBack`/`onOpenAsk`；`AppNavAiRoutes` 瘦身 | ✅ P68 |
| P17b | `SettingsImportFilePipeline`；`ImportResult` 统一；Gateway 绑定保持 | ✅ P68 |

**验收：** `compileDebugKotlin` + `ArchitectureTest` ✅

## Phase 18: 遗留死代码 + Settings JSON 管道化（P69 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P18a | 删除 `AppNavRoutes` 遗留 VM 路由（`registerAnalysisRoutes` 等）；保留 `navToResult` | ✅ P69 |
| P18b | `SettingsJsonExportUriPipeline`；删 `:app` 重复 `Localization.kt` | ✅ P69 |

**验收：** `compileDebugKotlin` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

## Phase 19: Excel 导出管道化（P70 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P19a | `ExcelSheetBuilder` → `:feature-settings`；`SettingsExcelExportPipeline` | ✅ P70 |
| P19b | `SettingsExcelWorkbookUriPipeline`（`:app` POI 写 Uri）；历史导出去 impl 强转 | ✅ P70 |

**验收：** `compileDebugKotlin` + `ArchitectureTest` ✅

## Phase 20: 导入管道化 + QuizFileBrowser 下沉（P71 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P20a | `SettingsImportResultPipeline` / `SettingsImportUriPipeline`；`ImportCoordinator` 瘦身 | ✅ P71 |
| P20b | `QuizFileBrowser` + `SettingsLocalFileImportHostImpl` → `:feature-settings`；删死代码 | ✅ P71 |

**验收：** `compileDebugKotlin` + `ArchitectureTest` ✅

## Phase 21: 统一 SettingsIoUriPipeline（P72 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P21 | 合并 JSON/Excel/Import `*UriPipeline` → `SettingsIoUriPipeline` | ✅ P72 |

**验收：** `compileDebugKotlin` + `ArchitectureTest` ✅

## Phase 22: Settings IO 收尾（P73 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P22a | `SettingsImportFilePipeline` 并入 `SettingsIoUriPipeline` | ✅ P73 |
| P22b | 删除 `SettingsLocalFileImportHost` 间接层；`SettingsScreen` 直连 `QuizFileBrowser` | ✅ P73 |

**验收：** `compileDebugKotlin` + `ArchitectureTest` ✅

## `:app` 薄壳终态（P78 后 — 模块化主链完成 ✅）

`:app` `presentation/` **~22 个 `.kt`**：
1. Session/AI 导航薄路由 + `AppNavAiWritebackPipeline` + Hilt EntryPoint / Registry
2. Home/Practice/Exam/Result/History 薄 Route 与 Session bindings

**模块化重构主链（P51–P78）已全部完成。**

## Phase 23: Strategy 长尾（P75 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P23a | `PracticeProgressIdPipeline` / `PracticeReviewSessionLoadPipeline` | ✅ P74 |
| P23b | Navigation orchestration → Strategy 提供方（Practice bind / Exam 删死字段） | ✅ P74 |
| P23c | `NavigationSequentialNext` → `SessionPracticePostAnswerNavigationPipeline` 门禁 | ✅ P75 |
| P23d | `NavigationEnvironment` icon-nav 尊重 `exitAnsweredHistoryBeforeIconNav` | ✅ P75 |
| P23e | Exam 复盘/持久化对称：`ExamReviewSessionLoadPipeline` / `ExamReviewSourceQuestionsPipeline` / `ExamProgressPersistencePipeline.shouldRestoreAnswersFromMap` | ✅ P75 |
| P23f | `NavigationSequentialPrev` → Strategy 门禁 + `resolveBackwardAdvanceRoute` | ✅ P76 |
| P23g | `ExamProgressIdPipeline`（`exam_` 前缀对称） | ✅ P76 |

**Phase 23 Strategy 长尾主链完成。**

## Phase 24: `:app` 薄壳终态收尾（P77 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P24a | Settings IO 实现下沉 `:feature-settings`（`SettingsIoUriPipeline` + 3 coordinators + `SettingsIoBindingModule`） | ✅ P77 |

`:app` `presentation/` **~22 个 `.kt`** — 仅导航薄路由 + Session bindings + Hilt EntryPoint。

**模块化重构主链（P51–P78）已全部完成。**

## Phase 25: 可选收尾（P78 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P25a | AI 叠层写回 → `SessionCommand`（`AppNavAiWritebackPipeline` + `AppendNote`） | ✅ P78 |
| P25b | `:app` 移除冗余 POI/StAX 依赖 | ✅ P78 |
| P25c | `detekt.yml` 收紧（unused imports/members、WildcardImport、EmptyCatchBlock、IgnoredReturnValue） | ✅ P78 |

## Phase 26: 归档计划收尾（P79 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P26a | `QuestionAnalysisRepository.kt` → `QuestionAnalysisUseCases.kt`（domain 命名修正） | ✅ P79 |
| P26b | `:app` 移除冗余 Room 依赖（`:data` 传递） | ✅ P79 |
| P26c | `AppNavAiWritebackPipelineTest` + `config/detekt` 指向根配置 | ✅ P79 |

**主链外无阻塞项；后续仅为产品功能 / 真机回归 / 按需 Strategy 深化。**

## Phase 13: `:data` 持久化 + QuestionEdit 收尾（P63 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P13a | `FontSettingsRepositoryImpl` → `:data`；`DataBindModule` 绑定；删 `AppModule` | ✅ P63 |
| P13b | `DrawerQuestionEditHost` + `DrawerQuestionEditPipeline` → `:feature-practice` | ✅ P63 |

**验收：** `compileDebugKotlin` + `DrawerQuestionEditPipelineTest` + `ArchitectureTest` + `check-loc-over-500.ps1` ✅

## Phase 14: DrawerQuestionEdit 接线 + 单测基建（P64 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P14a | `question_edit` 路由 + 抽屉长按改题 + `QuestionEditSessionRoutePipeline` | ✅ P64 |
| P14b | `feature-practice` junit/coroutines-test；跳过 unit-test kapt | ✅ P64 |

**验收：** 同上 + `PracticeSessionStrategyCoordinatorTest` ✅

## Phase 15: Browse 详情 + Result/History/WrongBook 下沉（P65 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P15a | `question_detail` → `question/{quiz}?targetQuestionId=0` Browse；删 `QuestionScreen` | ✅ P65 |
| P15b | `ResultScreen`/`HistoryScreen`/`WrongBookScreen`/`FavoriteScreen` → `:feature-practice` + 薄路由 | ✅ P65–P66 |

**验收：** `compileDebugKotlin` + `BrowseSessionRoutePipelineTest` + arch 单测 ✅

## 遗留（P58 后 — 已过时，见 P63 后）

## Phase 10: `:app` VM 外置 + components 下沉（P58 收尾）

| 批次 | 内容 | 状态 |
|------|------|------|
| P10a | WrongBook/Favorite VM → `:feature-practice` | ✅ P53 |
| P10b | `ImportQuestionsUseCase` → `:domain` | ✅ P53 |
| P10c | `SoundEffects` / `SwipeRevealActionBox` → `:ui-common` | ✅ P53 |
| P10d | Exam 编辑对话框 → `SaveEditedQuestionFields` Command | ✅ P53 |
| P10e | AI VM + SessionAnalysis* + AISyncEffects → `:feature-ai` / feature | ✅ P54 |
| P10f | FileCard/Result/Drawer pipelines → `:ui-common` | ✅ P54（部分） |
| P10g | `DrawerQuestionEditHost` → `rememberQuestionEditSessionBindings` + Command | ✅ P54 |
| P10h | AI Screens（R/Settings 解耦）→ `:feature-ai` | ✅ P55 |
| P10i | Settings VM + coordinators + Screen/ui | ✅ P56 VM / P58 UI |
| P10j | NoteScreen → `:feature-ai` | ✅ P56 |

## Phase 11: Home / Progress 瘦身（P60 收尾 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P11a | Home drawer/shell 外置 + settings res 下沉 | ✅ P57 |
| P11b | Progress load/save Pipeline | ✅ P57 |
| P11c | Home dialogs/overlays/swipe 预防性拆分 | ✅ P58 |
| P11d | `HomeFileListGrid` + `HomeFileListDragScroll` 外置 | ✅ P59 |
| P11e | `PracticeProgressLoadQuestionsPipeline` + review/reset 管道 | ✅ P59 |
| P11f | `HomeFileListColumn` 外置 | ✅ P60 |

## Phase 12: `:app` components 下沉（P62 收尾 ✅）

| 批次 | 内容 | 状态 |
|------|------|------|
| P12a | `OptimizedFileCard` / `DraggingFileCard` → `:ui-common` | ✅ P61 |
| P12b | `PracticeProgressApplyLoaded` + `SaveRequest` Pipeline | ✅ P61 |
| P12c | Home Screen 包 → `:feature-practice`（薄 `HomeRoute` 留 app） | ✅ P62 |
| P12d | QuestionBank drawer + VM → `:feature-practice` | ✅ P62 |
| P12e | `FontSettingsDataStore` → `:data`（解耦 Home 依赖） | ✅ P62 |

**验收：** `:app` Kotlin 占比 ≤40% ✅；`ArchitectureTest` 模块边界通过 ✅

## 历史：PracticeScreen 迁移阻塞（P51 已解除）

原阻塞项（Sound/FontSettings/6 VM/R.string/共享组件）经 P51 以 `ExternalPracticeState` + `feature.practice.R` + 组件迁移逐项解除；仅 AI VM 与 Settings 注入仍留 `:app` 薄包装。

## Phase 6: ExamVM 状态统一 — 已完成
- 16 StateFlow → 1 PracticeSessionState
- 向后兼容派生的 StateFlow

## 额外清理
- `.history/` 备份目录已删除
- `domain/bin/` 已删除
- `AppNavHost_backup.kt` 已删除
- `PracticeNavigationCoordinator` 的 `@Deprecated` bridge 类型已移除

## 历史 Commits 说明
由于大量文件跨模块移动（:app → feature-*），git diff 显示大量 `D` (删除) + 新增。这是文件级重命名的正常结果，非预期数据丢失。所有关键文件已验证存在于目标模块中。

## 2026-06-14 剩余优化执行方案

### Phase 7: SettingsViewModel Facade — 已完成
- 目标: 将 `SettingsViewModel` 的 Repository 注入从 7 个聚合为 1 个 `SettingsRepositoryFacade`。
- 变更:
  - 新增 `SettingsRepositoryFacade`，聚合 Question/WrongBook/History/Favorite/Analysis/Ask/Note repositories。
  - `SettingsViewModel` 改为通过 facade 访问各 repository。
- 结果: 构造函数注入项从 12 降至 6，回到 <=8 红线内。

### Phase 8: ExamViewModel Coordinator 拆分 — 已完成
- 目标: 将 `ExamViewModel` 中仍内嵌的编辑、评分、统计职责拆出。
- 变更:
  - 新增 `ExamQuestionEditCoordinator`，承接 `prepareEditableQuestion`、`clearEditableQuestion`、`normalizeEditedSelectedOptions`、`saveEditedQuestion`。
  - 新增 `ExamGradeCoordinator`，承接 `scheduleGradeExamAfterDispose`、`gradeExam`、`calculateElapsedTime`。
  - 新增 `ExamStatisticsCoordinator`，承接 `calculateCumulativeStats`、`incrementExamCount`。
- 结果: `ExamViewModel` 中编辑、评分、统计职责已改为委托；当前约 590 行。

### Phase 9: FontSettingsDataStore 泛型代理 — 已完成
- 目标: 降低 DataStore get/set 样板代码。
- 变更:
  - 新增 `PreferenceDelegate<T>` 与 `BooleanPreferenceDelegate`。
  - 在 `FontSettingsDataStore` 内使用代理实现原有 getter/setter。
  - 保留现有公开 API，避免调用方大规模迁移。
- 结果: `FontSettingsDataStore` 当前约 247 行；设置读写逻辑集中，外部公开 API 不变。

### Phase 7-9 验证
- IDE lints: 无错误。
- Compile: `.\gradlew.bat :app:compileDebugKotlin :feature-exam:compileDebugKotlin` ✅ BUILD SUCCESSFUL。
- TD-014: `Swipeable`/`FractionalThreshold` 全仓库无引用 — CLOSED（P52）
