# PowerAI Engineering OS

> Cursor / Agent 的**唯一入口**。任何编码任务从这里开始，不从这里开始 = 违规。

## 启动协议（强制顺序）

```
PROJECT_CONSTITUTION.md          ← L0 宪法（冲突时最高优先级）
        ↓
architecture/session_architecture.md
        ↓
ADR/（按需读相关 ADR，新抽象必读全部）
        ↓
current/current_state.md + current/loc_audit.md
        ↓
workflows/architecture_guard.md   ← 执行 Guard，输出 PASS/FAIL
        ↓
workflows/bootstrap.md            ← 任务类型选路径
        ↓
rules/cursor_rules.md
        ↓
【用户 Task】
```

**禁止**：跳过上述顺序，直接读 Task 并开始编码。

---

## 目录地图

```text
.ai/
├── PROJECT_CONSTITUTION.md     ← 项目宪法（L0）
├── README.md                   ← 本文件（入口）
│
├── architecture/               ← 长期架构规则（L1）
│   ├── session_architecture.md
│   ├── module_rules.md
│   ├── dependency_rules.md
│   ├── coding_principles.md
│   └── migration_guide.md
│
├── ADR/                        ← 架构决策记录（L2）
│   ├── 001-session.md
│   ├── 002-capabilities.md
│   ├── 003-ui-policy.md
│   ├── 004-extension.md
│   └── 005-architecture-tests.md
│
├── current/                    ← 活文档 / 现状快照（L3）
│   ├── current_state.md        ← 每次任务必读
│   ├── loc_audit.md            ← LOC 红线清单
│   ├── architecture_map.md
│   ├── module_map.md
│   ├── dependency_graph.md
│   ├── file_registry.md
│   ├── change_log.md
│   ├── refactoring_plan.md
│   └── tech_debt.md
│
├── workflows/                  ← 执行流程（L4）
│   ├── bootstrap.md            ← 启动协议详解
│   ├── architecture_guard.md   ← 六步门禁（最重要）
│   ├── task_execution.md       ← 标准任务流 + 输出模板
│   ├── refactor_workflow.md
│   └── code_review.md
│
├── rules/                      ← 编码细则（L5）
│   ├── cursor_rules.md
│   ├── naming_rules.md
│   ├── compose_rules.md
│   └── session_rules.md
│
└── [legacy]                    ← 历史报告 / 分解 spec（按需）
    ├── practice_screen_decomposition.md
    ├── exam_screen_decomposition.md
    ├── 12_CONTEXT_LOADING_RULES.md
    └── …
```

---

## 任务一句话触发

用户输入：

```text
实现：XXX
```

Agent **必须**先输出（未完成 Guard 不得写代码）：

```markdown
## Task: XXX

### Architecture Guard
- Architecture Scan: PASS / FAIL — …
- Dependency Scan: PASS / FAIL — …
- Reuse Scan: PASS / FAIL — …
- ADR Check: PASS / FAIL / N/A — …
- Impact Analysis: PASS / FAIL — …
- Change Plan: …

### 开始编码？
```

全部 PASS 且用户确认（或任务足够明确）后，进入 `workflows/task_execution.md`。

---

## 完成后必做

| 动作 | 条件 |
|------|------|
| `scripts/check-loc-over-500.ps1` | 任何 `.kt` 改动 |
| 更新 `current/change_log.md` | Phase / 架构变更 |
| 更新 `current/loc_audit.md` | LOC 变化或拆分 |
| 更新 `current/current_state.md` | 里程碑 / 冒烟结果 |
| 新 ADR | 新核心抽象或边界变更 |

---

## Cursor Rules 联动

- `.cursor/rules/architecture-guard.mdc` — alwaysApply，指向本 OS
- 细则见 `.ai/rules/cursor_rules.md`

---

## 上下文预算

见 `.ai/12_CONTEXT_LOADING_RULES.md`（legacy，逐步迁入 `workflows/`）。  
黄金法则：**只读需要的文件，信任 current/ 层，禁止全仓库盲扫。**
