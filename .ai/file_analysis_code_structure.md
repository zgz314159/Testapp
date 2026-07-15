<!--
  分析生成时间: 2026-06-14 09:37 UTC+8
  分析范围: PROJECT_ROOT 下全部 Kotlin 源代码文件（app/ data/ domain/）
  分析依据: 直接阅读源文件、.ai/file_registry.md、.ai/module_map.md、.ai/architecture_map.md、.ai/dependency_graph.md
-->

# 项目全部代码文件功能分析

---

## 一、项目总体结构

```
PROJECT_ROOT/
├── app/src/main/java/com/example/testapp/       ← :app 模块 (148文件, ~21962行)
│   ├── presentation/screen/                     ← 所有 Screen + ViewModel + Coordinator
│   │   ├── components/                          ← 共享UI组件 (12文件)
│   │   ├── practice/                            ← 练习专用组件 (3文件)
│   │   ├── settings/                            ← 设置子UI模块 (M3 卡片/ListItem 重构)
│   │   └── questionbank/                        ← 题库组件 (2文件)
│   ├── presentation/navigation/                 ← 导航路由 (1文件)
│   ├── presentation/viewmodel/                  ← AI分析VM (3文件)
│   ├── presentation/component/                  ← 共享Compose组件
│   ├── presentation/model/                      ← UI模型
│   ├── presentation/util/                       ← UI工具类
│   ├── data/datastore/                          ← DataStore (2文件)
│   ├── data/network/                            ← 网络API服务 (3文件)
│   ├── data/local/                              ← 本地工具类
│   ├── domain/model/                            ← (部分领域模型仍在app中)
│   ├── domain/usecase/                          ← (部分UseCase仍在app中)
│   ├── domain/repository/                       ← (部分仓库接口仍在app中)
│   ├── util/                                    ← 通用工具类
│   └── di/                                      ← Hilt DI模块
│
├── data/src/main/java/com/example/testapp/      ← :data 模块 (60文件, ~3121行)
│   ├── local/entity/                            ← Room实体 (10文件)
│   │   └── converter/                           ← Room类型转换器 (6文件)
│   ├── local/dao/                               ← Room DAO (10文件)
│   ├── local/                                   ← AppDatabase.kt
│   ├── repository/                              ← 仓库实现 (12文件)
│   │   └── parser/                              ← 文件解析器 (6文件)
│   ├── init/                                    ← 数据初始化
│   ├── mapper/                                  ← 数据映射器 (2文件)
│   ├── di/                                      ← DI模块
│   └── domain/                                  ← (2个文件在data中)
│
└── domain/src/main/kotlin/com/example/testapp/  ← :domain 模块 (28文件, ~1193行)
    ├── model/                                   ← 领域模型 (13文件)
    ├── repository/                              ← 仓库接口 (9文件)
    ├── usecase/                                 ← 用例 (1文件+app中多个)
    ├── util/                                    ← 工具类 (3文件)
    └── presentation/model/                      ← UI模型 (1文件)
```

---

## 二、app/.../presentation/screen/ — Coordinator 层（已从 VM 提取的子协调器）

### Practice 系列 Coordinator（6/6 完成）

