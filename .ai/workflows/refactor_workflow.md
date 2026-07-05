# Refactor Workflow

> 单文件 / 模块级重构专用。编码前仍须 Architecture Guard。

## 前置阅读

| 文件 | 用途 |
|------|------|
| `current/tech_debt.md` | 已知债 |
| `current/file_registry.md` | 当前 LOC |
| `current/dependency_graph.md` | 耦合 |
| `*_decomposition.md` | 屏幕/VM 职责表（legacy 根目录） |

## 步骤

1. **Scope** — 明确单一目标文件或子系统  
2. **Guard** — Impact Analysis 列出拆分后文件清单  
3. **Extraction Plan** — 新文件路径 + 职责（≤3）+ 预估 LOC  
4. **Tests first** — 确保现有单测绿，或补 characterization test  
5. **Mechanical move** — Pipeline / components 先搬，行为不变  
6. **Wire** — 更新 DI / import / 路由  
7. **Delete dead** — 删 bridge / deprecated  
8. **Verify** — compile + 单测 + `check-loc-over-500.ps1`  
9. **Sync** — `loc_audit.md`、`change_log.md`、decomposition spec  

## 拆分落点速查

| 抽出内容 | 目标 |
|----------|------|
| 纯判定逻辑 | `*Pipeline.kt` |
| UI 区块 | `components/<Screen>*.kt` |
| 对话框 | `*DialogsHost.kt` |
| 派生 StateFlow | `*SessionFlows.kt` |
| 进度/导航 | `*Coordinator.kt`（≤300 行） |

## 禁止

- 重构同时加 unrelated 功能  
- 一次 PR 拆整个 `:app`  
- 无单测的大块搬移  
