# ExamScreenContent 分解规范（Phase 37）

> **门禁：** `scripts/check-exam-screen-loc.ps1` — `ExamScreenContent.kt` ≤500 行。

## 文件职责

| 文件 | 职责 |
|------|------|
| `ExamScreenContent.kt` | 状态订阅 + 三段式布局 |
| `components/ExamScreenGestureModifier.kt` | 横滑/边缘手势 |
| `components/ExamScreenBottomBar.kt` | 底栏导航 |
| `components/ExamScreenEffects.kt` | LaunchedEffect + Overlays |
| `ExamSessionExitPipeline.kt` | 退出判定（无 Compose） |

## 禁止

- 在 `ExamScreenContent` 内新增 >30 行 inline Composable 或重复 `ExamDialogs` 块
