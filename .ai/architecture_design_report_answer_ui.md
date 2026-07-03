# 答题界面完整架构报告

## 1. 模块依赖关系

```
app (ExamScreen.kt, PracticeScreen.kt, ExamAISyncEffects.kt)
 └─► feature-exam / feature-practice (ViewModel + Coordinators + Content)
      └─► domain (Question, QuestionWithState, PracticeSessionState, QuestionTypes)
```

- `app/` — 入口 Composable，组合 ViewModel，注入外部状态（设置、AI ViewModel）
- `feature-exam/` / `feature-practice/` — ViewModel + 协调器 + UI Content 逻辑
- `domain/` — 纯数据模型，无平台依赖

---

## 2. 两个答题模式对比

| | Practice | Exam |
|---|---|---|
| **入口** | `PracticeScreen.kt` (app) | `ExamScreen.kt` (app) |
| **Content** | `PracticeScreen.kt` 内 Composable | `ExamScreenContent.kt` (feature-exam) |
| **ViewModel** | `PracticeViewModel` (feature-practice) | `ExamViewModel` (feature-exam) |
| **核心 State** | `MutableStateFlow<PracticeSessionState>` | 同 |
| **计时器** | `var autoJob: Job?` (local) | `ExamAutoAdvanceTimer` class |
| **导航** | `PracticeNavigationCoordinator` | `ExamNavigationCoordinator` |
| **批改** | 无（练习模式即时判题） | `ExamGradeCoordinator`（交卷） |
| **记忆模式** | 无 | `ExamMemoryModeCoordinator` + `ExamMemoryModeEngine` |

---

## 3. State 管理策略

**单源头**：`MutableStateFlow<PracticeSessionState>`，所有可变状态集中在一个 data class 中。

```kotlin
data class PracticeSessionState(
    val questionsWithState: List<QuestionWithState> = emptyList(),
    val currentIndex: Int = 0,
    val sessionStartTime: Long = System.currentTimeMillis(),
    // ... more fields
)
```

**派生 StateFlow**：UI 通过细粒度订阅获取各自需要的列表：

```kotlin
val questions: StateFlow<List<Question>> = _sessionState.map { it.questions }.stateIn(...)
val currentIndex: StateFlow<Int> = _sessionState.map { it.currentIndex }.stateIn(...)
val selectedOptions: StateFlow<List<List<Int>>> = _sessionState.map { s -> s.questionsWithState.map { it.selectedOptions } }.stateIn(...)
val showResultList: StateFlow<List<Boolean>> = _sessionState.map { s -> s.questionsWithState.map { it.showResult } }.stateIn(...)
val analysisList: StateFlow<List<String>> = _sessionState.map { s -> s.questionsWithState.map { it.analysis } }.stateIn(...)
val noteList: StateFlow<List<String>> = _sessionState.map { s -> s.questionsWithState.map { it.note } }.stateIn(...)
```

**state hoisting 模式**：Content composable 不持有本地 state；所有 sections 的展开/折叠 state 通过 `ExamDialogState` data class (local `remember`) 管理，但题目状态完全来自 ViewModel。

---

## 4. 计时器/自动跳转机制

**Exam 侧 — `ExamAutoAdvanceTimer`**：

```kotlin
class ExamAutoAdvanceTimer {
    var job by mutableStateOf<Job?>(null)

    fun cancel() { job?.cancel(); job = null }

    fun schedule(scope: CoroutineScope, delayMs: Long, onAdvance: suspend () -> Unit) {
        cancel()
        job = scope.launch {
            delay(delayMs)
            onAdvance()
        }
    }
}
```

- 单选/判断题点击选项 → `selectOption()` → 显示结果 → `autoAdvance(examDelay)` 延迟后自动跳到下一题
- **Cancel 时机**：prev/next 导航、展开/收起区块、编辑笔记、查看 AI 分析、删除笔记/分析、长按 — 用户任一主动操作均 `timer.cancel()`

**Practice 侧** — 同样的 `autoJob: Job?` 本地变量模式，逻辑一致。

---

## 5. Coordinator 委托模式（ExamViewModel）

