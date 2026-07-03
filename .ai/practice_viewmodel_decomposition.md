# PracticeViewModel 分解规范（Phase 39）

> **门禁：** `scripts/check-practice-vm-loc.ps1` — `PracticeViewModel.kt` ≤500 行。

## 文件职责

| 文件 | 职责 |
|------|------|
| `PracticeViewModel.kt` | 薄编排：协调器委托 + 公开 API |
| `PracticeViewModelSessionFlows.kt` | 派生 StateFlow（questions / uiQuestions / currentIndex 等） |
| `PracticeReviewSessionCoordinator.kt` | 复盘模式加载、浏览、已答历史滑动 |
| `PracticeQuestionReopenPipeline.kt` | pending / 全答重开题目状态（无 Compose） |

## 禁止

- 在 `PracticeViewModel` 内新增 >20 行 inline `stateIn` 或复盘分支逻辑
- 复盘滑动逻辑不得回写到 VM 私有字段（由 `PracticeReviewSessionCoordinator` 持有）