| 文件 | 行数 | 依赖数 | 职责 | 功能描述 |
|------|------|--------|------|----------|
| **PracticeNavigationCoordinator.kt** | ~617 | 0 | 导航编排 | 顺序/随机 `next/prev` 与历史浏览 `browseAnsweredHistoryOlder/Newer` 分离；底栏图标走前者，右/左滑走后者 |
| **PracticeAnswerHandler.kt** | ~213 | 0 | 纯答案判断 | 零副作用的纯函数集合。`isQuestionAnswered()`/`isQuestionCorrect()`（支持选择题和填空题）、`isQuestionPendingForCurrentMode()`（综合全答案模式/要求正确模式判断）、`shouldReopenUnansweredReveal()`（只显示未答的题需重开）、`hasPendingQuestions()`/`findFirstPendingIndex()`/`findResumeIndex()`（起始位置恢复）、`currentSourcePendingIndices()`/`nextFullAnswerCandidateIndices()`/`currentFullAnswerCandidateIndices()`/`findNextSourceEntryIndices()`/`isCurrentSourceComplete()`（全答案模式下同源题目候选；单槽与多轮次统一 pending 判定） |
| **PracticeFillConfigPipeline.kt** | ~91 | 0 | 练习 Fill 管道 | 无状态管道：`read()` 从 `FontSettingsRepository` 读取动态填空配置；`signature()` 生成进度复用签名；`isFillConfigSensitive()` 判定题库是否受 Fill 设置影响；`applyTransform()` 调用 `transformQuestionVariantsForFillSettings` 生成练习题目变体 |
| **PracticeQuestionCountPolicy.kt** | ~22 | 0 | 练习题数策略 | `0`=全部；`canReuseSavedOrder` 在「全部」时要求已保存题数覆盖全库 |
| **PracticeRoundCompletePipeline.kt** | ~15 | 上一轮练习是否已答完 |
| **PracticeRoundReusePipeline.kt** | ~20 | 轮次完成后禁止复用 `fixedQuestionOrder` |
| **PracticeQuestionOrderPipeline.kt** | ~50 | 新轮次未答池出题 + recyclePool |
| **PracticeNewRoundProgressPipeline.kt** | ~25 | 新轮次空白 progress 快照 |
| **PracticeSessionRestorePipeline.kt** | ~40 | 新轮次不回填；中途/复盘才恢复 map |
| **PracticeSourceQuestionPipeline.kt** | ~15 | 练习侧源题 ID 归一化 |
| **PracticeRoundLoadLog.kt** | ~30 | 轮次加载日志（`PracticeRoundLoad`） |
| **PracticeProgressLifecycleCoordinator.kt** | — | — | 练习进度生命周期 | 题目加载主路径；`scheduleNavigationSave` 防抖导航落盘；`performSaveProgress` 在 Default/IO 合并 map |
| **PracticeProgressMapPipeline.kt** | ~25 | 0 | 进度 map 合并 | Default 线程将当前会话题态并入 `questionStateMap` |
| **PracticeReviewReusePipeline.kt** | ~15 | 0 | 复盘复用判定 | 同 progressId 且已加载则跳过 `loadReviewSession` |
| **PracticeReviewPresentationPipeline.kt** | ~25 | 0 | 复盘展示准备 | Default 线程 `SessionReviewPresentation` + 滑动序 |
| **LibraryCatalog.kt** | ~8 | 0 | 库列表目录 | `fileNames` + `fileStatistics` 轻量模型 |
| **ScopedLibraryCatalogPipeline.kt** | ~55 | 0 | 错题/收藏目录聚合 | SQL 计数 + 题型聚合，不经全量题目 |
| **GetWrongBookLibraryCatalogUseCase.kt** | ~12 | 0 | 错题库目录用例 | |
| **GetFavoriteLibraryCatalogUseCase.kt** | ~12 | 0 | 收藏库目录用例 | |
| **PracticeProgressRestorePipeline.kt** | ~45 | 0 | 进度恢复管道 | 合并 `questionStateMap` 字段；补偿缺失 `answerTime` |
| **PracticeAnsweredHistorySeedPipeline.kt** | ~25 | 0 | 已答历史 seed | 重进练习时从 `questionsWithState` 重建 `answeredHistorySnapshots` |
| **PracticeAnswerCorrectnessPipeline.kt** | 与 `buildPracticeAnswerResult` 一致判题 |
| **PracticeSubmitSideEffectsPipeline.kt** | reveal 后延后音效/回调/错题本 |
| **PracticeSubmitRevealPipeline.kt** | 调用 `revealShowResult`（无同步持久化） |
| **PracticeAutoAdvanceController.kt** | `advanceOnly` 跳过重复 reveal |
| **PracticeProgressCoordinator.kt** | ~99 | 0 | 进度工具 | 纯工具函数。`practiceProgressSeed()`（从 sessionId 提取种子）、`buildSessionIdWithFillSignature()`（构建带 Fill 配置签名的进度 ID，格式 `{baseId}_{seed}|fill={signature}`）、`extractFillConfigSignature()`/`canReuseByFillSignature()`（Fill 签名校验复用）、`fillGenerationModeFromSignature()`（从签名恢复 Fill 生成模式）、`buildProgressSnapshotFromState()`（从 sessionState 构建 PracticeProgress 快照） |
| **PracticeModeCoordinator.kt** | ~394 | 0 | 记忆模式引擎 | 记忆模式全部逻辑。Phase 4 激进提取后直接持有 `_sessionState: MutableStateFlow`、`Mutex`、`persistentQuestionStateMap`、`removedMemoryPoolQuestionIds`。纯函数：`buildMemoryRoundPlan()`（根据错题优先策略构建记忆轮计划）、`MemoryRoundPlan` 数据类。状态变更方法：`buildMemoryRoundStates()`（构建记忆轮题目，含 retainCorrectFillAnswerParts 逻辑）、`refreshMemoryRoundPoolIfNeeded()`（IN_OUT 模式动态补充新题）、`removeCurrentQuestionFromMemoryPool()`（移除并补充，ROUND 模式自动切换下一轮）、`advanceMemoryRoundIfNeeded()`（当前轮次完成后自动推进）、`updatePersistentStateMap()`、`effectiveCurrentMemoryRoundQuestionIds()` |
| **PracticeUnansweredNavigation.kt** | 顺序模式按锚点前后 pending；随机模式全库 pending（除当前） |
| **PracticeFullAnswerRoundIconNavPipeline.kt** | ~45 | 0 | 多轮单击池内导航 | pending 列表 → 目标索引；有 pending 不出池 |
| **PracticeFullAnswerSameSourceRoundAdvancePipeline.kt** | ~45 | 0 | 同词条换轮 | 当前轮次池完成后进同源其他轮 |
| **PracticeFullAnswerNextRoundPoolPipeline.kt** | ~35 | 0 | 相邻轮次号池 | 全局 round±1 pending |
| **PracticeFullAnswerRoundPoolPipeline.kt** | ~15 | 0 | 全答轮次题池 | 同轮次号跨词条索引 |
| **PracticeFullAnswerRoundSlotPendingPipeline.kt** | ~25 | 0 | 轮次槽 pending | 有输入=已答（全答）/ 须全对判定 |
| **PracticeFullAnswerRoundNavigablePipeline.kt** | ~25 | 0 | 轮次池可导航槽 | 委托 SlotPending |
| **PracticeFullAnswerIconNavOrderPipeline.kt** | ~10 | 0 | 全答箭头随机开关 | 全答读 `fillFullAnswerRandomOrder`，否则练习随机 |
| **PracticeFullAnswerIconNavTargetPipeline.kt** | ~35 | 0 | 池内上下题 | 顺序环绕 / 随机 |
| **PracticeFullAnswerIconUnansweredPipeline.kt** | ~55 | 0 | 非原子全答未答导航 | 顺序环绕 + 全答随机 |
| **PracticeFullAnswerIconNavigation.kt** | ~80 | 0 | 全答底栏箭头 | 轮次池 / 词条池委托 NavTarget；随机顺序 |
| **PracticePostAnswerAdvancePipeline.kt** | 批改后：仍有 pending → 继续；否则结束/交卷确认 |
| **PracticeFullAnswerIconRetryPipeline.kt** | ~35 | 0 | 全答图标错题重开 | 须全对且当前源未完成时，底栏图标导航优先 `reopenQuestionForFullAnswerRetry`，不跨词条 |
| **PracticeFullAnswerHistoryNavigation.kt** | ~90 | 0 | 全答滑动历史 | `resolveOlder/NewerTargetIndex` 池内优先 + 全局时间跨词条；`TAG=PracticeHistorySwipe` |
| **PracticeFullAnswerNavigation.kt** | ~55 | 0 | 全答跳词条 | 底栏**双击**（旧）：`resolveSkipToAdjacentSourceIndex`（顺序/随机相邻词条） |
| **PracticeFullAnswerUnansweredSourceNavigation.kt** | ~65 | 0 | 全答双击未作答词条 | 退出历史后按题号找前/后**仍有 pending** 的词条；`isSourceIncomplete` / `resolveFirstPendingInSource`（空轮次池回退 entry 自身）；`resolveUnansweredSourceEntryIndex` |
| **SkipUnansweredSourceResult.kt** | ~6 | 0 | 跳词条结果 | `Navigated` / `NoPrevSource` / `NoNextSource` |
| **PracticeFontController.kt** | ~65 | 0 | 练习字体持久化 | `practiceFontSize`/`practiceLineSpacing`/`practiceLetterSpacing` 读写（对齐 ExamFontController） |
| **PracticeSubmitFlow.kt** | ~12 | 0 | 练习交卷判定 | 双击提交图标：已作答→确认交卷；未作答→直接退出 |
| **PracticeQuestionRetryPipeline.kt** | ~30 | 0 | 批改后重答 | `reopenCurrent` 整题清空；`reopenWrongBlanks` 保留答对填空 |
| **PracticeQuestionListDialog.kt** | ~65 | 0 | 练习答题卡 | `AnswerCardListDialogShell` + `AnswerCardDialogContent` |
| **PracticeFullAnswerCoordinator.kt** | ~112 | 0 | 全答案模式 | `shouldReuseSavedSourceOrder()`（判断是否复用保存的题目顺序）、`restoreConfiguredQuestionsForProgress()`（根据 Progress 中的 questionStateMap 恢复已配置的 Fill 题目变体）、`shouldApplyDynamicFillTransform()`（检测题目是否需要动态 Fill 变换，仅对 JSON/SQLite 来源的全填空题有效，缓存到 `dynamicFillSensitivityCache`） |
| **PracticeSessionCoordinator.kt** | ~641 | 4（sub-coordinator）+9 UseCases | 会话管理+持久化 | 最复杂的 Coordinator。持有所有子协调器引用和 UseCase。核心方法：`buildStoredQuestionState()`（构建带 DeepSeek/Spark/Baidu AI 分析和笔记的完整 QuestionWithState）。`saveProgress()`（Mutex 保护的异步保存，含记忆模式下 persistentQuestionStateMap 合并）。`loadProgress()`（从 Flow 恢复进度，含 questionStateMap 恢复、fallbackAnswerTime 补偿、随机模式 smartIndex 计算）。`enqueueSupplementaryRepositoryLoads()`（并行触发 4 个分析载入器：`loadAnalysisFromRepository/loadSparkAnalysisFromRepository/loadBaiduAnalysisFromRepository/loadNotesFromRepository`）。`loadWrongQuestions()`/`loadFavoriteQuestions()`（从错题本/收藏载入会话，含 Fill 签名升级、记忆模式初始化、智能顺序恢复）。`clearProgress()` |

### 其他 Coordinator

| 文件 | 行数 | 功能描述 |
|------|------|----------|
| **PracticeSubmitCoordinator.kt** | ~70 | **提交流程** — `submitMultiSelect()`（多选题提交：记录错题→触发延迟→刷新记忆池→判断全答案模式推进→`onNextQuestion()`）。`submitTextAnswer()`（文本回答提交，同样含延迟和自动跳转逻辑）。协调错误记录、历史记录、答题后自动导航和 quizEnd 判定 |
| **PracticeInteractionCoordinator.kt** | ~140 | **用户交互** — `answerQuestion()`（单选直接提交+标记showResult）、`selectSingleOption()`（仅选中不提交）、`toggleOption()`（多选切换）、`textAnswerChanged()`（文本输入更新）、`toggleShowResult()`/`showAnswer()`（显示答案，`showAnswer`含Fill答案保留逻辑）、`reopenQuestionForPendingRetry()`/`reopenQuestionForFullAnswerRetry()`（重开题目：清除状态并应用最新Fill设置） |
| **PracticeArtifactCoordinator.kt** | ~180 | **分析产物管理** — 管理三种 AI 分析（DeepSeek/Spark/Baidu）的增删改。`updateAnalysis()`/`updateSparkAnalysis()`/`updateBaiduAnalysis()`（同步更新 `_sessionState` 和持久化存储）。`appendNote()`（Mutex 保护的笔记追加，防并发冲突）。`removeAnalysis()`（清除分析并同步持久化）。`getNote()`/`getAnalysis()`/`getSparkAnalysis()`/`getBaiduAnalysis()`（读取分析） |
| **PracticeEditorCoordinator.kt** | ~250 | **题目编辑器** — `buildEditableQuestion()`（从当前题目或所有源题目构建可编辑对象）。`commitEdit()`（提交编辑：保存到仓库→从仓库重新载入→更新sessionState）。`applyFillTransformForEdit()`（对编辑后的题目应用Fill变换）。管理 `editedQuestionSnapshotMap` 快照缓存，支持 `clearAllFillEditsForCurrentFile()` |