```
ExamViewModel
 ├── ExamNavigationCoordinator     — 上/下一题、跳转、随机跳转
 ├── ExamAnswerCoordinator         — 选择选项、文本答案、显示结果
 ├── ExamMemoryModeCoordinator     — 记忆模式轮次管理
 ├── ExamQuestionEditCoordinator   — 题目编辑、保存
 ├── ExamStatisticsCoordinator     — 累计统计
 ├── ExamGradeCoordinator          — 交卷批改
 ├── ExamArtifactStateCoordinator  — 笔记/AI分析 CRUD
 ├── ExamProgressResetCoordinator  — 进度重置
 └── ExamLoadDelegate              — 加载题目、恢复进度
```

每个 Coordinator 持有 `_sessionState` 的引用，通过闭包获取配置值（如 `{ randomExamEnabled }`），实现高内聚低耦合。

---

## 6. AI 分析集成流程

```
ExamScreen (app)
  ├── 注入 DeepSeekViewModel, SparkViewModel, BaiduQianfanViewModel
  ├── 订阅 analysisPair, sparkPair, chatGptResult
  └── 传给 ExamScreenContent → ExamAISyncEffects
        │
        ├── 检测 showResult=true 时，如果 AI 有结果则 sync 到 viewModel
        └── viewModel.updateAnalysis(index, text) → artifactCoordinator → sessionState
```

在 `ExamScreenContent` 中显示：

```
ExamAnalysisArea
 ├── AnswerResultRow (答对/错标识)
 ├── ExamExplanation (题目解析, section 0)
 ├── ExamNoteSection (笔记, section 1)
 ├── ExamAIResults (DeepSeek 分析, section 2)
 ├── ExamAIResults (星火分析, section 3)
 ├── ExamAIResults (百度千帆, section 4)
 └── 通过 expandedSection 互斥展开
```

每个区块的交互（`onToggle`, `onDoubleTap`, `onLongPress`）均会 call `timer.cancel()` 防止自动跳转冲突。

### 6.1 AI 提问页对话模式（Phase 24 → Phase 26 Gemini）

```
DeepSeekAskViewModel.chatTurns (StateFlow)
  → DeepSeekAskChatTurnMapPipeline
  → AiChatTurnFlattenPipeline
  → AiChatMessageList (Gemini：assistant 全宽平铺)

AiChatConversationLayout (Scaffold)
  ├── content: AiChatMessageList（随 IME 收缩，不被输入栏遮挡）
  └── bottomBar: AiChatPromptSheet
        ├── AiChatPromptField（圆角 surfaceContainerHighest）
        └── AiChatPromptSendButton（圆形 primary）

用户输入 → viewModel.ask() → MultiTurnMessagesPipeline → api.chat()
```

- **IME**：禁止 prompt 上手动 `imePadding()`；由 `Scaffold.bottomBar` 与系统键盘联动
- **Gemini 视觉**：底部 sheet + 顶部分割线；user 右侧 pill；assistant 无气泡
- **DeepSeek / Spark / Baidu** 共用 `AiChatConversationLayout`

---

## 7. 题目类型系统

`domain/QuestionTypes.kt` — `object QuestionTypes`，提供 `isSingle()`, `isMulti()`, `isJudge()`, `isFill()` 判断方法。

`Question` model：

```kotlin
data class Question(
    val id: Int, val content: String, val type: String,  // 判断/单选/多选/填空
    val options: List<String>, val answer: String,
    val explanation: String, val isFavorite: Boolean, val isWrong: Boolean,
    val isEdited: Boolean, val fileName: String?, val stemImages: List<String>
)
```

运行时包裹在 `QuestionWithState` 中，附加 `selectedOptions`, `textAnswer`, `showResult`, `isCorrect`, `analysis`, `note` 等字段。

---

## 12. 导航与手势（Phase 23 → Phase 29）

> **完整规格（修改前必读）：** [practice_session_navigation_spec.md](./practice_session_navigation_spec.md)

**延时 0 串题:** `PracticeQuestionUiResolvePipeline` 校验 `ui.index == currentIndex`；自动跳题前 `yield()`。

**多轮 vs 单轮全答（Phase 29）:** `FullAnswerMultiRoundSessionPipeline.isMultiRoundSession` — 含第 2 轮及以上才启用轮次池约束。

**轮次槽 pending（Phase 29）:** 全答模式 = 有任意输入即完成该题；须全对 = 批改且答对。

**底栏 ← / → 单击:**
| 场景 | 行为 |
|------|------|
| 多轮全答 | 当前轮次池 pending 题间跳转 → 轮次全部完成 → 跨词条 |
| 单轮全答 / 普通练习 | 全库未作答题间跳转 |

**底栏 ← / → 双击（全答）:** 强制跨词条。

