# EVOLUTION STRATEGY PLANNER

> 设计受控的进化策略：明确目标、边界、迁移步骤。

---

## MUST DEFINE

每次进化必须明确：

- **what to improve** — 具体指标
- **why improvement is needed** — 触发原因
- **what modules are affected** — 影响范围
- **what MUST NOT change** — 安全边界

---

## OUTPUT FORMAT

每次进化策略输出以下格式：

```yaml
evolution_id: "EVO-YYYYMMDD-NN"
evolution_goal: "一句话描述目标"
trigger: "触发条件 ID"
safe_boundaries:
  - "不可变更项 1"
  - "不可变更项 2"
risk_level: "LOW | MEDIUM | HIGH | CRITICAL"
migration_plan:
  - step: 1
    action: "具体操作"
    verify: "验证命令"
    rollback: "回滚操作"
  - step: 2
    action: "..."
    verify: "..."
    rollback: "..."
affected_modules:
  - "模块1"
  - "模块2"
```

---

## PROJECT-SPECIFIC SAFE BOUNDARIES

任何进化策略必须遵守以下不可逾越的边界：

| # | 不可变更 | 原因 |
|---|---------|------|
| 1 | Gradle 模块名称 (`:app`, `:domain`, `:data`, etc.) | 引用遍布全项目 |
| 2 | Room `@Entity` 类字段（除非 migration） | 数据完整性 |
| 3 | 已有公共 API 签名（除非 deprecation 路径） | 编译兼容性 |
| 4 | `PracticeProgressScope` 的 `__scope=` 格式 | 影响 Room 行、home 聚合、delete by pattern |
| 5 | `legacyRandomScopedPracticeProgressId` 不删除 | 遗留数据兼容 |
| 6 | 一次仅改一个文件/一个职责 | 可控性 |
| 7 | 改后必须 `compileDebugKotlin` + 单元测试通过 | 质量门 |

---

## EXAMPLE: 首个进化策略（供参考，非自动执行）

```yaml
evolution_id: "EVO-20260611-01"
evolution_goal: "从 PracticeViewModel 提取 NavigationCoordinator 以减少单文件行数"
trigger: "TD-001, DRIFT-002"
safe_boundaries:
  - "不改动 PracticeScreen UI 层"
  - "不改动 PracticeSessionState 数据结构"
  - "不改动进度保存逻辑"
  - "所有现有单元测试必须通过"
risk_level: "MEDIUM"
migration_plan:
  - step: 1
    action: "提取 prevQuestion/nextQuestion 导航逻辑到 PracticeNavigationCoordinator"
    verify: "./gradlew :app:compileDebugKotlin && ./gradlew :app:testDebugUnitTest --tests PracticeViewModelTest"
    rollback: "git checkout -- PracticeViewModel.kt && rm PracticeNavigationCoordinator.kt"
  - step: 2
    action: "提取 answered-history 管理逻辑到 coordinator"
    verify: "同上 + 手动验证设备端 history 行为"
    rollback: "git checkout -- PracticeViewModel.kt"
  - step: 3
    action: "提取进度保存协调逻辑到 coordinator"
    verify: "同上 + HomeViewModelTest, ProgressScopeTest"
    rollback: "git checkout -- PracticeViewModel.kt"
affected_modules:
  - ":app"
```
