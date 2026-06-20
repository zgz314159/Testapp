<!--
  分析生成时间: 2026-06-14 09:37 UTC+8
  分析范围: .ai/ 目录下 Level 6 自进化工程操作系统全部文件
  分析依据: 13_ARCHITECTURE_GUARD.md, 12_CONTEXT_LOADING_RULES.md, 11_MEMORY_SYNC_PROTOCOL.md, architecture_map.md, file_registry.md, dependency_graph.md, module_map.md, current_state.md
-->

# Level 6 自进化工程操作系统 — 全部文件功能分析

---

## 一、核心流水线文件（规则引擎）

| 序号 | 文件 | 功能 |
|------|------|------|
| **11** | **11_MEMORY_SYNC_PROTOCOL.md** | **内存同步协议** — 定义 `CURRENT_STATE.md`/`ARCHITECTURE.md`/`KNOWN_ISSUES.md`/`TASK_LOG.md` 四个 SoT（Source of Truth）文件何时触发 `.ai/` 派生文件重新生成。包含增量同步规则、时间戳校验（stale/fresh 检测）、冻结快照追加原则。Rule 1: 增量不重写；Rule 2: 时间戳必打；Rule 3: 任务完成时同步；Rule 4: 大阶段前同步；Rule 5: 冻结快照追加不覆盖 |
| **12** | **12_CONTEXT_LOADING_RULES.md** | **Token 预算管理（上下文加载规则）** — 硬性禁止每次任务全项目扫描 225+ 文件。按任务类型严格限制加载文件数：功能实现(7文件/~15K tokens)、架构设计(6文件/~10K tokens)、重构(6文件/~15K tokens)、健康扫描(25文件/~50K tokens)、文档(4文件/~5K tokens)、测试(4文件/~10K tokens)。超预算必须拆分子任务。Golden Rule: READ WHAT YOU NEED. NOTHING MORE. |
| **13** | **13_ARCHITECTURE_GUARD.md** | **架构守门员（架构防护门控）** — ANY 新功能实现前的强制前置门控。6条核心规则：Rule1: 禁止直接实现，必须先做 Architecture Placement Review；Rule2: LOC 红线（VM≤400/Repo≤500/Screen≤500/Coordinator≤300，超 Danger 线先拆分再实现）；Rule3: 职责数红线（≤3，第4职责必须新文件）；Rule4: 依赖数红线（>8 禁止）；Rule5: Architecture Placement Review 强制模板；Rule6: 正确请求格式。记录当前风险状态（PracticeSessionCoordinator 641行/617行超出红线、依赖热点≥12） |

### 1~10 号引擎文件（未在磁盘创建，仅在 13_ARCHITECTURE_GUARD.md 中引用）

| 序号 | 文件名 | 功能 |
|------|--------|------|
| 1 | 1_EVOLUTION_ENGINE.md | 触发检测器 |
| 2 | 2_SYSTEM_HEALTH_ENGINE.md | 健康扫描 |
| 3 | 3_ARCHITECTURE_DRIFT_ANALYZER | 架构漂移检测 |
| 4 | 4_TECH_DEBT_MINER.md | 技术债挖掘 |
| 5 | 5_EVOLUTION_STRATEGY_PLANNER | 演进策略设计 |
| 6 | 6_IMPACT_SIMULATOR.md | 全系统影响模拟 |
| 7 | 7_CONTROLLED_EXECUTION_ENGINE | 逐步执行引擎 |
| 8 | 8_LIVE_REGRESSION_MONITOR.md | 回归校验 |
| 9 | 9_POST_EVOLUTION_VALIDATOR | 稳定性检查 |
| 10 | 10_EVOLUTION_REPORT_ENGINE.md | 最终报告 |

---

## 二、派生内存文件（数据层）

### Agent 入口文件

| 文件 | 功能 | 数据源 |
|------|------|--------|
| **current_state.md** | Agent 统一入口点 — 当前所有活跃 track（6个 track）、系统健康指标（DEGRADED）、代码热点 Top 7（PracticeVM 1982行/98分→ExamVM 663行/45分→QuestionRepositoryImpl 1441行/85分→SettingsScreen 1218行/78分→PracticeScreen 1165行/76分→SettingsVM 1029行/74分→ExamScreen 390行/35分）、模块状态（4个空壳feature模块）、工作流位置（EXECUTE→VERIFY完成）、禁止事项（禁止拆分PracticeVM/K-001修复期间禁止大规模重构）、快速验证命令 | CURRENT_STATE.md + 2_SYSTEM_HEALTH_ENGINE |

