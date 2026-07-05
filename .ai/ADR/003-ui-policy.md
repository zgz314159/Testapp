# ADR-003: UiContract 由 Capabilities 推导

## Status

Accepted (2026-07-05)

## Context

- 同时维护 Capabilities 与 UiContract 易产生冲突（canSubmit=true 但 bottomBar=Hidden）。

## Decision

- Session **不手写** `SessionUiContract`。
- **`UiPolicyFactory.from(capabilities)`** 生成 topBar / bottomBar / gesture / menu / animation。
- Data-driven UI：Compose 只 `collectAsState(session.uiContract)`。

## Mapping (首版)

| Capability | UiContract |
|------------|------------|
| `canSubmit=false` | bottomBar: NavOnly 或 Hidden |
| `canSwipeAnsweredHistory=true` | gesture: HistoryBrowse |
| `canRevealOnSubmit=true` | animation.resultDisplayDelay > 0 |
| `canUseAiAsk=true` | menu.showAi=true |

## Consequences

- 单一真相源；新增 UI 区域时扩展 Factory 表，不碰 Session 类。

## Alternatives Considered

- Session 子类 override uiContract — 拒绝：双重配置风险。
