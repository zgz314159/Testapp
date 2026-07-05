# ADR-001: Question Session 作为行为边界

## Status

Accepted (2026-07-05)

## Context

- 抽屉浏览、练习、复盘、考试共用 `PracticeScreen` + `PracticeViewModel`，导致退出交卷、进度恢复、导航手势交叉污染。
- 布尔标志（`isReviewMode`, `targetQuestionId`, …）随模式线性增长。

## Decision

- 引入 **`QuestionSession`** 为会话生命周期与行为边界；**Screen 只渲染**，不发模式 if。
- **`SessionHost`** 仅 `enter(kind)` / `leave()` / 暴露 `StateFlow<QuestionSession?>`，不转发 submit/goto。
- **`SessionRegistry`** 使用 `Map<KClass<QuestionSessionKind>, SessionCreator>` O(1) 创建。
- 首版完整实现 **`BrowseSession`**，其余模式渐进迁入。

## Consequences

- 新增模式 = 新 Kind + Session 类 + Creator 注册，不改 Screen。
- 短期 `PracticeScreen` 双轨（Session / 旧 VM）可接受，Browse 切片后尽快单轨。

## Alternatives Considered

- **BrowseScreen 复制 UI** — 拒绝：UI 99% 相同，维护两份。
- **胖 QuestionSessionController** — 拒绝：易重现 God ViewModel。