### 架构文件

| 文件 | 功能 | 数据源 |
|------|------|--------|
| **architecture_map.md** | 架构静态快照 — 模块分层依赖关系（app→data→domain，单向正确）、Pipeline 六层模型（Load→Normalize→Transform→Interact→Persist→Aggregate）、练习/考试子系统的完整分解进度（6/6 Coordinator 完成）、Testing 映射表（4个测试文件）、重构北星计划（三步走：提取Coordinator→迁移feature模块→进度ID策略迁移到domain） | ARCHITECTURE.md + L6 Architecture Design Reports |

### 代码度量文件

| 文件 | 功能 | 数据源 |
|------|------|--------|
| **file_registry.md** | 热文件索引 — Top 30 文件的 LOC/职责数/注入依赖数/风险等级（🔴EXTREME→🟢LOW）/评分矩阵。涵盖 🔴EXTREME: PracticeVM(1982行/98分); 🔴HIGH: QuestionRepositoryImpl(1441行/85分); 🟠HIGH: SettingsScreen(1218行/78分), PracticeScreen(1165行/76分), SettingsVM(1029行/74分); 🟡MEDIUM 和 🟢LOW 各区。Coordinator 提取进度（6/6 完成，VM从3900→~800行）。关键指标：总225+文件/26276行，6个>1000行文件，2个God VM，最高注入16个依赖 | PROJECT_SCAN + L6 Decomposition Blueprint |
| **dependency_graph.md** | M1 级依赖关系图 — Gradle 显式模块依赖矩阵。Top 热文件耦合矩阵（VM↔Screen 双向耦合、Practice↔Exam 60-70% 代码重复）。5个隐藏耦合点。Pipeline 依赖链。Coordinator 提取顺序链（Step1 Navigation→Step2 Answer→Step3 Progress→Step4 Mode→Step5 FullAnswer→Step6 Session）。M2/M3 待完成：Compose 组件图、Repository→DAO 映射、VM→UseCase 映射、全225+文件依赖树 | PROJECT_SCAN_DEPENDENCY_REPORT |

### 模块文件

| 文件 | 功能 | 数据源 |
|------|------|--------|
| **module_map.md** | 模块地图 — 8 个 Gradle 模块逐模块摘要：`:app`(148文件/21962行/🔴DEGRADED—所有业务代码在此，应委托Practice到:feature-practice、Exam到:feature-exam)、`:data`(49文件/3121行/🟡STABLE—QuestionRepositoryImpl过载)、`:domain`(28文件/1193行/✅HEALTHY—唯一漂移D-002应迁移PracticeProgressScope)、`:feature-practice`(0行/🔴EMPTY STUB)、`:feature-exam`(0行/🔴EMPTY STUB)、`:ui-common`(0行/🔴EMPTY STUB)、`:core`(0行/🔴EMPTY STUB)、`:baseline-profile`(✅OK) | PROJECT_SCAN + ARCHITECTURE.md |

---

## 三、设计报告与分解蓝图

### PracticeViewModel 分解系列

| 文件 | 功能 |
|------|------|
| **architecture_design_report.md** | PracticeViewModel 架构设计总报告 — 3900→724 行方案总览，6 Coordinator 提取设计 |
| **decomposition_blueprint.md** | PracticeViewModel 分解蓝图总版 — 提取步骤、依赖关系、迁移计划 |
| **architecture_design_report_practice_vm_phase_g.md** | PracticeVM 分解第 G 阶段设计 — 激进提取方案（直接持有 _sessionState） |
| **decomposition_blueprint_practice_vm_phase_g.md** | PracticeVM 分解第 G 阶段蓝图 — 具体实现步骤 |
| **architecture_design_report_practice_screen.md** | PracticeScreen 分解设计 — 1279→499 行方案 |
| **decomposition_blueprint_practice_screen.md** | PracticeScreen 分解蓝图 |

### ExamViewModel 分割系列

| 文件 | 功能 |
|------|------|
| **architecture_design_report_exam_vm_phase_e.md** | ExamVM Bounded Context 分割第 E 阶段设计 — 2455→415 行方案 |
| **decomposition_blueprint_exam_vm_phase_e.md** | ExamVM 分割第 E 阶段蓝图 |

### Repository 分解系列

| 文件 | 功能 |
|------|------|
| **architecture_design_report_repo.md** | QuestionRepositoryImpl 分解设计 — 1619→~306 行方案，7 个 Parser/Extractor 提取 |
| **decomposition_blueprint_repo.md** | Repository 分解蓝图 |

