# EVOLUTION REPORT ENGINE

> 每次进化周期结束后生成最终报告。

---

## REPORT TEMPLATE

```markdown
# Evolution Report: EVO-YYYYMMDD-NN

**Date:** YYYY-MM-DD
**Status:** COMPLETED | ROLLED_BACK | CANCELLED

---

## 1. WHAT CHANGED

- [变更项 1]
- [变更项 2]
- ...

## 2. WHY IT CHANGED

- [触发原因: TD-xxx, DRIFT-xxx]
- [预期收益]

## 3. MODULES AFFECTED

| 模块 | 变更类型 | 新增文件 | 修改文件 |
|------|---------|---------|---------|
| :app | MODIFIED | — | PracticeViewModel.kt |
| :domain | NEW | — | — |

## 4. STEPS EXECUTED

| Step | Action | Gate 1 (编译) | Gate 2 (核心测试) | Gate 3 (全部测试) | Status |
|------|--------|-------------|-----------------|-----------------|--------|
| 1 | [描述] | ✅ | ✅ | ✅ | DONE |
| 2 | [描述] | ✅ | ✅ | ✅ | DONE |

## 5. RISK ENCOUNTERED

| 风险 | 实际发生？ | 缓解措施 |
|------|----------|---------|
| [风险描述] | YES/NO | [措施] |

## 6. ROLLBACK EVENTS

| Step | 原因 | 根因 | 是否重试 |
|------|------|------|---------|
| — | — | — | — |

(若无则填 "NONE")

## 7. IMPROVEMENT OUTCOME

| 指标 | 进化前 | 进化后 | 改善率 |
|------|--------|--------|--------|
| PracticeViewModel 行数 | 3900 | 3200 | -18% |
| 模块独立性 | 4/8 空壳 | 3/8 空壳 | +12.5% |
| 单元测试数 | 45 | 45 | 0% (未降) |

## 8. IMPROVEMENT SCORE

| 维度 | 得分 |
|------|------|
| 代码可读性 | 7/10 |
| 模块独立性 | 5/10 |
| 测试覆盖 | 4/10 |
| 构建稳定性 | 8/10 |
| 架构一致性 | 7/10 |
| **总分** | **6.2/10** |

## 9. POST-EVOLUTION MEMORY UPDATE

- [x] `CURRENT_STATE.md` updated
- [x] `TASK_LOG.md` updated
- [x] `KNOWN_ISSUES.md` updated
- [x] `REFACTOR_TODO.md` updated

## 10. NEXT RECOMMENDED EVOLUTION

- [下一个进化建议，基于本次结果]
```

---

## REPORT STORAGE

每次进化报告保存到根目录：

```
EVOLUTION_REPORT_EVO-YYYYMMDD-NN.md
```

同时追加摘要到 `TASK_LOG.md`。