### ui-common 答题卡管道

| 文件 | 功能描述 |
|------|----------|
| **AnswerCardDisplayInfoPipeline.kt** | 词条序号；全答轮次标签 `1①`（圈号） |
| **AnswerCardEntryGrouping.kt** | 按词条 order 分组题目 index |
| **AnswerCardEntryCompactLayout.kt** | 紧凑答题卡行：折叠 `1` + 展开 `1①` `1②`；状态聚合 |
| **AnswerCardEntryGridLines.kt** | 词条 5 列行；展开分题号行带 `anchorColumn` / `startColumn` |
| **AnswerCardRoundLinePlacement.kt** | 分题号以词条列为中心向两侧排；溢出 chunked 每行仍居中 |
| **AnswerCardRoundCell.kt** | 分题号格：primary 字色 + 当前题边框 |
| **AnswerCardEntryCell.kt** | 词条格：单击展开、双击跳转；底部 ExpandMore |
| **AnswerCardExpandIndicator.kt** | 12dp 展开/折叠箭头（列表内静态旋转，无动画） |
| **AnswerCardCompactEntryGrid.kt** | LazyColumn 分行：词条行 / 分题号行；`contentType` + hoist `statusColors` |
| **AnswerCardListDialogShell.kt** | 答题卡弹窗壳：`AppScrollBottomSheet` + 高度约束 Box（替代 Dialog） |
| **QuestionNavigationControls.kt** | 底栏上一题/提交/下一题；箭头与提交图标均支持 `combinedClickable` 双击 |
| **ScreenSafeInsets.kt** | `Modifier.screenTopSafePadding()`（轻量顶栏 padding） |
| **ScreenSafeScaffold.kt** | 答题/设置页统一 `WindowInsets.safeDrawing` Scaffold 容器 |
| **ArtifactFullscreenShell.kt** | 笔记/AI 全屏页壳：safeDrawing + 顶栏/底栏 action 槽 |
| **AnswerCardCell.kt** | 单格 UI；`answerCardStatusColors()` 带 `remember` |
| **AnswerCardGrid.kt** | 静态 5 列网格（非 Lazy，避免与外层 LazyColumn 嵌套） |
| **AnswerCardDialogContent.kt** | 全答→紧凑网格；否则题型折叠 |
| **AnswerCardTypeLabels.kt** | 题型分组标题 |
| **CollapsibleAnswerCardSection.kt** | 可折叠区块 + `AnswerCardGrid` |

### ui-common 胶囊步进器管道

| 文件 | 功能描述 |
|------|----------|
| **CapsuleStepperDefaults.kt** | 胶囊步进器统一尺寸（130×40dp） |
| **StepperInputParsePipeline.kt** | 数字输入过滤 + clamp（无状态） |
| **StepperAnimatedValue.kt** | 加减位移动效（上/下滑入淡出） |
| **StepperScoreRangePipeline.kt** | 分值区间 min/max 归一化（无状态） |
| **CapsuleStepperInput.kt** | M3 全圆角胶囊步进器 + `BasicTextField` 编辑态；可选 `contentDescription` |

### ui-common 设计语言（design/）

| 文件 | 功能描述 |
|------|----------|
| **AppSpacing.kt** | 8dp 网格间距：xs=4 / sm=8 / md=16 / lg=24 / xl=32 |
| **AppCard.kt** | 统一 ElevatedCard：12dp 圆角、1dp elevation、16dp 内边距；浅色答题容器显式使用兼容色 `#F0F0F2`，避免 Material3 默认 token 升级导致色偏 |
| **AppTopBar.kt** | 标准 M3 TopAppBar；可选返回 + actions |
| **AppCenterAlignedTopBar.kt** | 通用居中 TopBar（设置等页复用；答题页改 `PracticeExamTopBarShell`） |
| **PracticeExamTopBarMetrics.kt** | 答题顶栏尺寸（48dp 高 / 36dp 图标） |
| **PracticeExamTopBarShell.kt** | `SpaceEvenly` 六槽顶栏壳（← ✨ 计时 ★ 📝 ⋮） |
| **PracticeExamTimerPipeline.kt** | 无状态：答题顶栏计时 `MM:SS` |
| **SessionReadingSectionTokens.kt** | 会话区护眼色 token（结果/解析/笔记/AI） |
| **SessionReadingSectionColorPipeline.kt** | 无状态：分区 container/content |
| **SessionReadingAnswerFeedbackPipeline.kt** | 无状态：正误结果 muted 配色 |
| **ReadingCollapsibleSection.kt** | 会话阅读区折叠壳：点击展开/收起 + 展开态折叠按钮 |
| **AnswerResultPreviewPipeline.kt** | 无状态：`resolveAnswerResultPreviewLine` 单行预览截断 |
| **QuestionSessionActionRow.kt** | 题下操作行：左重答该题 / 中复制 / 右重答错题 |
| **QuestionCopyActionRow.kt** | 薄封装 → `QuestionSessionActionRow`（仅复制） |
| **AppContentText.kt** | 可缩放正文（LocalFontSize + LocalFontFamily） |
| **AppScrollBottomSheet.kt** | 可滚动 ModalBottomSheet 内容壳 |
| **AppScrollBottomSheetDefaults.kt** | Sheet 内列表最大高度常量（400.dp） |
| **AppEmptyState.kt** | 居中空状态；`AppEmptyStateInline` 供列表/Sheet 内嵌 |
| **AppLoadingOverlay.kt** | 全屏 loading 遮罩 + AppCard 内容槽 |
| **AppLoadingIndicator.kt** | 40dp 主色 spinner；`AppLoadingContent` 居中加载 + 可选文案 |
| **AnalysisSectionTone.kt** | 解析/笔记/AI 区块语义枚举 |
| **AnalysisSectionColorPipeline.kt** | 委托 `sessionReadingSectionColors(tone)` |
| **AnswerFeedbackColorPipeline.kt** | 委托 `sessionReadingAnswerFeedbackColors()` |
| **InlineBlankEditColorPipeline.kt** | `inlineBlankEditColors()` → 内联填空编辑下划线 + 光标色 |
| **AnswerChoiceTonePipeline.kt** | 无状态：选项/答题卡正误/已选 → `AnswerChoiceTone` |
| **AnswerChoiceColorPipeline.kt** | `answerChoicePalette()` + `colorFor(tone)` |
| **AnswerChoiceCorrectColorPipeline.kt** | 答对/答错选项容器 token（答对委托 Highlight） |
| **AnswerCorrectHighlightColorPipeline.kt** | Cursor diff 新增行答对 container/content（单一数据源） |
| **QuestionSessionChromeLayout.kt** | Box 锚定顶栏/底栏；scroll 居中填充 |
| **QuestionSessionBottomNavMetrics.kt** | 底栏高度 token（64dp） |
| **QuestionSessionChromeInsetsPipeline.kt** | scroll 区 top/bottom inset |
| **QuestionSessionImeScrollSpacer.kt** | scroll 末尾 IME 可滚占位 |
| **QuestionSessionBodyScroll.kt** | 中间 scroll；无 layout IME padding |
| **QuestionCardHeaderRow.kt** | 题目 Card 顶行：题型 + 进度 + 紧凑列表 Icon |
| **QuestionSessionHeader.kt** | 练习/考试共用：进度条 + Header Row + extraContent |
| **QuestionCopyActionRow.kt** | 薄封装 → `QuestionSessionActionRow`（仅复制） |
| **AppTopBarIconButton.kt** | 顶栏 IconButton；默认 40dp，答题顶栏可传 36dp |
| **PracticeExamTopBar.kt** | 练习/考试顶栏：`← ✨ 计时 ★ 📝 ⋮` 均分整行 |
| **PracticeExamAiMenuAction.kt** | AI 下拉动作枚举 |
| **PracticeExamAiMenuIconPipeline.kt** | 无状态：AI 菜单项 → Material Icon |
| **PracticeExamAiDropdown.kt** | AI 解析 DropdownMenu（DeepSeek + Spark，带图标） |
| **QuestionTypographyStepPipeline.kt** | 无状态：排版 step ↔ float 值 |
| **QuestionTypographyStepperRow.kt** | 排版 Sheet 步进器行 |
| **QuestionTypographySheet.kt** | 排版设置 BottomSheet |

