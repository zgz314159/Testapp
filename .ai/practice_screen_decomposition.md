# PracticeScreen 分解规范（Phase 35+）

> **门禁**: `scripts/check-practice-screen-loc.ps1` — `PracticeScreen.kt` 不得超过 **500 行**。

## 职责边界

| 文件 | 职责 | LOC 预算 |
|------|------|----------|
| `PracticeScreen.kt` | 状态订阅、LaunchedEffect、布局拼装、回调接线 | ≤500 |
| `components/PracticeScreenBottomBar.kt` | 底栏 ←/→/提交与全答模式导航 | ≤180 |
| `components/PracticeScreenQuestionScrollContent.kt` | 题目区、结果区、解析/笔记/AI 区块 | ≤250 |
| `components/PracticeScreenEffects.kt` | LaunchedEffect、生命周期、会话标记 | ≤180 |
| `components/PracticeScreenOverlays.kt` | Sheet、答题卡、编辑、PracticeDialogsHost | ≤200 |
| `feature-practice/PracticeDialogsHost.kt` | 删除/交卷/ChatGPT 对话框 | ≤80 |
| `feature-practice/PracticeSessionExitPipeline.kt` | 退出判定（无 Compose） | ≤50 |
| `feature-practice/PracticeSessionExitConfirmPipeline.kt` | 交卷确认参数（无 side effect） | ≤40 |

## PracticeScreen 允许包含

- ViewModel / Settings Flow 的 `collectAsState`
- 会话初始化 `LaunchedEffect`
- `requestSessionExit` → 调用 `PracticeSessionExitPipeline`
- `QuestionSessionChromeLayout` 三段式拼装
-  overlay：排版 Sheet、答题卡、编辑题目、解析全屏

## 禁止在 PracticeScreen 内新增

- `private fun DialogsHost` — 使用 `PracticeDialogsHost`
- 超过 ~30 行的 `@Composable` 子树 — 抽到 `components/` 或 `feature-practice`
- 无 Compose 的业务判定 — 抽到 `*Pipeline.kt`（feature-practice）

## 改功能前的检查清单

1. 预估新增行数；若 `当前行数 + 新增 > 450` → 先拆文件再实现
2. 新 UI 块 → `app/.../practice/components/PracticeScreen*.kt`
3. 新退出/交卷/导航规则 → `feature-practice/*Pipeline.kt` + 单测
4. 跑 `scripts/check-practice-screen-loc.ps1` 与 `:app:compileDebugKotlin`

## 子组件索引

```
PracticeScreen
├── PracticeScreenEffects (init / sync / lifecycle)
├── PracticeExamTopBar
├── PracticeScreenQuestionScrollContent
│   ├── QuestionSessionHeader + FillAnswerRoundLabel
│   ├── PracticeQuestionContent
│   ├── QuestionSessionActionRow
│   ├── PracticeResultSection
│   └── ExamAnalysisSection × N
├── PracticeScreenBottomBar → QuestionNavigationControls
├── practiceHistorySwipe (Modifier)
└── PracticeScreenOverlays → PracticeDialogsHost
```
