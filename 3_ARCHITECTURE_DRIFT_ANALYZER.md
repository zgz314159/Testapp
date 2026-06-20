# ARCHITECTURE DRIFT ANALYZER

> 检测代码实际结构与 `ARCHITECTURE.md` 定义的目标架构之间的偏离。

---

## REFERENCE ARCHITECTURE

引自 `ARCHITECTURE.md`:

```
:app
 ├── :domain
 └── :data → :domain

:feature-practice, :feature-exam, :ui-common, :core  (stubs / minimal)
```

**Rule:** `domain` has no project dependencies. `data` implements domain repos. `app` hosts UI, VMs, DI.

---

## DRIFT DETECTION

### DRIFT-001: Business Code Concentration in `:app`

| 项 | 值 |
|----|---|
| **定义** | `:app` 承载了全部 UI/VM/DI/UseCase 代码 |
| **目标** | `feature-practice` / `feature-exam` 应承载对应功能模块的 UI+VM |
| **实际** | `feature-*` 模块 0 行代码 |
| **偏离度** | **SEVERE** — 完全未迁移 |
| **影响** | 模块边界形同虚设，`:app` 成为 God module |

### DRIFT-002: ViewModel God Files

| 项 | 值 |
|----|---|
| **定义** | `PracticeViewModel.kt` ~3900 行，`ExamViewModel.kt` ~2455 行 |
| **目标** | 单 ViewModel <500 行；通过 coordinator/useCase 分层 |
| **实际** | 远超阈值，承载过多职责 |
| **偏离度** | **HIGH** — 已标记但未开始拆分 |
| **影响** | 可维护性差，新人理解成本高，测试覆盖困难 |

### DRIFT-003: Progress Scope ID in Wrong Layer

| 项 | 值 |
|----|---|
| **定义** | `PracticeProgressScope.kt` 位于 `presentation.screen` |
| **目标** | 进度 ID 策略应属于 domain 层 |
| **实际** | 在 presentation 层 |
| **偏离度** | **LOW** — 功能正确，仅层位置不当 |
| **影响** | 其他模块无法直接复用进度 ID 策略 |

### DRIFT-004: Hidden Dependencies via Hilt

| 项 | 值 |
|----|---|
| **定义** | Hilt module 在 `:app` 中提供所有 binding |
| **目标** | 每个模块自包含 DI |
| **实际** | 单一 `:app` DI module 承担所有 |
| **偏离度** | **MEDIUM** |
| **影响** | 模块无法独立编译/测试 |

---

## DRIFT REPORT SUMMARY

| ID | 描述 | 严重度 | 关联已知问题 |
|----|------|--------|-------------|
| DRIFT-001 | `:app` God module | 🔴 SEVERE | D-001 |
| DRIFT-002 | ViewModel God files | 🔴 HIGH | K-002, K-003 |
| DRIFT-003 | Progress scope in wrong layer | 🟡 LOW | D-002 |
| DRIFT-004 | Hilt 集中化 | 🟡 MEDIUM | — |

---

## AFFECTED MODULES

- `:app` — 所有漂移的承载体
- `:feature-practice` — 应承载 Practice UI+VM（当前空）
- `:feature-exam` — 应承载 Exam UI+VM（当前空）
- `:domain` — 应承载 Progress ID 策略（当前缺失）