### ui-common AI 对话 UI（model + design + component）

| 文件 | 功能描述 |
|------|----------|
| **AiChatMessageRole.kt** | User / Assistant 枚举 |
| **AiChatMessage.kt** | 单条气泡展示模型 |
| **AiChatTurn.kt** | 单轮 user→assistant 对 |
| **AiChatTurnFlattenPipeline.kt** | 多轮 → 气泡序列（无状态） |
| **AiChatSingleTurnPipeline.kt** | 单轮问答 → 两条气泡（无状态） |
| **AiChatBubbleColorPipeline.kt** | 角色 → 容器/文字色（无状态） |
| **AiChatScrollTargetPipeline.kt** | LazyColumn 滚到底部索引（无状态） |
| **AiChatSaveGatePipeline.kt** | 返回键是否弹保存确认（无状态） |
| **AiChatBubbleLayoutPipeline.kt** | Gemini 模式 assistant 全宽 / user 宽度（无状态） |
| **AiChatSendEnabledPipeline.kt** | 发送按钮可点判定（无状态） |
| **AiChatPromptDesignTokens.kt** | Gemini prompt sheet 尺寸 token |
| **AiChatBubble.kt** | 消息展示；Gemini 模式 assistant 无气泡 |
| **AiChatTypingBubble.kt** | 解析中占位（Gemini 扁平行） |
| **AiChatMessageList.kt** | LazyColumn + 自动滚底 |
| **AiChatPromptField.kt** | 圆角 BasicTextField 输入容器 |
| **AiChatPromptSendButton.kt** | 圆形 FilledIconButton 发送 |
| **AiChatPromptSheet.kt** | 底部 prompt sheet（顶部分割 + field + send） |
| **AiChatInputBar.kt** | 薄封装 → `AiChatPromptSheet` |
| **AiChatConversationLayout.kt** | Scaffold：消息区 + bottomBar prompt sheet |

### app AI 提问页（presentation/screen/ai）

| 文件 | 功能描述 |
|------|----------|
| **DeepSeekAskChatTurnMapPipeline.kt** | DeepSeekChatTurn → AiChatTurn |
| **AiAskFontMenu.kt** | 提问页字号 DropdownMenu |
| **AiAskSaveConfirmDialog.kt** | 退出保存确认 AlertDialog |
| **DeepSeekAskScreen.kt** | DeepSeek 多轮对话 UI（`chatTurns` 驱动） |
| **SparkAskScreen.kt** / **BaiduAskScreen.kt** | 单轮对话 UI（`AiChatSingleTurnPipeline`） |

### app 库页 / 结果页管道

| 文件 | 功能描述 |
|------|----------|
| **LibraryQuestionDetailScreen.kt** | 错题本/收藏库详情：AppTopBar + AppCard 列表 + bottomBar 主操作 |
| **ScopedQuestionLibraryScreen.kt** | 作用域题库列表：AppTopBar（根/文件夹）+ HomeFileList |
| **ResultScreen.kt** | 练习/考试结果：AppTopBar + AppCard 统计 + 历史走势 + 历史 Sheet |
| **ResultHistoryLinePipeline.kt** | 无状态：历史记录行文本格式化 |
| **ResultHistorySheet.kt** | 历史成绩 ModalBottomSheet 列表（`heightIn` 限高） |
| **ResultStatColorPipeline.kt** | 无状态：结果页统计/图表主题色映射 |
| **FileStatColorPipeline.kt** | 无状态：首页文件卡统计色（题数/错题/收藏/进度） |
| **FileCardTonePipeline.kt** | 无状态：文件卡题型 → `FileCardTone` |
| **FileCardColorPipeline.kt** | `fileCardPalette(tone)` → M3 容器/边框色 |

### Practice navigation 管道（feature-practice）

| 文件 | 行数 | 功能描述 |
|------|------|----------|
| **NavigationHistory.kt** | — | `Active.orderedIndices` 冻结；左/右滑 position 步进；`preferSnapshot` |
| **PracticeAnsweredBrowseNavigation.kt** | ~60 | `resolveOlder/NewerHistoryPosition` 纯 position 管道 |
| **PracticeAnsweredBrowseNavigation.kt** | ~48 | 练习右滑历史与 domain `AnsweredBrowseOrder` 衔接；记忆轮优先级；`navigateReadOnly` |
| **AnsweredHistoryForwardResult.kt** | — | 历史左滑结果：`Navigated` / `AtLatestAnswered` / `NotInHistory` |
| **AnsweredHistoryBackwardResult.kt** | — | 历史右滑结果：`Navigated` / `AtOldestAnswered` / `NoMoreHistory` |
| **NavigationController.kt** | — | 编排 next/prev/icon/skip；索引变更 `scheduleNavigationSave`；`skipToUnansweredSource` 委托 `PracticeFullAnswerUnansweredSourceNavigation` |

### Exam 系列已提取组件

| 文件 | 行数 | 功能描述 |
|------|------|----------|
| **ExamState.kt** | — | 考试状态数据类定义 |
| **ExamFillTransform.kt** | 81 | 考试填空题变换逻辑（委托 `ExamFillConfigPipeline`） |
| **ExamFillConfigPipeline.kt** | ~90 | 考试 Fill 无状态管道：读取配置、签名、应用 `transformQuestionVariantsForFillSettings`（含全答轮次顺序） |
| **ExamFullAnswerModeActivePipeline.kt** | ~12 | 全答模式生效判定：FULL_ANSWER 且会话含 inline-blank 题；协调器与 UI 单一数据流 |
| **ExamFullAnswerNavigation.kt** | ~300 | 考试全答导航纯函数：pending 判定、同源轮次、顺序下一题；`resolveCandidateIndices` 轮次池优先 |
| **ExamFullAnswerRoundPoolPipeline.kt** | ~15 | 全答轮次题池：同轮次跨词条索引 |
| **ExamFullAnswerRoundUnansweredPipeline.kt** | ~30 | 轮次池未作答槽位（`!showResult`） |
| **ExamFullAnswerRoundNavigablePipeline.kt** | ~35 | 轮次池可单击导航目标 |
| **ExamFullAnswerRoundCompletePipeline.kt** | ~20 | 轮次池全部已作答（`showResult`） |
| **ExamFullAnswerIconNavigation.kt** | ~45 | 全答底栏**单击**：在 navigable 轮次池内上下题 |
| **ExamFullAnswerIconRetryPipeline.kt** | ~30 | 须全对答错后单击重开 |
| **ExamFullAnswerReopenPipeline.kt** | ~40 | 重开单题作答态（填空保留已对空） |
| **ExamReviewSwipePipeline.kt** | ~40 | 答题详情滑动：已答时间序更旧/更新 |
| **ExamAISyncEffects.kt** | 52 | 考试 AI 分析同步副作用处理，注入 4 个依赖 |
| **ExamArtifactCoordinator.kt** | — | 考试分析产物管理（对应 Practice 的 PracticeArtifactCoordinator） |
| **ExamProgressCoordinator.kt** | — | 考试进度持久化协调 |
| **ExamNavigationHelper.kt** | 130 | 考试导航辅助，状态构建和导航逻辑，1个依赖 |
| **ExamNavigationCoordinator.kt** | — | `mustStayInRoundPool` 阻断跨词条；索引变更 `scheduleNavigationSave` |
| **ExamLoadDelegate.kt** | 182 | 考试题目加载；新轮次走 `ExamQuestionOrderPipeline`；复盘 `preserveFinishedProgress` |
| **ExamMemoryModeEngine.kt** | 48 | 考试记忆轮次引擎，1个依赖 |
| **ExamAnswerRules.kt** | 20 | 考试答案判断规则，0依赖 |
| **ExamEndFlow.kt** | 25 | 考试结束流程处理，1个依赖 |
| **ExamDialogState.kt** | 24 | 考试对话框状态管理，0依赖 |
| **ExamFontController.kt** | 62 | 考试字体/行距/字间距读写与持久化（`examFontSize`/`examLineSpacing`/`examLetterSpacing`） |
| **ExamSubmitFlow.kt** | 14 | 交卷入口无状态判定（未作答退出 / 弹出交卷确认） |
| **ExamQuestionCountPolicy.kt** | ~25 | 考试题数截断；「全部」时复用门槛 |
| **ExamSavedOrderMatchPipeline.kt** | ~12 | 已保存题序与期望序匹配（随机/顺序） |
| **ExamSourceQuestionPipeline.kt** | ~15 | 变体题号 → 源题号；已答/上轮源题集合 |
| **ExamRoundCompletePipeline.kt** | ~25 | 轮次结束：finished 或 fixedOrder 变体全 showResult |
| **ExamRoundLoadLog.kt** | ~30 | 轮次加载/恢复/保存日志（`ExamRoundLoad`） |
| **ExamQuestionOrderPipeline.kt** | ~70 | 新轮次未答池出题；`recyclePool` 避开上一轮 |
| **ExamQuestionStateMapPipeline.kt** | ~12 | 跨轮次 `questionStateMap` 合并 |
| **ExamSessionRestorePipeline.kt** | ~35 | 是否/如何从 map 回填作答；新轮次保持空白题面 |
| **ExamPendingQuestionPipeline.kt** | ~20 | 考试未作答 pending 判定（`selectedOptions` 空） |
| **ExamUnansweredNavigation.kt** | ~100 | 考试未答题导航池：随机全库 pending（除当前）；顺序按锚点 |
| **ExamSequentialNextPipeline.kt** | ~20 | 顺序考试下一目标：后 pending → 前 pending → 顺序下一格 |
| **ExamPostAnswerAdvancePipeline.kt** | ~15 | 单题作答后：仍有 pending → 继续；否则交卷确认 |
| **ExamEdgeSwipePipeline.kt** | ~20 | 答题区左滑末缘：可导航下一题 / 未作答退出 / 交卷确认 |
| **ExamSessionStats.kt** | 30 | 考试会话评分统计，2个依赖 |
| **ExamGestureNavigator.kt** | 14 | 考试手势导航状态，0依赖 |
| **ExamAutoAdvanceTimer.kt** | 36 | 考试自动前进计时器，1个依赖 |

