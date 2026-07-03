# NavigationController 分解规范（Phase 36）

> **门禁：** 各 handler ≤300 行；`NavigationController.kt` ≤200 行。

## 文件职责

| 文件 | 职责 |
|------|------|
| `NavigationController.kt` | 薄门面，公开 API 委托 |
| `NavigationEnvironment.kt` | 共享依赖 + 无状态 helper |
| `NavigationTargetNavigator.kt` | `navigateToQuestion` 副作用 |
| `NavigationMultiRoundIconNav.kt` | 多轮全答 step0–4 单击链 |
| `NavigationUnansweredIconNav.kt` | 底栏 ←/→ + `NavigationIconCanMove` |
| `NavigationSkipSource.kt` | 跨词条 skip |
| `NavigationSequentialNext.kt` | 答后自动 `nextQuestion()` |

## 改导航前

1. 读 `.ai/practice_session_navigation_spec.md`
2. 业务判定进 `PracticeFullAnswer*Pipeline`，不进 Controller
3. 改后跑 `PracticeFullAnswer*` 单测 + `:feature-practice:compileDebugKotlin`
