# ADR-006: 自适应渐隐原子练习作为独立 Session 模式

## Status

Accepted (2026-07-16)

## Context

原子题库把一条原文拆成多个带标签、权重的填空片段。全部片段直接进入常规填空复习会产生大量重复和长期复习债务。该问题需要独立的候选池、呈现阶段和调度状态，但不得改变常规 Practice/Exam/Review 行为。

## Decision

- 新增 `QuestionSessionKind.AdaptiveFading`，复用 Practice Screen、Command、Snapshot 和导航/揭示策略。
- 原子题库保持只读；自适应进度通过独立 `AdaptiveAtomRepository` 持久化。
- 新模式使用 `AdaptiveFadingQuestionPipeline` 动态生成题目，不修改普通 `PracticeFillConfigPipeline` 的输入与输出。
- 答题结果由 `AdaptiveFadingProgressExtension` 消费 `AnswerSubmitted` 事件并写入仓库；Extension 不引用具体 Session。
- 首页仅对 SQLite/DB 原子题库显示新入口；普通练习入口与路由保持不变。
- MVP 使用固定、可解释的阶段间隔；个性化调度属于后续仓库实现替换，不新增 UI 行为开关。

## Consequences

- 新模式可独立演进和清除进度，不污染普通练习进度。
- UI 继续保持单一实现；题型差异由生成后的 `Question` 数据驱动。
- Room 新表需要显式迁移，避免升级时破坏既有题库和练习记录。

## Alternatives Considered

- 在普通 Practice 中增加布尔开关：拒绝，会形成第二套分散模式判断并污染原有加载逻辑。
- 预生成三套题库：拒绝，会复制数据和进度，无法表达同一原子的阶段演进。
- 使用 AI 生成情境题：暂不采用，成本高且偏离原子库自动出题边界。
