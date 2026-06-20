# TECH DEBT MINER

> 识别累积的系统低效，按优先级排序。

---

## DETECTED DEBT INVENTORY

### 🔴 HIGH PRIORITY — 阻碍大规模重构

| ID | 描述 | 位置 | 量级 | 关联 |
|----|------|------|------|------|
| TD-001 | PracticeViewModel 超限 | `PracticeViewModel.kt` | ~3900 行 | K-002, DRIFT-002 |
| TD-002 | 设备 smoke 未跑 | 设备测试 | 1 项待验证 | K-001 |
| TD-003 | 无 CI/CD | 工程配置 | 无自动验证 | D-003 |

### 🟡 MEDIUM PRIORITY — 应在下次重构中处理

| ID | 描述 | 位置 | 量级 | 关联 |
|----|------|------|------|------|
| TD-004 | ExamViewModel 超限 | `ExamViewModel.kt` | ~2455 行 | K-003, DRIFT-002 |
| TD-005 | 功能模块空壳 | `feature-practice`, `feature-exam` | 0 行业务代码 | D-001, DRIFT-001 |
| TD-006 | duplicated logic (Practice↔Exam) | 两个 VM 中 | 大量平行代码 | — |
| TD-007 | SettingsScreen 超限 | `SettingsScreen.kt` | ~1218 行 | — |
| TD-008 | PracticeScreen 超限 | `PracticeScreen.kt` | ~1165 行 | — |
| TD-009 | SettingsViewModel 超限 | `SettingsViewModel.kt` | ~1029 行 | — |

### 🟢 LOW PRIORITY — 可延后但不应忘记

| ID | 描述 | 位置 | 量级 | 关联 |
|----|------|------|------|------|
| TD-010 | 缺失 ktlint/detekt | 工程配置 | — | D-003 |
| TD-011 | Progress scope 在 presentation 层 | `PracticeProgressScope.kt` | 策略位置不对 | D-002, DRIFT-003 |
| TD-012 | 根目录 SUMMARY 文件过多 | 根目录 | ~20 个 `*_SUMMARY.md` | D-004 |
| TD-013 | VM line counts stale | `PROJECT_SCAN_DEPENDENCY_REPORT.md` | 基线过期 | D-005 |

---

## DUPLICATION HOTSPOTS

| 区域 | 重复模式 | 建议 |
|------|---------|------|
| `PracticeViewModel` ↔ `ExamViewModel` | 导航状态管理、进度保存、答案处理 | 提取共享 coordinator 或 base class |
| `PracticeScreen` ↔ `ExamScreen` | 题目渲染、选项展示、手势处理 | 提取共享 Compose 组件到 `ui-common` |
| 多个 Screen | 文件导入流程、进度显示 | 统一到 `SettingsViewModel` 或提取独立 use case |

---

## SUGGESTED FIX ORDER

1. **先跑 K-001 设备 smoke** — 确保当前系统稳定
2. **配置 CI + ktlint/detekt** (TD-003, TD-010) — 建立自动化防线
3. **提取 PracticeViewModel coordinator** (TD-001) — 渐进式拆分，每次一个职责
4. **迁移到 feature-practice** (TD-005) — 按 Screen 逐步迁移
5. **复用模式到 Exam** (TD-004, TD-006) — 用已验证模式处理第二个 VM
