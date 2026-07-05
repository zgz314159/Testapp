# ADR-002: SessionCapabilities 作为唯一行为开关

## Status

Accepted (2026-07-05)

## Context

- `isBrowseMode` / `isExam` 等分散在 Screen、VM、Pipeline。
- Fill 设置「全答对」在非全答模式下泄漏（`||` 而非 `&&` 门禁）。

## Decision

- 每个 Session 声明 **`SessionCapabilities`**（canSubmit, canPersistProgress, canRestoreProgress, canSwipeAnsweredHistory, …）。
- Screen / Strategy **只读 capabilities**，禁止 `when (kind)`。
- Fill 相关运行时门禁：`fullAnswerModeActive && fullAnswerRequireCorrect`（Capabilities 可反映推导结果）。

## Consequences

- Browse：`canSubmit=false`, `canPersistProgress=false`, `canRestoreProgress=false` → 返回 PopBack，不交卷。
- 产品变更（浏览可试答）只改 `BrowseSession.capabilities` 一处。

## Alternatives Considered

- 继续布尔参数 — 拒绝：已证明交叉感染。
