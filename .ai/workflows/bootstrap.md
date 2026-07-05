# Cursor Startup Protocol

> 每次 Agent 会话处理**编码类任务**时执行。文档类 / 纯问答可跳过 Guard，但仍建议读 `current/current_state.md`。

## 七步启动

| Step | 动作 | 文档 |
|------|------|------|
| 1 | 读宪法 | `PROJECT_CONSTITUTION.md` |
| 2 | 读入口 | `README.md` |
| 3 | 读 Session 架构 | `architecture/session_architecture.md` |
| 4 | 读现状 | `current/current_state.md` + `current/loc_audit.md` |
| 5 | 执行 Architecture Guard | `workflows/architecture_guard.md` |
| 6 | 输出 Task Plan | `workflows/task_execution.md` 模板 |
| 7 | 编码 → 验证 → Review | `workflows/code_review.md` |

## 按任务类型分支

| 类型 | 额外必读 | 流程 |
|------|----------|------|
| 新功能 | 相关 ADR + `current/module_map.md` | Guard → Plan → Code |
| Bug 修复 | 触达文件 + 单测 | 轻量 Guard → Fix → Compile |
| 重构 | `current/tech_debt.md` + decomposition spec | `refactor_workflow.md` |
| 架构变更 | 全部 ADR | 先写 ADR 草稿 → Guard → 实现 |
| 仅文档 | `11_MEMORY_SYNC_PROTOCOL.md` | 禁止读 `.kt` |

## 输出契约

Guard 通过后，回复中须包含：

```text
Architecture Guard: PASS
Architecture: PASS
Dependency: PASS
Reuse: PASS
ADR: PASS | N/A
Impact: PASS
```

任一 FAIL → 只输出 **Change Plan / Extraction Plan**，不写实现代码。

## Windows 终端提醒

PowerShell 5：`cd path; ./gradlew`（不用 `&&`）。  
中文编码：见 `.ai/ENCODING_AND_TERMINAL_GUIDE.md`。
