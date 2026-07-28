# ADR-009: 磁吸重建作为独立 QuestionSession

## Status

Accepted (2026-07-29)

## Context

磁吸重建需要读取原子题库、恢复完整条文、维护候选词块与组装顺序，并提供与普通答题完全不同的拖动交互。若把这些模式判断加入 PracticeScreen 或 PracticeSessionEngine，会污染现有练习、考试和自适应渐隐路径。

## Decision

- 新增 `QuestionSessionKind.MagneticRebuild`，通过现有 `SessionRegistry` 和 `SessionHost` 创建。
- `MagneticRebuildSession` 是该模式唯一行为边界；UI 只发送 `SessionCommand.Magnetic*`。
- 使用独立 `MagneticRebuildScreen`，不修改现有 PracticeScreen、ExamScreen 和答题组件。
- 原子题库保持只读；MVP 不写正式练习历史、错题或收藏。
- 入口仅对 `.sqlite` / `.db` 原子题库显示，并在设置页原子题库出题模式中增加说明。
- 语义块由确定性 Pipeline 生成，标准顺序始终来自原题，不使用 AI 实时改写。

## Consequences

- 旧功能和 UI 无模式分支污染；新玩法可独立演进。
- 新 Session 使用定制状态流，通用 `SessionSnapshot` 只提供生命周期与汇总信息。
- MVP 暂不增加 Room 表；跨日状态与长条文层级拼图需另立迁移方案。

## Alternatives Considered

- 在 PracticeScreen 增加 `magneticMode` 布尔开关：拒绝，违反 Capabilities/Session 单一行为边界。
- 把词块排序伪装成普通选择题：拒绝，无法表达移动、磁吸和相邻关系。
- AI 实时拆分条文：拒绝，法规原文需确定、离线和可复现。
