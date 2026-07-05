# Cursor Rules（PowerAI Engineering OS）

> 与 `.cursor/rules/architecture-guard.mdc` 配套。冲突时以 `PROJECT_CONSTITUTION.md` 为准。

## 启动（每次编码任务）

1. 读 `.ai/README.md` 规定的顺序  
2. 执行 `.ai/workflows/architecture_guard.md` 六步  
3. Guard PASS 后按 `.ai/workflows/task_execution.md` 执行  

## 禁止

| 禁止 | 替代 |
|------|------|
| 新 Manager / Controller 大包 | Session + Command + Extension |
| 新 GlobalState / 第二套 Singleton 行为源 | SessionRegistry + Capabilities |
| 未经 ADR 的核心抽象 | 先 ADR 再实现 |
| 复制整块 Pipeline / Coordinator / Repository | 扩展或参数化已有类型 |
| Utils 万能类（>200 行） | 命名 Pipeline 或放 :core |
| 全项目 `**/*.kt` 盲扫 | `current/file_registry` + 模块内 glob |
| 跳过 Guard 直接写代码 | 先输出 PASS/FAIL 表 |

## 必须

| 必须 | 说明 |
|------|------|
| 复用 Session / Strategy / Extension / Service / Capabilities | Reuse Scan |
| 业务判定 → `*Pipeline.kt` | 见 `coding_principles.md` |
| UI 区块 → `components/*.kt` | 见 `compose_rules.md` |
| 最小正确 diff | 不改无关代码 |
| 编译 + loc check | 任务结束前 |
| 用户未要求不 git commit | — |

## 输出格式（实现类任务）

```markdown
## Task: …
### Architecture Guard
| Step | Result | … |
…
### Plan
…
```

## 路径速查

| 需求 | 文件 |
|------|------|
| 宪法 | `.ai/PROJECT_CONSTITUTION.md` |
| Guard | `.ai/workflows/architecture_guard.md` |
| 现状 | `.ai/current/current_state.md` |
| LOC | `.ai/current/loc_audit.md` |
| Session | `.ai/architecture/session_architecture.md` |
| ADR | `.ai/ADR/` |

## 用户一句话指令

```
按 PowerAI Engineering OS：读 README → Guard 六步 → Plan → 编码
```
