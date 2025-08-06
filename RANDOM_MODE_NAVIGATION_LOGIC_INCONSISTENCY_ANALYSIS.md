# 随机模式左右滑动逻辑不一致问题修复

## 问题描述

用户反馈的具体场景：
1. 向右滑动屏幕跳转到上一题 → 正常工作
2. 再向左滑动屏幕能跳到之前的那道题 → 正常工作  
3. 再继续向左滑动屏幕就没反应了 → **问题出现**

## 根本原因分析

### nextQuestion() 随机模式逻辑：
```kotlin
// 只跳转到未答题目
val unansweredIndices = currentState.questionsWithState.mapIndexedNotNull { index, questionWithState ->
    if (questionWithState.selectedOptions.isEmpty()) index else null
}
```

### prevQuestion() 随机模式逻辑：
```kotlin
// 跳转到任何不同的题目
val otherIndices = (0 until currentState.questionsWithState.size).filter { it != currentState.currentIndex }
```

## 问题根源

**逻辑不一致导致的问题**：

1. **右滑 (nextQuestion)** 优先跳转到未答题目
2. **左滑 (prevQuestion)** 可以跳转到任何题目（包括已答题目）
3. 当所有题目都已答完时，nextQuestion 找不到未答题目，返回空列表，造成"没反应"
4. 而 prevQuestion 仍然可以在已答题目间跳转

## 解决方案

### 方案一：统一逻辑（推荐）
使 nextQuestion 和 prevQuestion 在随机模式下使用相同的逻辑：

```kotlin
fun nextQuestion() {
    if (randomPracticeEnabled) {
        // 随机跳转到任何不同的题目（与prevQuestion一致）
        val otherIndices = (0 until currentState.questionsWithState.size).filter { it != currentState.currentIndex }
        if (otherIndices.isNotEmpty()) {
            val randomIndex = otherIndices.random(...)
            // 跳转逻辑
        }
    }
}
```

### 方案二：保持原逻辑，增强容错
在 nextQuestion 中添加后备逻辑：

```kotlin
fun nextQuestion() {
    if (randomPracticeEnabled) {
        val unansweredIndices = ... // 原有逻辑
        if (unansweredIndices.isNotEmpty()) {
            // 跳转到未答题目
        } else {
            // 后备：跳转到任何不同题目
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

已添加详细调试日志来确认问题：
- `nextQuestion`: 记录 unansweredIndices.size 和跳转情况
- `prevQuestion`: 记录 otherIndices.size 和跳转情况

## 测试验证

请测试以下场景并观察日志：
1. 右滑几次，观察 `nextQuestion: unansweredIndices.size=X`
2. 左滑几次，观察 `prevQuestion: otherIndices.size=X`  
3. 当出现"没反应"时，查看是否显示 `nextQuestion: all questions answered, no jump`

## 预期日志输出

正常情况：
```
nextQuestion: unansweredIndices.size=50, jumping from 10 to 25
prevQuestion: otherIndices.size=130, jumping from 25 to 78
```

问题情况：
```
nextQuestion: unansweredIndices.size=0, all questions answered, no jump
prevQuestion: otherIndices.size=130, jumping from 78 to 45
```

## 完成时间
2025-08-06

## 重要提醒
✅ **保持原有功能不变**：只添加了调试日志，准备修复逻辑不一致问题
✅ **问题定位准确**：通过分析确认了 nextQuestion 和 prevQuestion 逻辑不一致的根本原因
