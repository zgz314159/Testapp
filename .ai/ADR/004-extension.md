# ADR-004: SessionExtension + Service（非 Plugin Framework）

## Status

Accepted (2026-07-05)

## Context

- AI、收藏、统计横切能力若写入 Session 会无限膨胀。
- 完整 PluginRegistry/PluginLifecycle 对当前规模过重。

## Decision

- **`LifecycleExtension`**：`onStart(SessionContext)` / `onDestroy()`。
- **`FeatureExtension`**：`onEvent(SessionEvent, SessionSnapshot)` — **禁止引用 `QuestionSession` 类型**。
- 业务能力在 **`AiAskService` / `BookmarkService`**；Extension 薄包装。
- Hilt `@IntoSet` 提供 `List<SessionExtension>`，即注册表。
- 接口预留升级路径：`SessionExtension` → `SessionPlugin`（Session 无感）。

## Event 边界

- Extension 只消费 **SessionEvent**（AnswerSubmitted, QuestionChanged, …）。
- UI 只发 **SessionCommand**（Back, SubmitAnswer, GoToQuestion, …）。

## Consequences

- `AiExtension` 可独立测试：给定 Event + Snapshot，不 mock Session。
- 未来 Agent/多 Model 只增 Service + Extension，不改 Session 核心。

## Alternatives Considered

- 立即 Plugin Framework — 暂缓至第三方可插拔需求明确。
