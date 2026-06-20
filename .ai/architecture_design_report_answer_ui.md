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
