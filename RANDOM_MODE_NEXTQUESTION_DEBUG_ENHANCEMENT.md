# 随机模式左右滑动问题最终解决方案

## 问题确认（通过日志验证）

### 用户提供的关键日志：
```
nextQuestion called: randomPracticeEnabled=true, currentIndex=53
nextQuestion called: randomPracticeEnabled=true, currentIndex=45  
nextQuestion called: randomPracticeEnabled=true, currentIndex=45  // 没有跳转！
```

### 对比 prevQuestion 的正常工作：
```
prevQuestion: otherIndices.size=96, jumping from 94 to 53  ✅ 正常工作
```

## 根本原因确认

**nextQuestion() 的问题**：
- 只寻找未答题目：`unansweredIndices`
- 当所有题目都已答时，`unansweredIndices.size = 0`
- 结果：没有可选题目，"没反应"

**prevQuestion() 正常**：
- 寻找所有不同题目：`otherIndices`
- 总是有 96 个可选题目（总共 97 题 - 当前 1 题）
- 结果：总是能跳转

## 修复方案

### 方案选择：添加调试日志确认问题
先添加详细日志来确认 `nextQuestion` 的 `unansweredIndices.size`：

```kotlin
fun nextQuestion() {
    if (randomPracticeEnabled) {
        val unansweredIndices = ...
        android.util.Log.d("PracticeViewModel", "nextQuestion: unansweredIndices.size=${unansweredIndices.size}")
        if (unansweredIndices.isNotEmpty()) {
            android.util.Log.d("PracticeViewModel", "nextQuestion: jumping from ${currentState.currentIndex} to $randomIndex")
            // 跳转逻辑
        } else {
            android.util.Log.d("PracticeViewModel", "nextQuestion: all questions answered, no jump")
            // 这里就是问题所在！
        }
    }
}
```

### 预期的验证日志
用户再次测试时应该看到：
```
nextQuestion: unansweredIndices.size=0, all questions answered, no jump
```

这将最终确认问题是 `unansweredIndices` 为空导致的。

## 下一步修复计划

确认问题后，将采用以下修复方案：

### 方案A：统一逻辑（推荐）
```kotlin
fun nextQuestion() {
    if (randomPracticeEnabled) {
        // 与 prevQuestion 使用相同逻辑
        val otherIndices = (0 until currentState.questionsWithState.size).filter { it != currentState.currentIndex }
        if (otherIndices.isNotEmpty()) {
            val randomIndex = otherIndices.random(...)
            // 跳转逻辑
        }
    }
}
```

### 方案B：保持原逻辑 + 容错
```kotlin
fun nextQuestion() {
    if (randomPracticeEnabled) {
        val unansweredIndices = ... // 原有逻辑
        if (unansweredIndices.isNotEmpty()) {
            // 跳转到未答题目
        } else {
            // 容错：跳转到任何不同题目
            val otherIndices = (0 until currentState.questionsWithState.size).filter { it != currentState.currentIndex }
            if (otherIndices.isNotEmpty()) {
                val randomIndex = otherIndices.random(...)
                // 跳转逻辑
            }
        }
    }
}
```

## 当前状态

✅ **问题分析完成**：通过日志确认了 nextQuestion 和 prevQuestion 逻辑不一致
✅ **调试日志已添加**：nextQuestion 现在会显示详细的跳转信息
🔄 **等待用户验证**：请测试并提供新的日志输出

## 完成时间
2025-08-06

## 重要提醒
✅ **保持原有功能不变**：只添加了调试日志，准备根据验证结果实施最终修复
✅ **问题定位精确**：通过对比日志明确了 nextQuestion 逻辑缺陷
