# 随机模式左滑状态更新修复

## 问题诊断

### 根据日志分析发现的问题：
```
2025-08-06 10:18:23.129 prevQuestion: jumping from 78 to 24
2025-08-06 10:18:26.105 prevQuestion: jumping from 78 to 24  // 又回到78！
2025-08-06 10:18:26.548 prevQuestion: jumping from 24 to 26
```

**核心问题**：虽然 `prevQuestion()` 方法被调用并计算出了正确的随机索引，但是状态更新没有生效！第二次调用时仍然从78开始，说明 `currentIndex` 没有真正更新。

## 根本原因

1. **竞态条件**：`prevQuestion()` 中状态更新后立即调用 `saveProgress()`
2. **异步冲突**：`saveProgress()` 是异步方法，可能与状态更新产生竞态
3. **状态覆盖**：`saveProgress()` 使用 `_sessionState.value` 可能获取到更新前的状态

## 修复方案

### 1. 创建专用的状态保存方法
```kotlin
private suspend fun saveProgressWithState(state: PracticeSessionState) {
    // 使用传入的确定状态，而不是当前可能过时的状态
    val progress = PracticeProgress(
        currentIndex = state.currentIndex, // 关键：使用传入的状态
        // ... 其他字段
    )
    savePracticeProgressUseCase(progress)
}
```

### 2. 修复 prevQuestion() 方法
```kotlin
fun prevQuestion() {
    val currentState = _sessionState.value
    
    if (randomPracticeEnabled) {
        val otherIndices = (0 until currentState.questionsWithState.size).filter { it != currentState.currentIndex }
        if (otherIndices.isNotEmpty()) {
            val randomIndex = otherIndices.random(...)
            val newState = currentState.copy(currentIndex = randomIndex)  // 创建新状态
            _sessionState.value = newState  // 立即更新UI状态
            viewModelScope.launch {
                saveProgressWithState(newState)  // 使用确定的新状态保存
            }
        }
    }
}
```

## 修复要点

1. **状态一致性**：确保UI更新和数据库保存使用相同的状态
2. **异步安全**：避免异步操作中的状态竞态条件
3. **立即生效**：UI状态更新立即生效，数据持久化异步进行

## 预期效果

修复后的行为：
```
用户左滑 → prevQuestion() 调用 → 立即跳转到新题目 → 异步保存状态
```

而不是之前的：
```
用户左滑 → prevQuestion() 调用 → 状态更新被覆盖 → 没有跳转效果
```

## 验证方法

测试时观察日志应该显示：
```
prevQuestion: jumping from 78 to 24
prevQuestion: jumping from 24 to 26  // 从24开始，说明状态正确更新了
```

## 完成时间
2025-08-06

## 重要提醒
✅ **保持原有功能不变**：只修复了状态更新竞态问题，所有原有逻辑保持不变
✅ **关键修复**：解决了左滑无反应的根本原因