### Settings 系列已提取组件

| 文件 | 功能描述 |
|------|----------|
| **ImportCoordinator.kt** | 文件/URI 导入协调器 |
| **FillQuestionFilterCoordinator.kt** | Fill 题目过滤协调器 |
| **FontSettingsCoordinator** | （在 SettingsViewModel 内嵌）字体设置协调器 |
| **JsonExportCoordinator** | JSON 导出协调器 |
| **ExcelExportCoordinator** | Excel 导出协调器 |

---

## 三、app/.../presentation/screen/ — ViewModel 层

| 文件 | 行数 | 注入依赖 | 功能描述 |
|------|------|----------|----------|
| **PracticeViewModel.kt** | ~724 | ~15（含6Coordinator+UseCases） | **练习核心 VM** — 复盘：`reviewBrowseSession` 箭头全量顺序；右滑 `reviewAnsweredSwipeOrder` 仅已答 |
| **ExamViewModel.kt** | ~415 | ~12 | **考试核心 VM** — 考试会话全生命周期。`fullAnswerModeActiveNow()` 经 `ExamFullAnswerModeActivePipeline` 统一全答判定；复盘：`SessionReviewPresentation` → `ReviewBrowseSession`（图标）+ `ReviewAnsweredSwipePipeline`（滑动） |
| **HomeViewModel.kt** | — | 10 | **首页 VM** — 文件列表展示、练习进度聚合、设置持久化。注入 `GetQuestionsUseCase/GetFileStatisticsUseCase/ClearPracticeProgressUseCase/ClearExamProgressUseCase/ClearPracticeProgressByFileNameUseCase/ClearExamProgressByFileNameUseCase/SavePracticeProgressUseCase/GetAllPracticeProgressFlowUseCase/GetPracticeProgressFlowUseCase`。暴露状态：`fileNames/practiceProgress/messageResult/fileStatistics/storedPrefs/homeContentReady` |
| **SettingsViewModel.kt** | ~416 | 8+ | **设置 VM** — 字体设置、导入导出、Fill 配置的薄委托层。注入 `QuestionRepository/WrongBookRepository/HistoryRepository/FavoriteQuestionRepository/QuestionAnalysisRepository/QuestionAskRepository/QuestionNoteRepository` 和 7 个协调器（`FontSettingsCoordinator/FillQuestionFilterCoordinator/ImportCoordinator/JsonExportCoordinator/ExcelExportCoordinator`）。暴露状态：`isLoading/progress` |
| **DeepSeekViewModel.kt** | — | — | DeepSeek AI 解析 VM — 单轮 `analyze` |
| **DeepSeekAskPersistFormatPipeline.kt** | 多轮编解码；`---` 分隔兼容 |
| **DeepSeekAskDisplayPipeline.kt** | 多轮 assistant 拼接展示 |
| **DeepSeekAskSavePipeline.kt** | 落库展示文本（追加后的完整答案） |
| **DeepSeekAskPersistPipeline.kt** | 加载源解析；兼容旧笔记/`question_ask` |
| **DeepSeekAskViewModel.kt** | 多轮 `chatTurns` + `errorMessage` + `saveAndWait` 持久化 |
| **SparkViewModel.kt** | — | — | 讯飞星火 AI 问答 VM |
| **SparkAskViewModel.kt** | — | — | 星火提问专用 VM |
| **BaiduAskViewModel.kt** | — | — | 百度千帆提问 VM |
| **WrongBookViewModel.kt** | — | — | 错题本 VM — `libraryCatalog` 轻量列表；`ensureFullListLoaded` 按需全量 |
| **FavoriteViewModel.kt** | — | — | 收藏夹 VM — 同上 catalog 驱动列表 |
| **FileFolderViewModel.kt** | — | — | 文件/文件夹管理 VM — 文件移动、文件夹创建 |
| **DragDropViewModel.kt** | — | — | 拖拽导入 VM — 处理屏幕底部拖放区状态 |

---

## 四、app/.../presentation/screen/ — Screen（Compose UI）层

| 文件 | 行数 | 功能描述 |
|------|------|----------|
| **PracticeScreen.kt** | ~390 | **练习入口（仅拼装）** — 子 UI 见 `components/PracticeScreen*`；退出/交卷见 `PracticeSessionExit*Pipeline`；LOC 门禁 `scripts/check-practice-screen-loc.ps1` |
| **ExamScreenContent.kt** | — | **考试界面内容** — 题面/顶栏/导航/交卷；底栏与自动跳转按全库 pending（非答题卡末索引）；复盘模式 browse 管道 |
| **HomeScreen.kt** | ~399 | **首页界面** — 文件卡片网格/Flex 布局、拖拽接收区；根目录无题库时 `HomeEmptyLibraryPanel` |
| **HomeFileTypeVisualPipeline.kt** | ~140 | **题库语义视觉管道** — 文件名显式题型 → 来源扩展名 → `FileStatistics` 的优先级解析；输出 14 类稳定 kind、Material 图标和渐变色 |
| **HomeQuestionBankCard.kt** | ~150 | **首页题库视觉卡片** — 根据真实题型统计缓存语义图标；Column/Grid 共用；轻量 Row/Box 进度实现 |
| **HomePerformanceLog.kt** | — | Debug-only 首页冷启动/滚动帧与卡片进入日志；Performance/Release 构建不输出 |
| **HomeLibraryEmptyPipeline.kt** | — | 无状态：根目录空列表 → `HomeLibraryEmptyReason` |
| **HomeEmptyLibraryPanel.kt** | — | 首页无题库引导空态（`AppEmptyState`） |
| **HomeNavigationDrawer.kt** | **主页抽屉壳** — M3 默认 scrim + `BackHandler` |
| **HomeResumeDeferGate.kt** | ~18 | pop 回主页首帧后再挂重交互 Modifier |
| **HomeStartQuizSheet.kt** | ~85 | 首页文件「开始练习/考试」BottomSheet（AppSpacing + M3 Button） |
| **HomeActionOverlays.kt** | — | Loading/拖拽/Sheet/对话框叠加层；Sheet 委托 `HomeStartQuizSheet` |
| **SettingsScreen.kt** | ~380 | **设置主页** — TopAppBar + 外观/答题/数据管理 Card 分组；填空题设置跳转子页 `settings/fill` |
| **FillSettingsScreen.kt** | ~100 | **填空题设置子页** — `SettingsFillPanelContent` + 详细说明折叠开关 |
| **ResultScreen.kt** | ~220 | **结果界面** — AppTopBar + AppCard 统计；M3 默认 Button；跳转 review |
| **FillAnswerRoundLabel.kt** | — | 全答多轮标签（练习/考试共用） |
| ~~**AnswerReviewScreen.kt**~~ | — | 已删除；复盘改复用 `ExamScreen`/`PracticeScreen` + `isReviewMode` |
| **QuestionScreen.kt** | — | **题目详情界面** — 单题浏览/编辑 |
| **WrongBookScreen.kt** | ~70 | **错题本** — 列表 `ScopedQuestionLibraryScreen`；详情 `LibraryQuestionDetailScreen` |
| **FavoriteScreen.kt** | ~70 | **收藏库** — 同上结构 |

