<!--
  Derived from: TASK_LOG.md
  Last synced: 2026-06-19 23:55 UTC+8
  Do not edit directly — append to TASK_LOG.md, then re-sync.
-->

# Change Log

> Chronological log for agent continuity. Source: `TASK_LOG.md`.

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
