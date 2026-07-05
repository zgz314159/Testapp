# Architecture Guard

> **PowerAI Engineering OS 核心门禁。** 所有编码任务必须先执行本文件六步。  
> 完整 LOC/职责/依赖红线见 `architecture/coding_principles.md`。

---

## 六步流程

### Step 1 — Architecture Scan

- 读 `architecture/session_architecture.md`（触达 Session 时）
- 读 `current/architecture_map.md` + `current/module_map.md`
- 确定 **Owner 模块**（`:app` | `:feature-*` | `:data` | `:domain` | `:core` | `:ui-common`）

**FAIL 条件**：新逻辑落在错误模块；违反 `PROJECT_CONSTITUTION` 依赖方向。

---

### Step 2 — Dependency Scan

- 读 `current/dependency_graph.md`
- 列出触达文件的 **直接依赖** 与 **注入数**
- 规则：单文件依赖 >8 → 必须先 Facade / Coordinator

**FAIL 条件**：新增依赖使热点文件超红线；引入循环依赖。

---

### Step 3 — Reuse Scan

**必须优先复用**（禁止复制粘贴整块）：

| 类型 | 查找位置 |
|------|----------|
| Session | `SessionRegistry` + `*SessionCreator` |
| Strategy | `core/session/strategy/`、`core/session/policy/` |
| Extension | `SessionExtension` / `FeatureExtension` |
| Pipeline | 同 feature 或 `:core` 下 `*Pipeline.kt` |
| Coordinator | 同 screen 包下已有 `*Coordinator.kt` |
| Capabilities | `SessionCapabilitiesPresets` + ADR-002 |

**FAIL 条件**：存在可复用组件仍新建平行实现；复制 >30 行已有 Pipeline/UI。

---

### Step 4 — ADR Check

| 情况 | 动作 |
|------|------|
| 新 Session Kind / 新核心抽象 / 行为开关变更 | **必须先有 ADR** 或本次同时起草 ADR |
| 仅 bugfix / 小改动 | N/A，标注 `ADR: N/A` |
| 与 ADR 冲突 | **STOP**，提议修订 ADR |

已接受 ADR：`.ai/ADR/001`–`005`。

---

### Step 5 — Impact Analysis

填写 **Architecture Placement Review**：

```markdown
## Architecture Placement Review — [功能名]

### 模块归属
- Owner: …

### 影响文件
| 文件 | 类型 | 当前 LOC | 预估新增 | 预测合计 | LOC | 职责≤3 | 依赖≤8 |
|------|------|----------|----------|----------|-----|--------|--------|
| … | … | N | +M | N+M | ✅/⚠️/❌ | ✅/❌ | ✅/❌ |

### 拆分决策
- [ ] 预测未触红线 → 可改
- [ ] 触红线 → extraction plan，禁止堆代码

### 新代码落点
| 内容 | 目标路径 |
|------|----------|
| 业务判定 | `*Pipeline.kt` + 单测 |
| UI 区块 | `components/<Screen>*.kt` |
| 对话框 | 已有 `*DialogsHost.kt` |
| 会话逻辑 | Session / Extension / Command |
```

数据源：`current/file_registry.md`、`current/loc_audit.md`、`scripts/check-loc-over-500.ps1`。

**FAIL 条件**：任一影响文件预测超 LOC/职责/依赖红线且无拆分计划。

---

### Step 6 — Change Plan

输出：

1. 改哪些文件（创建 / 修改 / 删除）
2. 测试计划（单测 / 冒烟 / `ArchitectureTest`）
3. 文档更新清单（`change_log`、`loc_audit`、`ADR`）
4. 预估 diff 规模

**全部 PASS** → 进入 `task_execution.md` 编码阶段。

---

## 快速 PASS 输出模板

```markdown
## Architecture Guard

| Step | Result | Notes |
|------|--------|-------|
| Architecture Scan | PASS | Owner: :feature-practice |
| Dependency Scan | PASS | 无新注入 |
| Reuse Scan | PASS | 复用 SessionCommand + 已有 Pipeline |
| ADR Check | N/A | 无新抽象 |
| Impact Analysis | PASS | 2 文件，均 <500 LOC |
| Change Plan | … | … |

**Guard: PASS** — 可以开始编码。
```

---

## 禁止清单（Guard 层）

- 新增未经 ADR 的 Manager / Controller / Utils 大包 / GlobalState / 第二套 Singleton 行为源
- 在任意 `>500` 行文件直接加功能
- Screen 内 `DialogsHost`、>30 行 inline Composable、大段 LaunchedEffect 业务链
- ViewModel 内 >20 行 `stateIn` / 导航 if-else 链
- `Feature Request → Code` 跳过 Guard

---

## 验证脚本（编码后）

```powershell
scripts/check-loc-over-500.ps1
# 按需
scripts/check-practice-screen-loc.ps1
scripts/check-exam-screen-loc.ps1
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.arch.ArchitectureTest"
```

---

## 历史

本文件整合自 `.ai/13_ARCHITECTURE_GUARD.md`（Level 6 OS）。旧路径保留为 redirect stub。
