<!--
  生成时间: 2026-06-14 09:37 UTC+8
  分析范围: 完整 PROJECT_ROOT（所有源码 + .ai/ 系统文件）
  包含: Level 6 自进化工程操作系统 + 全部 Kotlin 源文件 + 架构健康评估
-->

# 项目架构总览分析

---

## 快速导航

| 文档 | 内容 |
|------|------|
| [file_analysis_level6_system.md](file_analysis_level6_system.md) | Level 6 系统全部文件功能详细分析 |
| [file_analysis_code_structure.md](file_analysis_code_structure.md) | 全部 Kotlin 源代码文件功能详细分析 |
| 本文档 | 项目架构总览汇总 |

---

## 一、项目概述

**项目名**: Testapp — Android 题库练习/考试应用
**技术栈**: Kotlin + Jetpack Compose + MVVM + Room + Hilt DI

### 核心功能
- 多格式题库文件导入（TXT/DOCX/SQLite/JSON/Excel）
- 练习模式：顺序/随机/全答案/记忆模式
- 考试模式：计时/自动评分
- 错题本、收藏夹、AI 分析（DeepSeek/讯飞星火/百度千帆）
- 首页文件卡片进度聚合、拖拽导入
- 字体/暗色模式/Fill 配置等个性化设置

---

## 二、模块分层架构

```
┌─────────────────────────────────────────────────────────────────┐
│  :app (148 files, 21,962 lines) — Application Shell            │
│  ├── presentation/screen/        ← Screen + VM + Coordinators  │
│  │   ├── Coordinator × 17        ← 已提取的子协调器               │
│  │   ├── ViewModel × 10          ← 薄委托层                     │
│  │   ├── Screen × 7              ← Compose UI                 │
│  │   └── components/             ← 共享 UI 组件 (17 files)     │
│  ├── presentation/navigation/    ← AppNavHost                  │
│  ├── data/datastore/             ← DataStore                   │
│  ├── data/network/               ← AI API Services             │
│  ├── di/                         ← Hilt Modules                │
│  ├── domain/model/               ← 部分遗留域模型               │
│  └── domain/usecase/             ← 部分遗留 UseCase             │
├─────────────────────────────────────────────────────────────────┤
│  :data (60 files, 3,121 lines) — Persistence & IO             │
│  ├── repository/                 ← Repository Impl × 12        │
│  │   └── parser/                 ← File Parsers × 6            │
│  ├── local/                      ← Room DB + DAO × 10 + Entity × 12 │
│  └── mapper/                     ← Data Mappers × 3            │
├─────────────────────────────────────────────────────────────────┤
│  :domain (28 files, 1,193 lines) — Business Logic (Clean)     │
│  ├── model/                      ← Domain Models × 13          │
│  ├── repository/                 ← Repository Interfaces × 9   │
│  ├── usecase/                    ← Use Cases × 1+              │
│  └── util/                       ← Pure Utils × 3              │
├─────────────────────────────────────────────────────────────────┤
│  :feature-practice (0 lines) — 🔴 EMPTY STUB                   │
│  :feature-exam     (0 lines) — 🔴 EMPTY STUB                   │
│  :ui-common        (0 lines) — 🔴 EMPTY STUB                   │
│  :core             (0 lines) — 🔴 EMPTY STUB                   │
│  :baseline-profile              — ✅ Performance Baseline      │
└─────────────────────────────────────────────────────────────────┘
```

**依赖方向**: `app → data → domain`（单向，无循环依赖 ✅）

---

## 三、Coordinator 提取成果

### Practice 系列 (9/9 完成)

| Coordinator | 行数 | 核心职责 | 状态 |
|-------------|------|----------|------|
| PracticeNavigationCoordinator | ~617 | 导航编排（前进/后退/历史浏览/随机导航） | 🟡 红线边缘 |
| PracticeAnswerHandler | ~213 | 纯答案判断（正确/完成/待处理/候选计算） | 🟢 |
| PracticeProgressCoordinator | ~99 | Fill 签名管理/进度快照构建 | 🟢 |
| PracticeModeCoordinator | ~394 | 记忆模式引擎（轮次计划/池刷新/题目移除/轮次推进） | 🟡 |
| PracticeFullAnswerCoordinator | ~112 | 全答案模式（顺序恢复/配置恢复/Fill检测） | 🟢 |
| PracticeSessionCoordinator | ~641 | 会话管理+持久化（save/load/分析载入/错题/收藏） | 🟡 红线边缘 |
| PracticeSubmitCoordinator | ~70 | 提交流程（延迟/记录/跳转） | 🟢 |
| PracticeInteractionCoordinator | ~140 | 用户交互（选项/输入/显示答案/重开题目） | 🟢 |
| PracticeArtifactCoordinator | ~180 | 分析产物管理（3种AI分析+笔记） | 🟢 |
| PracticeEditorCoordinator | ~250 | 题目编辑器（编辑/提交/Fill变换） | 🟢 |

