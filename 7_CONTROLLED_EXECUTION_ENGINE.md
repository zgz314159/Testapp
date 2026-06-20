# CONTROLLED EXECUTION ENGINE

> 逐步执行进化，每步验证，不稳定即回滚。

---

## RULES

1. **execute one step at a time** — 每次只执行迁移计划中的一个 step
2. **validate after each step** — 编译 + 单元测试 +（如适用）设备验证
3. **rollback if instability detected** — 任何失败立即回滚到上一步稳定状态
4. **no full rewrite** — 禁止一次性重写整个文件
5. **no global restructuring** — 禁止无计划的大规模结构调整
6. **commit after each step** — 每步成功即 commit，确保可单独回滚

---

## EXECUTION STYLE

### ✅ 允许的重构模式

```kotlin
// ✅ 提取方法到 coordinator
class PracticeNavigationCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>
) {
    fun goToNext() { /* 从 VM 迁移来的逻辑 */ }
    fun goToPrev() { /* 从 VM 迁移来的逻辑 */ }
}

// VM 中改为委托
class PracticeViewModel {
    private val navigationCoordinator = PracticeNavigationCoordinator(_sessionState)
    fun nextQuestion() = navigationCoordinator.goToNext()
}
```

### ❌ 禁止的重构模式

```kotlin
// ❌ 一次性重写整个 ViewModel
class PracticeViewModel { // 全新重写，无法评估影响
    // ... 500 行新代码 ...
}
```

```kotlin
// ❌ 同时改多个文件
// Step 1: 同时改 PracticeViewModel + PracticeScreen + ExamViewModel → 禁止
```

---

## ROLLBACK PROTOCOL

| 失败类型 | 回滚操作 | 验证 |
|---------|---------|------|
| **编译失败** | `git checkout -- <changed_file>` | `./gradlew :app:compileDebugKotlin` |
| **测试失败** | `git checkout -- <changed_file>` | 重新运行失败测试 |
| **设备行为异常** | `git checkout -- <changed_file>` + 重新安装 APK | 设备端验证 |
| **未知异常** | `git reset --hard HEAD~1` | 完整编译+测试+设备验证 |

### 回滚后必须

1. 记录失败原因到 `TASK_LOG.md`
2. 更新进化报告标记为 ROLLED_BACK
3. 分析根因后再设计新策略

---

## ARCHITECTURE GATE REVIEW (每 Step 完成后执行)

> **目标**: 防止 "God Object A → God Object B, C, D"。按 Bounded Context 而非代码行数拆分。

### Gate Review 检查项

对每个新创建/修改的文件，检查:

| 检查维度 | 阈值 | 超过时处理 |
|---------|------|-----------|
| **职责数** | ≤3 | 拆分文件 |
| **外部依赖数** | ≤8 (含注入 + 回调) | 重新审视职责边界 |
| **LOC** | ≤350 行 | 寻找未分离的子职责 |
| **是否单一 Bounded Context** | 是 | 若为 "Utility Bag"，标记 🔴 FAIL |

### 判定标准

```
🟢 PASS:   ≤3职责, ≤8依赖, ≤350行, 单一BoundedContext
🟡 PASS:   略有超标但有明确的单一职责 (如: 多个小helpers围绕同一概念)
🔴 FAIL:   多职责 / Utility Bag / God Object B → 必须拆分后再继续
```

### Gate Review 输出格式

```markdown
| 文件 | LOC | 职责 | 依赖 | 判定 |
|------|-----|------|------|------|
| NewFileA.kt | 120 | 1 | 2 | 🟢 PASS |
| NewFileB.kt | 302 | 10 | 1 | 🔴 FAIL — Utility Bag |
```

### Gate Review 失败后的处理

1. **Stop**: 不继续下一步
2. **Redesign**: 对 FAIL 文件重新分析 Bounded Context
3. **Re-split**: 将 FAIL 文件进一步拆分
4. **Re-Gate**: 重新验证直到 PASS

---

## EXECUTION CHECKLIST (每步完成后验证)

```bash
./gradlew :app:compileDebugKotlin

# ✅ Gate 2: 核心单元测试通过
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.PracticeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.HomeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "com.example.testapp.presentation.screen.ProgressScopeTest"

# ✅ Gate 3: 全部单元测试通过
./gradlew :app:testDebugUnitTest

# ✅ Gate 4: Git commit
git add -A && git commit -m "evolution(EVO-xxx): step N - description"
```

---

## INCREMENTAL REFACTOR ONLY

| 允许 | 禁止 |
|------|------|
| 提取一个类/函数 | 重写整个文件 |
| 移动一个文件到另一个模块 | 重组模块结构 |
| 添加一个 use case | 删除一个 use case（除非确认无引用）|
| 修改 private/internal 方法 | 修改 public API（除非 deprecation 路径） |
