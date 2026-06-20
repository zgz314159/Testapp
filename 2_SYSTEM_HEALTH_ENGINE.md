# SYSTEM HEALTH ENGINE

> 持续评估系统状态，判定是否允许进化。

---

## METRICS

| 指标 | 计算方式 | 本项目基准 (2026-05-27) |
|------|---------|------------------------|
| **coupling score** | `:app` 中代码占比 | ~84% (21962/26276 lines) → **HIGH** |
| **module independence** | 空壳模块数 / 总模块数 | 4/8 → **LOW** |
| **pipeline depth** | 最大调用链长度 (UI→VM→UC→Repo→DB) | ~5 层 → OK |
| **KB schema stability** | Room schema 自上次 migration 的变更次数 | 稳定 → OK |
| **duplication ratio** | 相似代码块数 / 总代码块 | PracticeVM↔ExamVM 高度平行 → **MEDIUM** |
| **regression frequency** | 近 30 天引入的回归 bug 数 | ~8 个各类 fix → **MEDIUM** |
| **test coverage** | 有单元测试的关键路径占比 | PracticeVM / HomeVM / Exam 评分 → **LOW-MEDIUM** |
| **God file count** | >1000 行文件数 | 10+ → **HIGH** |

---

## STATES

| 状态 | 条件 | 允许进化？ |
|------|------|----------|
| **HEALTHY** | 所有指标在阈值内 | ✅ 允许 |
| **STABLE** | 1-2 个指标预警，无 HIGH severity 债 | ✅ 允许（谨慎） |
| **DEGRADED** | 3+ 个指标预警 或 1 个 HIGH severity 债 | ⚠️ 允许（需额外审批） |
| **CRITICAL** | 编译失败 / 设备测试失败 / 数据损坏 | ❌ **NO EVOLUTION ALLOWED** |

### 本项目当前状态: **DEGRADED**

**原因:**
- K-002 (`PracticeViewModel` ~3900 行, HIGH)
- coupling score HIGH
- God file count HIGH
- K-001 设备 smoke 未完成

**可执行**: 仅允许对已标记债的**小范围渐进式重构**，禁止大规模架构重组。

---

## HEALTH CHECK COMMANDS

```bash
# 编译检查
./gradlew :app:compileDebugKotlin

# 单元测试
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.PracticeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.HomeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.ProgressScopeTest"

# 行数统计（更新指标）
git ls-files "*.kt" | xargs wc -l | sort -rn | head -20
```
