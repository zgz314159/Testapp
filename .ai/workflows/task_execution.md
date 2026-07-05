# Task Execution Workflow

> Guard PASS 后的标准执行路径。

## 流程图

```text
用户 Task
    ↓
Architecture Guard（六步）     ← workflows/architecture_guard.md
    ↓
Read Existing Implementation  ← 只读触达文件 + 直接依赖
    ↓
Implementation Plan           ← 本文件模板
    ↓
Coding                        ← 最小正确 diff
    ↓
Compile + Unit Tests
    ↓
Architecture Review           ← workflows/code_review.md
    ↓
Output Report + 文档同步
```

---

## Implementation Plan 模板

```markdown
## Task: [名称]

### 目标
一句话描述交付物。

### Guard 摘要
Architecture / Dependency / Reuse / ADR / Impact — 全部 PASS

### 现有实现
- 已读文件：…
- 复用点：Session / Pipeline / Extension / …

### 变更清单
| 操作 | 路径 | 说明 |
|------|------|------|
| 修改 | … | … |
| 新增 | … | … |

### 测试
- [ ] `./gradlew :app:compileDebugKotlin`
- [ ] 相关单测：…
- [ ] `check-loc-over-500.ps1`

### 文档
- [ ] change_log / loc_audit / current_state（如适用）
```

---

## 编码原则（执行中）

1. **最小 diff** — 只改任务相关代码  
2. **匹配现有风格** — 命名、Pipeline 模式、Hilt 注入方式  
3. **先拆后加** — 预测 LOC 触线则先 extraction  
4. **单测** — 有意义行为进 Pipeline 时补 `*Test.kt`  
5. **不主动 commit** — 除非用户要求  

---

## 任务类型速查

| 用户说 | Agent 做 |
|--------|-----------|
| `实现：XXX` | 完整 Guard + Plan + 询问确认 |
| `修复：崩溃/bug` | 轻量 Guard + 根因 + 最小修复 |
| `重构：XXX` | `refactor_workflow.md` |
| `审查：XXX` | `code_review.md`，不改代码除非要求 |

---

## 完成后 Output Report

```markdown
## 完成报告

### 变更
- …

### 验证
- compile: PASS
- tests: …
- loc check: PASS

### 文档
- 已更新 / 无需更新：…
```