### Exam 系列 (7/7 完成)

| 组件 | 行数 | 核心职责 |
|------|------|----------|
| ExamAnswerRules | 20 | 答案判断 |
| ExamFillTransform | 81 | Fill变换 |
| ExamMemoryModeEngine | 48 | 记忆模式 |
| ExamNavigationHelper | 130 | 导航辅助 |
| ExamLoadDelegate | 182 | 题目加载 |
| ExamProgressCoordinator | — | 进度持久化 |
| ExamArtifactCoordinator | — | 产物管理 |

---

## 四、God Class 分解历程

| 文件 | 分解前 | 分解后 | 优化比 |
|------|--------|--------|--------|
| PracticeViewModel | 3900 | 724 | **−81%** |
| ExamViewModel | 2455 | 415 | **−83%** |
| QuestionRepositoryImpl | 1619 | 306 | **−81%** |
| SettingsViewModel | 1178 | 416 | **−65%** |
| SettingsScreen | 1359 | 487 | **−64%** |
| HomeScreen | 1068 | 399 | **−63%** |
| PracticeScreen | 1279 | 499 | **−61%** |
| RichText | 892 | 252 | **−72%** |
| AppNavHost | 741 | 295 | **−60%** |

**总计**: 16,491 行 God code → 3,793 行（Coordinatorized）+ 薄层 VM/Screen = **约 52,000 → 26,276 行，消除冗余 ~50%**

---

## 五、Level 6 自进化工程操作系统

### 核心思想
```
特征请求 → 架构门控(13) → 影响模拟(6) → 依赖检查 → LOC检查 → 职责检查
    → 实施计划(5) → 代码(7) → 回归检查(8) → 内存同步(11) → 更新地图
```
**NOT**: 特征请求 → 代码（Old Way）

### 已激活文件

| 编号 | 文件 | 角色 |
|------|------|------|
| 11 | MEMORY_SYNC_PROTOCOL | 内存同步协议 |
| 12 | CONTEXT_LOADING_RULES | Token预算管理 |
| 13 | ARCHITECTURE_GUARD | 架构守门员（实施前强制门控） |
| — | current_state.md | Agent入口点 |
| — | architecture_map.md | 架构快照 |
| — | dependency_graph.md | 依赖图 |
| — | file_registry.md | 热文件索引 |
| — | module_map.md | 模块地图 |
| — | tech_debt.md | 技术债 |
| — | refactor_candidates.md | 重构候选 |
| — | change_log.md | 变更日志 |

### 架构红线

| 类别 | 🟢 Safe | 🟡Warning | 🟠Danger | 🔴Forbidden |
|------|---------|-----------|----------|-------------|
| ViewModel | ≤400 | 401-600 | 601-800 | >800 |
| Repository | ≤500 | 501-700 | 701-1000 | >1000 |
| Screen | ≤500 | 501-800 | 801-1000 | >1000 |
| Coordinator | ≤300 | 301-500 | 501-600 | >600 |
| 依赖数 | ≤5 | 6-8 | — | >8 |
| 职责数 | — | — | — | >3 |

---

## 六、当前架构健康

| 指标 | 状态 | 详情 |
|------|------|------|
| 耦合度 | 🔴 HIGH | 84% 代码在 :app |
| God 文件 | 🟡 改善中 | 原 6 个 >1000 行，已消除 4 个 |
| 模块独立 | 🔴 LOW | 4/8 空壳 |
| 代码重复 | 🔴 HIGH | Practice↔Exam 60-70% 相似 |
| 测试覆盖 | 🟡 LOW-MEDIUM | 仅 4 个测试文件 |
| Pipeline | 🟢 OK | 5 层正常 |

### 仍在红线区的文件

| 文件 | 行数 | 红线 | 状态 |
|------|------|------|------|
| PracticeSessionCoordinator | 641 | 600 | 🔴 需拆分 |
| PracticeNavigationCoordinator | 617 | 600 | 🔴 需拆分 |
| PracticeViewModel 依赖 | ~15 | 8 | 🔴 依赖超标 |
| ExamViewModel 依赖 | ~12 | 8 | 🔴 依赖超标 |

---

## 七、待办事项

1. **K-001 设备冒烟测试**：首页卡片持久化 + 原子题库重入
2. **Coordinator 红线修复**：PracticeSessionCoordinator(641→拆) + PracticeNavigationCoordinator(617→拆)
3. **模块迁移**：将 Practice Coordinator + Screen 迁移到 `:feature-practice`
4. **依赖精简**：引入 Facade/UseCase 降低 VM 依赖数至 ≤8
5. **测试补全**：Coordinator 单元测试覆盖
6. **1~10号引擎文件创建**：将流水线规则落地为实际文件
