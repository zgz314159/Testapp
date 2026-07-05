# Migration Guide

> 模块化与 Session V5 迁移路径。进度活文档：`current/refactoring_plan.md`。

## 已完成里程碑（摘要）

| Phase | 内容 | 状态 |
|-------|------|------|
| P1–P4 | UnifiedState、SessionEngine、Coordinator 拆分、UseCase Facade | ✅ |
| P5–P25 | feature-practice/exam、:core session、:domain UseCase | ✅ |
| P26–P52 | Session V5、Registry、Command CQRS、Strategy 工厂 | ✅ |
| P75–P79 | Navigation Strategy、Settings 下沉、归档收尾 | ✅ |

## Session 迁移规则

**旧路径（禁止新增）**  
- `PracticeViewModel` / `ExamViewModel` 模式布尔  
- Screen 内 `isReviewMode` / `targetQuestionId` 分支  

**新路径（唯一允许）**  
```text
Route → SessionHost(kind) → SessionRegistry → QuestionSession
Screen → session.handle(SessionCommand)
Extension ← SessionEvent
```

## 模块迁移检查表

迁出 `:app` 时确认：

- [ ] Hilt Module 已随实现迁移或保留 EntryPoint  
- [ ] Nav Route 仍薄（<80 行），只注入回调  
- [ ] `ArchitectureTest` 模块边界仍成立  
- [ ] 无 `:app` → 已迁文件的反向依赖  

## 双轨淘汰

- Browse / Practice / Review / Exam / QuestionEdit 已 Session 单轨  
- 遗留 `bindings` 裸引擎路径：仅 Extension 桥接，不新增  

## 新模式接入步骤

1. ADR 草稿（若新 Kind 或 Capability）  
2. `QuestionSessionKind` + `SessionCreator` + `SessionRegistryModule.register`  
3. `SessionCapabilitiesPresets` + `UiPolicyFactory`  
4. Route + `SessionHost`  
5. 单测：`SessionRegistryTest`、`ArchitectureTest`  
6. 更新 `session_architecture.md` / ADR  

## 参考

- `architecture/session_architecture.md`  
- `ADR/001-session.md`、`ADR/002-capabilities.md`  
- `current/refactoring_plan.md`（完整 Phase 列表）  