---

## 五、app/.../presentation/screen/components/ — 共享 UI 组件

| 文件 | 行数 | 功能描述 |
|------|------|----------|
| **InlineBlankQuestionContent.kt** | 536 | **填空题行内渲染** — 将填空题内容解析为文本+空白混合展示，支持编辑状态和显示状态 |
| **InlineBlankTokenizer.kt** | — | **填空题内容分词器** — 解析文本段/空白段；`InlineBlankVisualTransformation` 接收主题 `blankEditColor` |
| **RichTextParser.kt** | 252 | **富文本解析器** — 解析 Markdown/富文本标记为 Compose AnnotatedString，已从 892 行精简 72% |
| **OptimizedFileCard.kt** | 437 | **首页文件卡片** — 优化的文件卡片组件，支持点击/长按/拖拽 |
| **HomeFileListContainer.kt** | — | **首页文件列表容器** — 管理文件卡片的布局和滚动 |
| **HomeDragDock.kt** | — | **首页拖拽停靠区** — 底部拖放目标区域 |
| **HomeDragHandler.kt** | — | **首页拖拽处理** — 处理拖拽手势逻辑 |
| **HomeDraggingFileOverlay.kt** | — | **首页拖拽覆盖层** — 拖拽时的半透明覆盖效果 |
| **HomeTopBar.kt** | — | **首页顶栏** — 委托 `AppTopBar`（Menu 导航 + 设置 action + scrollBehavior） |
| **AnswerResultRow.kt** | — | **答题结果行** — `ReadingCollapsibleSection` 折叠正误区；展开 `ExamAnswerResultBody` |
| **PracticeNoteBox.kt** | — | **练习笔记框** — 当前题目的笔记编辑区域 |
| **PracticeExplanationBox.kt** | — | **练习解释面板** — 题目的正确解释展示 |
| **TextAnswerQuestionContent.kt** | — | **文本回答渲染** — 纯文本/填空/简答题的题面展示 |
| **EditCurrentQuestionDialog.kt** | — | **编辑题目对话框** — 编辑当前题目的内容/答案/选项 |
| **DrawingAnswerImages.kt** | — | **绘图答案图片** — 渲染题目中的图片/绘图类答案 |
| **ZoomableImageViewer.kt** | — | **可缩放图片查看器** — 支持双指缩放/拖拽的图片查看 |
| ~~**ExamTopBar.kt**~~ | — | 已删除；统一为 `ui-common/design/PracticeExamTopBar.kt` |
| **ExamHeader.kt** | — | 委托 `QuestionSessionHeader`（进度 Card + Header Row） |
| **ExamQuestionBody.kt** | — | **考试题面组件** — 考试界面的题面渲染 |
| **ExamOptionsList.kt** | — | **考试选项列表** — 考试界面的单选题/多选题选项 |
| **ExamDialogs.kt** | — | **考试对话框集** — 提交考试/退出考试等对话框 |
| **ExamHeader.kt** | — | **考试状态头** — 显示进度条、正确率、题号 |
| **ExamAnalysisArea.kt** | — | **考试分析面板** — 显示 AI 分析 |
| **ExamAnswerResultSummaryPipeline.kt** | — | 无状态：`resolveExamAnswerResultWrongToken` 正误摘要答案 token |
| **ExamQuestionRetryPipeline.kt** | — | 批改后整题重答 / 填空保留已对空 |
| **ExamIconUnansweredNavigationPipeline.kt** | — | 考试底栏箭头跨词条 fallback（委托 core strategy） |

---

## 六、app/.../presentation/screen/practice/ — 练习专用组件

| 文件 | 功能描述 |
|------|----------|
| **PracticeDialogsHost.kt** | 练习对话框宿主 — 管理退出/编辑/查看AI分析的对话框集合 |
| **PracticeFontSettingsMenu.kt** | 练习字体设置菜单 — 字号/字体族的快捷切换菜单 |
| **AnswerResultPreviewPipeline.kt** | 无状态：`resolveAnswerResultPreviewLine` 单行预览截断 |
| **PracticeResultSection.kt** | 练习结果区域 — `ReadingCollapsibleSection` 折叠正误区（重答在 `QuestionSessionActionRow`） |
| **PracticeIconUnansweredNavigationPipeline.kt** | 练习底栏箭头跨词条 fallback（委托 core strategy） |

---

## 七、app/.../presentation/screen/settings/ — 设置子UI模块（M3 重构 2026-06-28）

### 共享 UI 原子（单一职责 / 无状态）

| 文件 | 功能描述 |
|------|----------|
| **SettingsSectionHeader.kt** | 区域标题（labelLarge + primary） |
| **SettingsCardGroup.kt** | ElevatedCard 分组容器 + 内部分割线 |
| **SettingsUserText.kt** | 主标题/帮助文本（统一 -2sp 偏移） |
| **SettingsListSwitchRow.kt** | ListItem + trailing Switch |
| **SettingsListSliderRow.kt** | ListItem + Slider（字号/延迟） |
| **SettingsSegmentedChoiceRow.kt** | ListItem + SingleChoiceSegmentedButtonRow |
| **SettingsExpandableCardSection.kt** | 可展开 ListItem + AnimatedVisibility |
| **SettingsNavListItem.kt** | 跳转子页 ListItem（箭头 trailing） |
| **SettingsTopBar.kt** | TopAppBar（固定 titleLarge，不随用户字号缩放） |
| **SettingsStepperDisplay.kt** | `formatCountStepperDisplay` / `formatBlankCountDisplay` 公共格式化 |
| **SettingsImportProgressOverlay.kt** | 导入/导出 loading；薄封装 `AppLoadingOverlay` |
| **SettingsImportSnackbarMessages.kt** | Context → 导入 Snackbar 文案 |
| **SettingsMemoryCardSection.kt** | 记忆模式可展开区：Switch + Segmented 错题/池模式 |
| **SettingsBottomSheets.kt** | 导出选文件 / 导入题库入口 ModalBottomSheet |

### 分组面板

| 文件 | 功能描述 |
|------|----------|
| **SettingsAppearanceCard.kt** | 外观 Card：Slider 字号、Segmented 字体、Switch 深色/声音 |
| **SettingsAnswerSettingsCard.kt** | 答题 Card：练习/考试/记忆可展开区 |
| **SettingsDataGroupCard.kt** | 数据管理 Card：FilledTonal 导入 + Outlined 导出并排 |
| **SettingsFillPanel.kt** | `SettingsFillPanelContent` 填空题详细配置（子页专用） |
| **SettingsFillTagClearChip.kt** | 标签筛选清除 `FilterChip`（与标签 Chip 同族） |
| **SettingsStepperRow.kt** | 设置行：左侧标签 + 右侧胶囊步进器（必填 contentDescription） |
| **SettingsScoreRangeStepperRow.kt** | 分值区间行：标签上置 + 等宽双步进器（min/max contentDescription） |
| **SettingsStoragePermissionDialog.kt** | 本地导入权限对话框 |

### 管道（无 Compose / 无状态）

| 文件 | 功能描述 |
|------|----------|
| **SettingsStepperAccessibilityPipeline.kt** | 步进器可见 label ↔ TalkBack contentDescription 对齐 |

| 文件 | 功能描述 |
|------|----------|
| **SettingsExportRequestPipeline.kt** | 单文件直接导出判定 + 输出文件名格式化 |
| **SettingsImportSnackbarPipeline.kt** | 导入结果 → Snackbar 文案 / 是否回主页 |

---

## 八、app/.../presentation/screen/questionbank/ — 题库组件

