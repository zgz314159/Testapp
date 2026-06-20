# EVOLUTION ENGINE

> 核心进化引擎：定义何时触发架构进化、进化必须遵循的原则。

---

## EVOLUTION TRIGGERS

Evolution is triggered **ONLY** when:

| 触发条件 | 本项目检测方式 |
|----------|---------------|
| repeated code duplication detected | `PracticeViewModel` 与 `ExamViewModel` 高度平行的重复逻辑 |
| pipeline complexity exceeds threshold | 单文件 >3000 行 / 单函数 >100 行 |
| module coupling becomes HIGH | `:app` 承载全部业务代码，`feature-*` 模块空壳 |
| KB schema growth causes instability | Room entity 变更未配 migration |
| OCR / LLM pipeline inefficiency detected | 重复 LLM 调用 / 无缓存 |
| repeated bug patterns occur | 同类型 bug 在多个路由出现（如 answered-history 状态不一致） |

---

## CORE PRINCIPLE

**Evolution is NOT automatic.**

It requires:

1. **Detection** — 通过 System Health Scan 确认问题真实存在且可量化
2. **Validation** — 通过 Safety Gate 确认系统状态允许进化（非 CRITICAL）
3. **Approval simulation** — 通过 Impact Simulator 预测变更影响
4. **Safe execution plan** — 通过 Controlled Execution Engine 逐步执行

---

## PROJECT-SPECIFIC TRIGGER MAP

基于当前项目状态 (`KNOWN_ISSUES.md`, `PROJECT_SCAN_DEPENDENCY_REPORT.md`):

| 已激活触发 | 严重度 | 关联债务 |
|-----------|--------|---------|
| `PracticeViewModel` ~3900 行 | **HIGH** | K-002 |
| `ExamViewModel` ~2455 行 | MEDIUM | K-003 |
| 业务代码集中在 `:app`; `feature-*` 空壳 | MEDIUM | D-001 |
| 无 CI / ktlint / detekt | MEDIUM | D-003 |
| 多次 answered-history 相关 bug | 已修复但需监控 | P-001 |

---

## WHAT IS NOT A TRIGGER

- 用户说"重构一下"但没有具体问题 → NOT a trigger
- 单次偶发 bug → NOT a trigger（需 repeated bug patterns）
- 个人代码风格偏好 → NOT a trigger
- 新功能需求 → NOT a trigger（走正常开发流程）
