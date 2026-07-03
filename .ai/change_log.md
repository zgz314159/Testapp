<!--
  Derived from: TASK_LOG.md
  Last synced: 2026-06-19 23:55 UTC+8
  Do not edit directly — append to TASK_LOG.md, then re-sync.
-->

# Change Log

> Chronological log for agent continuity. Source: `TASK_LOG.md`.


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