| 文件 | 功能描述 |
|------|----------|
| **QuestionBankDrawerWidthPipeline.kt** | 无状态：抽屉宽度 `min(85%, 屏宽-56dp, 320dp)` |
| **QuestionBankDrawerHeader.kt** | 抽屉标题行（56dp Row + 关闭） |
| **QuestionBankDrawerExpansion.kt** | 抽屉展开态 snapshot；搜索/浏览模式纯函数判定 |
| **QuestionBankDrawer.kt** | 主页左侧抽屉 — 文件夹/题库树、搜索过滤；`QuestionBankDrawerHeader` |
| **QuestionBankDrawerViewModel.kt** | 搜索管道 + `expansionSnapshot`（combine 单一数据流）；toggle 写 collapsed/expanded 集 |
| **QuestionBankDrawerRows.kt** | 题库抽屉行 — 文件夹/文件/搜索命中题目行 |
| **DrawerQuestionEditHost.kt** | 抽屉内题目编辑宿主 |

---

## 九、app/.../presentation/navigation/

| 文件 | 行数 | 功能描述 |
|------|------|----------|
| **AppNavHost.kt** | ~430 | **导航路由根** — 含 `settings` / `settings/fill` 子路由 |
| **AppNavHomePipeline.kt** | ~17 | `navigateFromHome` / `popBackToHome` 单一出口 |

---

## 十、data/ 层 — Room 数据库与实体

### AppDatabase

| 文件 | 功能描述 |
|------|----------|
| **AppDatabase.kt** | Room 数据库定义 — 12 个实体表（`QuestionEntity/WrongQuestionEntity/HistoryRecordEntity/ExamHistoryRecordEntity/FavoriteQuestionEntity/PracticeProgressEntity/ExamProgressEntity/QuestionAnalysisEntity/QuestionNoteEntity/QuestionAskEntity/FileFolderEntity/FolderEntity`），v25 schema，12 个抽象 DAO 方法，4 个 TypeConverter（`IntListConverter/BooleanListConverter/NestedIntListConverter/StringListConverter`） |

### 实体（Entity）

| 文件 | 功能描述 |
|------|----------|
| **QuestionEntity.kt** | 题目实体 — ID/类型/内容/选项/答案/文件名/来源等 |
| **WrongQuestionEntity.kt** | 错题实体 — 关联题目ID+文件名+错误次数 |
| **HistoryRecordEntity.kt** | 练习历史记录实体 |
| **ExamHistoryRecordEntity.kt** | 考试历史记录实体 |
| **FavoriteQuestionEntity.kt** | 收藏题目实体 |
| **PracticeProgressEntity.kt** | 练习进度实体 — 保存练习会话的完整快照 |
| **ExamProgressEntity.kt** | 考试进度实体 — 考试进度的完整快照 |
| **QuestionAnalysisEntity.kt** | 题目 AI 分析实体 |
| **QuestionNoteEntity.kt** | 题目笔记实体 |
| **QuestionAskEntity.kt** | 题目 AI 提问记录实体 |
| **FileFolderEntity.kt** | 文件/文件夹实体 |
| **FolderEntity.kt** | 文件夹实体 |

### 类型转换器（TypeConverter）

| 文件 | 功能描述 |
|------|----------|
| **IntListConverter.kt** | `List<Int>` ↔ String 转换 |
| **BooleanListConverter.kt** | `List<Boolean>` ↔ String 转换 |
| **NestedIntListConverter.kt** | `List<List<Int>>` ↔ String 转换 |
| **StringListConverter.kt** | `List<String>` ↔ String 转换 |
| **QuestionAnswerStateMapConverter.kt** | `Map<Int, QuestionAnswerState>` ↔ JSON String 转换 |
| **ExamQuestionStateMapConverter.kt** | 考试题目状态 Map ↔ JSON String 转换 |

### DAO（Data Access Object）

| 文件 | 功能描述 |
|------|----------|
| **QuestionDao.kt** | 题目 CRUD — 按ID查/按文件名查/全部/插入/批量插入/删除/更新 |
| **WrongQuestionDao.kt** | 错题 CRUD — 按ID查/按文件名查/全部/插入/删除 |
| **HistoryRecordDao.kt** | 练习历史 CRUD |
| **ExamHistoryRecordDao.kt** | 考试历史 CRUD |
| **FavoriteQuestionDao.kt** | 收藏 CRUD — 按ID查/按文件名查/全部/插入/删除 |
| **PracticeProgressDao.kt** | 练习进度读写 — Flow 监听/插入/更新/删除 |
| **ExamProgressDao.kt** | 考试进度读写 — Flow 监听/插入/更新/删除 |
| **QuestionAnalysisDao.kt** | AI 分析 CRUD — 按题目ID查/插入/更新/删除 |
| **QuestionNoteDao.kt** | 笔记 CRUD — 按题目ID查/插入/更新/删除 |
| **QuestionAskDao.kt** | AI 提问记录 CRUD |
| **FileFolderDao.kt** | 文件/文件夹管理 — 路径查询/插入/删除/移动 |
| **FolderDao.kt** | 文件夹管理 |

---

## 十一、data/ 层 — 仓库实现（Repository）

| 文件 | 行数 | 注入依赖 | 功能描述 |
|------|------|----------|----------|
| **QuestionRepositoryImpl.kt** | ~306（已从1619精简） | 11 DAO + 3 Sub-Repo + 7 Parser | **题库仓库核心** — 实现 `QuestionRepository` 接口。统一导入入口：`importFromFilesWithOrigin()` 调度 5 种格式解析器（TXT/DOCX/SQLite/JSON/Excel）。题目 CRUD、批量操作、文件列表管理、Markdown 清理预览。已提取 7 个 Parser/Extractor（1619→306行 repo + ~1190行 extractor），注入依赖从 16 个降至 ~14（含 sub-repo） |
| **ScopedLibraryCatalogPipeline.kt** | ~55 | 0 | 错题/收藏目录聚合 | SQL 计数构建 `LibraryCatalog` |
| **WrongBookRepositoryImpl.kt** | 394 | 4 | 错题本仓库 — `observeLibraryCatalog`；`getAll` 按 ID 批量查题 |
| **FavoriteQuestionRepositoryImpl.kt** | 273 | — | 收藏仓库 — 收藏增删查、按文件名/全部获取 |
| **PracticeProgressRepositoryImpl.kt** | — | — | 练习进度持久化 — Flow 监听进度变化、保存/更新/按ID删除/按文件名批量清除 |
| **ExamProgressRepositoryImpl.kt** | — | — | 考试进度持久化 — 同上，面向考试进度 |
| **ExamHistoryRepositoryImpl.kt** | — | — | 考试历史持久化 — 考试记录 CRUD |
| **HistoryRepositoryImpl.kt** | — | — | 练习历史持久化 — 练习记录 CRUD |
| **QuestionNoteRepositoryImpl.kt** | — | — | 题目笔记仓库 — 按ID查/保存/删除 |
| **QuestionAskRepositoryImpl.kt** | — | — | AI 提问记录仓库 |
| **QuestionAnalysisRepositoryImpl.kt** | — | — | AI 分析仓库 |
| **FileFolderRepositoryImpl.kt** | — | — | 文件/文件夹仓库 — 路径管理、移动、排序 |

---

## 十二、data/ 层 — 文件解析器（Parser）

| 文件 | 功能描述 |
|------|----------|
| **QuestionFileParser.kt** | **解析器接口** — 定义两个接口：`QuestionFileParser`（`File → List<ImportedQuestionPayload>`，含元数据）和 `SimpleQuestionFileParser`（`File → List<Question>`，纯题目） |
| **TxtQuestionParser.kt** | TXT 格式题库解析器 — 解析纯文本格式的题目文件 |
| **DocxQuestionParser.kt** | DOCX 格式题库解析器 — 使用 Apache POI 解析 Word 文档中的题目 |
| **SqliteQuestionParser.kt** | SQLite 题库解析器 — 从 .db/.sqlite 文件读取题目表 |
| **JsonQuestionParser.kt** | JSON 题库解析器 — 解析 JSON 格式的题目数据 |
| **ExcelQuestionParser.kt** | Excel 题库解析器 — 使用 Apache POI 解析 .xlsx 表格中的题目 |
| **ParseTypes.kt** | 解析类型定义 — ImportedQuestionPayload、解析结果等类型 |

---

## 十三、data/ 层 — 其他支持文件