### Settings 分解系列

| 文件 | 功能 |
|------|------|
| **architecture_design_report_settings_screen.md** | SettingsScreen 分解设计 — 1359→487 行方案 |
| **decomposition_blueprint_settings_screen.md** | SettingsScreen 分解蓝图 |
| **architecture_design_report_settings_vm.md** | SettingsViewModel 分解设计 — 1178→416 行方案 |
| **decomposition_blueprint_settings_vm.md** | SettingsVM 分解蓝图 |

---

## 四、审计与扫描报告

| 文件 | 功能 |
|------|------|
| **bloat_scan_report_20260613.md** | 膨胀扫描报告（第一轮） — 检测项目中过度膨胀的文件 |
| **bloat_scan_report_20260613_round2.md** | 膨胀扫描报告（第二轮复查） — 验证分解后的效果 |
| **five_traits_review_report_20260613.md** | 五大特征审查报告 — 代码质量五维度审查 |

---

## 五、其他支持文件

| 文件 | 功能 |
|------|------|
| **change_log.md** | 变更日志 — 从 TASK_LOG.md 同步的追加式历史记录 |
| **tech_debt.md** | 技术债清单 — 从 KNOWN_ISSUES.md 和 L6 债挖掘引擎派生 |
| **refactor_candidates.md** | 重构候选 — 冻结评分的重构优先级排名快照，追加式更新 |
| **migration_plan.md** | 迁移计划 — 6 步迁移到 feature 模块的方案 |
| **method_index.md** | 方法索引 — 项目中关键方法的快速查找索引 |
| **fix_detekt.py** | Detekt 静态分析问题自动修复脚本 |
| **fix_all.py** | 批量问题修复脚本 |

---

## 六、Level 6 完整流水线（Mature Workflow）

```
Feature Request
    ↓
Architecture Placement Review   ← GUARD (13_ARCHITECTURE_GUARD.md)
    ↓
Impact Analysis                 ← 6_IMPACT_SIMULATOR
    ↓
Dependency Check               ← dependency_graph.md
    ↓
LOC Check                      ← file_registry.md
    ↓
Responsibility Check           ← architecture_map.md
    ↓
Implementation Plan            ← 5_EVOLUTION_STRATEGY_PLANNER
    ↓
Code                           ← 7_CONTROLLED_EXECUTION_ENGINE
    ↓
Regression Check               ← 8_LIVE_REGRESSION_MONITOR
    ↓
Memory Sync                    ← 11_MEMORY_SYNC_PROTOCOL
    ↓
Update Maps                    ← architecture_map.md + file_registry.md + dependency_graph.md
```

---

## 七、当前项目健康快照

| 指标 | 状态 |
|------|------|
| 耦合度 | 🔴 高（~84% 在 :app） |
| God 文件数 | 🔴 高（10+ 个 >1000 行） |
| 模块独立性 | 🔴 低（4/8 空壳） |
| Practice↔Exam 重复 | 🔴 高（60-70% 相似） |
| 测试覆盖率 | 🟡 中低 |
| 回归频率 | 🟡 中（~8 bugs/30天） |
| KB 稳定性 | 🟢 稳定 |
| Pipeline 深度 | 🟢 ~5 层正常 |

### 已完成的分解成果

| 文件 | 分解前 | 分解后 | 优化比 |
|------|--------|--------|--------|
| PracticeViewModel | 3900 | 724 | -81% |
| ExamViewModel | 2455 | 415 | -83% |
| QuestionRepositoryImpl | 1619 | 306 | -81% |
| SettingsViewModel | 1178 | 416 | -65% |
| SettingsScreen | 1359 | 487 | -64% |
| HomeScreen | 1068 | 399 | -63% |
| PracticeScreen | 1279 | 499 | -61% |
| RichText | 892 | 252 | -72% |
| AppNavHost | 741 | 295 | -60% |

### 仍在红线区

| 文件 | 行数 | 红线 | 状态 |
|------|------|------|------|
| PracticeSessionCoordinator | 641 | 600（Coordinator 红线） | 🟠 已暂停 |
| PracticeNavigationCoordinator | 617 | 600（Coordinator 红线） | 🟠 已暂停 |
| PracticeViewModel | 724 | 800（VM 红线）| 🟡 警告区 |
| PracticeViewModel 依赖数 | ~15 | >8 禁止 | 🔴 已超标 |
| ExamViewModel 依赖数 | ~12 | >8 禁止 | 🔴 已超标 |
