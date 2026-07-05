# Naming Rules

## 类型后缀

| 后缀 | 含义 | 示例 |
|------|------|------|
| `*Pipeline` | 无状态纯判定/转换 | `SessionPracticePostAnswerNavigationPipeline` |
| `*Coordinator` | 有状态编排，≤300 行 | `ImportCoordinator` |
| `*Delegate` | 单职责委托给引擎 | `PracticeSessionGradeDelegate` |
| `*Creator` | SessionRegistry 工厂 | `PracticeSessionCreator` |
| `*Session` | QuestionSession 实现 | `BrowseSession` |
| `*Extension` | Session 扩展 | `SessionAiAnalysisExtension` |
| `*Handler` | Command 分发 | `PracticeSessionCommandHandler` |
| `*Mapper` | 状态 → Snapshot 映射 | `BrowseSessionSnapshotMapper` |
| `*Bindings` | Screen 与 Engine 适配 | `PracticeScreenBindings` |
| `*Route` | Nav composable 薄入口 | `PracticePracticeRoute` |
| `*Effects` | Compose 副作用收集 | `PracticeScreenEffects` |
| `*Host` | 生命周期容器 | `SessionHost` |

## 禁止命名

- `*Manager`（除非已有遗留且 ADR 允许）  
- `*Helper` / `*Util` 超过 3 个公开函数 — 改为 Pipeline  
- `*Controller` 作为 Session 平行层 — 用 Command Handler  

## 包名

- `core.session.policy` — Exit / Ui / Persistence 工厂  
- `core.session.strategy` — Navigation / Reveal / Memory  
- `presentation.session.<area>` — Session 实现  
- `presentation.screen.<area>.components` — Screen 子 UI  

## 文件

- 一文件一主类型（允许 private 小 helper）  
- Pipeline / Test 同名：`FooPipeline.kt` + `FooPipelineTest.kt`  
