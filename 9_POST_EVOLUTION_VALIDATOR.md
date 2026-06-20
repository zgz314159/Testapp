# POST EVOLUTION VALIDATOR

> 进化完成后，全面验证系统稳定性和改善效果。

---

## VERIFY

进化完成后必须确认：

### 1. SYSTEM STABILITY

| 检查项 | 验证方式 | 期望结果 |
|--------|---------|---------|
| 全局编译 | `./gradlew :app:compileDebugKotlin` | SUCCESS |
| 全部单元测试 | `./gradlew :app:testDebugUnitTest` | ALL PASS |
| 无新增 lint 错误 | IDE lint check | 0 new errors |
| Room schema 不变 | 对比 `app/schemas/` | No diff |
| DI 注入正确 | 编译通过即验证 | — |

### 2. PERFORMANCE IMPROVEMENT

| 指标 | 进化前 | 进化后 | 改善 |
|------|--------|--------|------|
| God file 行数 | 记录原值 | 记录新值 | 差值 |
| 模块行数分布 | 记录原值 | 记录新值 | 分布改善 |
| 编译时间 | 记录原值 | 记录新值 | 无显著退化 |

### 3. NO REGRESSION

| 检查项 | 验证方式 |
|--------|---------|
| 原有功能正常 | 设备端 smoke（如适用） |
| 单元测试覆盖率未降 | 对比进化前后测试数量 |
| 无新增已知问题 | 检查 `KNOWN_ISSUES.md` 无新增项 |

### 4. ARCHITECTURE ALIGNMENT

| 检查项 | 验证方式 |
|--------|---------|
| 进化方向符合 `ARCHITECTURE.md` 目标 | 对比 North Star |
| 未引入新的 Drift | 运行 Architecture Drift Analyzer |
| 模块依赖方向正确 | 确认无循环依赖 |

---

## OUTPUT

### PASS ✅

```
✅ Post-Evolution Validation: PASSED
- System stability: CONFIRMED
- Performance: IMPROVED (God file lines: 3900→3200)
- Regressions: NONE
- Architecture alignment: ON TRACK
```

### FAIL ❌

```
❌ Post-Evolution Validation: FAILED
- Reason: [具体失败原因]
- Action: ROLLBACK and RE-PLAN
```

---

## IMPROVEMENT SCORE

| 维度 | 权重 | 得分 (0-10) |
|------|------|------------|
| 代码可读性 | 25% | — |
| 模块独立性 | 25% | — |
| 测试覆盖 | 20% | — |
| 构建稳定性 | 15% | — |
| 架构一致性 | 15% | — |
| **总分** | 100% | **—** |

---

## POST-VALIDATION ACTIONS

1. ✅ 更新 `CURRENT_STATE.md` — 反映新状态
2. ✅ 更新 `TASK_LOG.md` — 记录进化完成
3. ✅ 更新 `KNOWN_ISSUES.md` — 关闭已解决的 issue
4. ✅ 更新 `REFACTOR_TODO.md` — 标记已完成的 todo
5. ✅ 生成 Evolution Report → `10_EVOLUTION_REPORT_ENGINE.md` template