**横滑:** 仅浏览已答历史（与底栏未答跳转分离）；`QuestionSessionHistorySwipePipeline` 方向判定。

**批改展示:** `rememberPracticeResultDisplayReady` 延迟一帧挂载批改区。

---

## 11. 填空输入与批改色（Phase 22 → Phase 27）

**填空焦点不上移:**
- `adjustNothing` — 窗口不 resize/pan
- `QuestionSessionChromeLayout` — Box 层叠：顶栏 `TopCenter`、底栏 `BottomCenter` + `consumeWindowInsets(ime)`，**底栏物理锚定屏幕底部**
- scroll 区 `padding(top=48dp, bottom=64dp)` 避开 chrome；**无 layout 级 imePadding**
- `QuestionSessionImeScrollSpacer` — 键盘弹出时 scroll 内容末尾追加可滚高度，用户手动上滑查看挖空

**批改色（Phase 25 — Cursor diff 新增行）:**
| 元素 | 浅色 | 说明 |
|------|------|------|
| 答对容器 | `#DFF7DF` | GitHub/Cursor 新增行背景 |
| 答对文字 | `#1A7F37` | 新增行前景绿 |
| 答错容器 | `#FFEBEE` | Material Red 50 |
| 答错文字 | `#C62828` | Red 800 |

单一数据源：`AnswerCorrectHighlightColorPipeline` → 选项容器 + 填空/结果文字。

**历史记录 sheet:** 与答题卡一致，`AppLazyBottomSheet` 92% 屏高 + 内层 LazyColumn。

---

## 9. 性能与弹层（Phase 21）

**滚动分区** — 顶栏 + `QuestionSessionBodyScroll(weight=1)` + 底栏导航，避免整页 scroll 重组。

**弹层分离** — `QuestionTypographySheet` / 答题卡 / 编辑对话框置于 `ScreenSafeScaffold` 外同级，Modal 不进入 scroll 树。

**Bottom Sheet 分工:**
| 场景 | 组件 | 说明 |
|------|------|------|
| 排版设置 | `AppStaticBottomSheet` | 3 行 stepper，无 scroll |
| 答题卡 | `AppLazyBottomSheet` | 内层 LazyColumn 自管滚动，92% 屏高 |
| 长文解析 | `AppScrollBottomSheet` | 保留外层 scroll |

**收藏数据流:**
```
FavoriteViewModel.ensureFullListLoaded()
  → favoriteQuestions Flow
  → FavoriteSessionPipeline.isFavorite(questionId, list)
  → PracticeExamTopBar 星标 primary 色
  → add/remove 同步收藏库
```

---

## 10. 完整文件清单（Phase 21 新增）

### ui-common
- `QuestionSessionBodyScroll.kt`
- `AppLazyBottomSheet.kt` / `AppStaticBottomSheet.kt`

### core
- `FavoriteSessionPipeline.kt`

---

## 8. 完整文件清单

### app 模块
- `app/.../exam/ExamScreen.kt` — 考试入口 + AI ViewModel 组装
- `app/.../exam/ExamAISyncEffects.kt` — AI 结果同步到 ViewModel
- `app/.../practice/PracticeScreen.kt` — 练习入口

### feature-exam 模块
- `ExamViewModel.kt` — 核心 ViewModel
- `ExamScreenContent.kt` — 主内容 Composable
- `ExamAutoAdvanceTimer.kt` — 计时器
- `ExamState.kt` — 23 个独立 StateFlow 的 state holder（备选架构）
- `ExamNavigationCoordinator.kt`, `ExamAnswerCoordinator.kt`, etc. — 各 Coordinator
- `components/ExamAnalysisArea.kt`, `ExamExplanation.kt`, `ExamAIResults.kt`, `ExamNoteSection.kt` — 分析区块组件
- `ExamLoadDelegate.kt`, `ExamEndFlow.kt`, `ExamDialogState.kt` — 辅助

### feature-practice 模块
- `PracticeViewModel.kt` — 练习 ViewModel
- `PracticeNavigationCoordinator.kt`, `PracticeAnswerHandler.kt`, etc. — 各 Coordinator
- `PracticeAnalysisSections.kt`, `PracticeResultSection.kt` — 分析展示

### domain 模块
- `Question.kt`, `QuestionWithState.kt`, `PracticeSessionState.kt`
- `QuestionTypes.kt`
- `UnifiedQuestionState.kt`, `AnswerStatus.kt`, `ExamProgress.kt`