| 文件 | 功能描述 |
|------|----------|
| **QuestionDataInitializer.kt** | 数据初始化器 — 首次启动时从 assets 加载预制题库 |
| **MarkdownNormalizer.kt** | Markdown 格式归一化 — 统一处理不同来源的 Markdown 标记差异 |
| **MetadataManager.kt** | 文件元数据管理 — 文件来源、格式、导入时间等 |
| **ImportExceptions.kt** | 导入异常定义 — 解析失败、格式不支持等自定义异常 |
| **ExportConstants.kt** | 导出常量定义 |
| **AtomicArticleConverter.kt** | 原子题库文章转换器 — 将原子题库格式的 Text 转为题目 |
| **Mappers.kt** | 数据映射器 — Entity ↔ Domain 模型转换函数 |
| **ExamHistoryMappers.kt** | 考试历史映射器 — 考试历史 Entity ↔ Domain 模型转换 |
| **DataModule.kt** | Hilt DI 模块 — 提供 DAO/数据库/解析器的绑定 |
| **LocalizedException.kt** | （在 data/domain/ 中）本地化异常类 |
| **IOConstants.kt** | （在 data/domain/ 中）IO 常量定义 |

---

## 十四、domain/ 层 — 领域模型

| 文件 | 功能描述 |
|------|----------|
| **Question.kt** | **题目领域模型** — 核心模型：ID/类型/内容/选项/答案/解析/文件名/来源/额外信息 |
| **PracticeSessionState.kt** | **练习会话状态** — 统一状态容器：`questionsWithState: List<QuestionWithState>`、`currentIndex: Int`、`answeredIndices: List<Int>`、`progressLoaded: Boolean`、`sessionStartTime: Long`。所有 Coordinator 和 VM 共享此单一状态流 |
| **QuestionWithState.kt** | **带运行时状态的题目** — `question: Question` + `selectedOptions: List<Int>` + `textAnswer: String` + `showResult: Boolean` + `isCorrect: Boolean?` + `analysis/sparkAnalysis/baiduAnalysis/note: String` + `sessionAnswerTime: Long` + `isAnswered: Boolean` |
| **QuestionAnswerState.kt** | **题目答案状态** — 持久化的答案快照：`questionId/selectedOptions/textAnswer/showResult/analysis/sparkAnalysis/baiduAnalysis/note/sessionAnswerTime/displayedQuestionContent/displayedQuestionAnswer` |
| **PracticeProgress.kt** | **练习进度实体** — `id/currentIndex/answeredList/selectedOptions/showResultList/analysisList/...` + `sessionId/timestamp/fixedQuestionOrder/questionStateMap` |
| **ExamProgress.kt** | **考试进度实体** — 面向考试的进度快照结构 |
| **WrongQuestion.kt** | **错题模型** — `question: Question` + `fileName: String` |
| **FavoriteQuestion.kt** | **收藏题目模型** |
| **HistoryRecord.kt** | **练习历史记录模型** |
| **ExamHistoryRecord.kt** | **考试历史记录模型** |
| **FileFolder.kt** | **文件/文件夹模型** |
| **ExportData.kt** | **导出数据模型** |
| **AnswerStatus.kt** | **答案状态枚举** — CORRECT/WRONG/UNANSWERED |

### domain/review — 已答浏览管道

| 文件 | 功能描述 |
|------|----------|
| **AnsweredBrowseOrder.kt** | 无状态排序：`hasAnswerContent`、`buildAnsweredIndicesByTimeDesc`、`buildReviewDisplayOrder`（已答时间倒序 + 未答置后） |
| **ReviewBrowseSession.kt** | 不可变 `displayOrder` + `position`；复盘箭头步进 |
| **ReviewAnsweredSwipePipeline.kt** | 复盘右滑：仅已答时间倒序；`resolveOlder/NewerIndex` |
| **SessionReviewPresentation.kt** | 复盘入口：`ReviewPresentation(questionsWithState, displayOrder)`，不重排列表，已答强制 `showResult` |
| **AnswerReviewOrder.kt** | 兼容委托 `AnsweredBrowseOrder` |

---

## 十五、domain/ 层 — 仓库接口（Repository Interfaces）

| 文件 | 功能描述 |
|------|----------|
| **QuestionRepository.kt** | 题库仓库接口 — `importFromFilesWithOrigin/getQuestions/getFiles/deleteByFileName/...` |
| **WrongBookRepository.kt** | 错题本仓库接口 |
| **FavoriteQuestionRepository.kt** | 收藏仓库接口 |
| **PracticeProgressRepository.kt** | 练习进度仓库接口 — Flow 监听 + 保存/删除 |
| **ExamProgressRepository.kt** | 考试进度仓库接口 |
| **HistoryRepository.kt** | 历史记录仓库接口 |
| **ExamHistoryRepository.kt** | 考试历史仓库接口 |
| **QuestionAnalysisRepository.kt** | AI 分析仓库接口 |
| **QuestionNoteRepository.kt** | 笔记仓库接口 |
| **QuestionAskRepository.kt** | AI 提问记录仓库接口 |
| **FileFolderRepository.kt** | 文件/文件夹仓库接口 |

---

## 十六、domain/ 层 — 工具类与UseCase

| 文件 | 功能描述 |
|------|----------|
| **FillQuestionTransformUtils.kt** (~360行) | **填空题变换工具** — `applyConfiguredFillQuestions()`/`transformQuestionForFillSettings()`/`transformQuestionVariantsForFillSettings()`（根据 Fill 生成模式变换题目）等填空题核心变换逻辑 |
| **FullAnswerMultiRoundSessionPipeline.kt** | 多轮全答会话判定（含第 2 轮及以上） |
| **FullAnswerIconNavigationStrategyPipeline.kt** | 多轮/单轮全答底栏箭头单击·双击策略 |
| **AtomicFullAnswerSessionPipeline.kt** | @deprecated → 委托 MultiRound |
| **AnswerUtils.kt** (~55行) | **答案比较工具** — `answerToOptionIndices()`（答案转为选项索引列表）、`isFillAnswerCorrect()`（填空题答案正确判断）、`resolveFillCorrectAnswer()`（解析填空题正确答案） |
| **MarkdownFormatNormalizer.kt** | Markdown 格式归一化 — 压缩段落拆分、孤立 `**`（含 `$...$**`）、孤立 code fence 行清理 |
| **QuestionTypes.kt** | 题目类型枚举和判断 — 选择题/填空题/判断题/简答题及其判断方法 |
| **GetQuestionsUseCase.kt** | 获取题目用例 |
| **QuestionUiModel.kt** | UI 模型 — 提供给 Compose 的展示层数据类 |

---

## 十七、app/ 层 — 其他重要文件

| 文件 | 功能描述 |
|------|----------|
| **FontSettingsDataStore.kt** (~374行) | 字体设置 DataStore — 字体族/字体大小/暗色模式等偏好设置的持久化存储 |
| **HomeProgressRedoHandler.kt** | 首页进度重置处理器 |
| **BaiduApiService.kt** | 百度千帆 API 服务 |
| **DeepSeekApiService.kt** | `analyze` 单轮；`chat(messages)` 多轮；`thinking: disabled` |
| **DeepSeekChatConfig.kt** | `API_URL`、`MODEL=deepseek-v4-flash`、system 提示词、采样参数、多轮纠错指引 |
| **DeepSeekChatTurn.kt** | 单轮 user/assistant 对 |
| **DeepSeekMultiTurnMessagesPipeline.kt** | 官方多轮 messages 拼接（system + history + user） |
| **DeepSeekAskFollowUpPipeline.kt** | 再次提问：改题用新内容，否则注入修正 follow-up |
| **DeepSeekChatMessages.kt** | 首轮 messages 委托 `DeepSeekMultiTurnMessagesPipeline` |
| **SparkApiService.kt** | 讯飞星火 API 服务 |

---

## 十八、架构关键指标总结

| 指标 | 数值 |
|------|------|
| 总 .kt 文件数 | ~225+ |
| 总代码行数 | ~26,276 |
| >1000 行文件数 | 6 |
| >500 行文件数 | 10 |
| God VM（原 >2000 行） | 2（PracticeVM/ExamVM，均已分解） |
| 最高注入依赖数 | 16（QuestionRepositoryImpl 分解前） |
| 空壳 feature 模块 | 4（feature-practice/feature-exam/ui-common/core） |
| Coordinator 提取完成 | Practice: 9/9, Exam: 7/7, Settings: 3/3 |
| 仍在红线区的文件 | PracticeSessionCoordinator(641行), PracticeNavigationCoordinator(617行) |
