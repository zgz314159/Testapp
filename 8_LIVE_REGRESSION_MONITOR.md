# LIVE REGRESSION MONITOR

> 进化执行期间实时监控系统状态。

---

## CHECKS (每步后执行)

| 检查项 | 验证方式 | 失败阈值 |
|--------|---------|---------|
| **KB integrity** | Room schema 未变 | 任何 schema 变更 |
| **pipeline correctness** | 数据流向不变（Load→Normalize→Transform→Interact→Persist→Aggregate） | 流程中断或短路 |
| **Android compatibility** | `compileDebugKotlin` 通过 | 编译失败 |
| **Unit test regression** | 原有测试全部通过 | 任何已有测试失败 |
| **No new warnings** | 编译 warning 数量未增加 | warning 数量增加 > 0 |

---

## MONITORING PROTOCOL

```
Step N 执行完毕
    ↓
Gate 1: compileDebugKotlin ─── 失败 → ROLLBACK
    ↓ 通过
Gate 2: 核心单元测试 ───────── 失败 → ROLLBACK
    ↓ 通过
Gate 3: 全部单元测试 ───────── 失败 → ROLLBACK
    ↓ 通过
Gate 4: Git commit
    ↓
下一步 Step N+1
```

---

## FAILURE RESPONSE

若任何检查失败：

1. **STOP evolution immediately** — 不继续下一步
2. **ROLLBACK** to last stable state — `git checkout`
3. **LOG** failure in `TASK_LOG.md` with:
   - 失败的 step 编号
   - 具体错误信息
   - 根因分析
4. **UPDATE** evolution plan — 调整策略后重试

---

## CONTINUOUS VERIFICATION DURING EVOLUTION

| 阶段 | 验证内容 | 频率 |
|------|---------|------|
| 开发中 | 当前文件编译通过 | 每保存 |
| Step 完成 | 全局编译 | 每 step |
| Step 完成 | 核心单元测试 | 每 step |
| Step 完成 | 全部单元测试 | 每 step |
| 多步后 | 设备 smoke (如适用) | 每 3-5 steps |

---

## STABILITY SIGNALS

| 信号 | 含义 |
|------|------|
| 连续 3 steps 无 rollback | 进化路径稳定 |
| 编译时间未显著增加 | 未引入重度依赖 |
| 测试全部通过且无新增 skip | 回归保护有效 |
| Git diff 聚焦在目标文件 | 未范围蔓延 |
