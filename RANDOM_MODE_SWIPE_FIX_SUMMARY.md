# 随机模式滑动切题功能修复总结

## 问题描述
在设置页面开启练习随机开关的状态下，练习答题界面向左滑动屏幕不能正确跳转到下一题的问题。

## 问题分析
1. **根本原因**：在 `PracticeViewModel.kt` 中，`prevQuestion()` 方法没有适配随机模式
2. **具体表现**：
   - 右滑调用 `nextQuestion()` 能正确在随机模式下跳转到随机题目
   - 左滑调用 `prevQuestion()` 仍然按顺序往前跳转，与随机模式逻辑不符
   - 导致用户在随机模式下左滑无法获得预期的随机跳转效果

## 修复方案
### 1. 修复 `prevQuestion()` 方法
**文件位置**：`app/src/main/java/com/example/testapp/presentation/screen/PracticeViewModel.kt`

**修复前**：
```kotlin
fun prevQuestion() {
    val currentState = _sessionState.value
    if (currentState.currentIndex > 0) {
        _sessionState.value = currentState.copy(currentIndex = currentState.currentIndex - 1)
        saveProgress()
    }
}
```

**修复后**：
```kotlin
fun prevQuestion() {
    val currentState = _sessionState.value
    
    if (randomPracticeEnabled) {
        // 随机模式：随机跳转到一个不同的题目
        val otherIndices = (0 until currentState.questionsWithState.size).filter { it != currentState.currentIndex }
        if (otherIndices.isNotEmpty()) {
            val randomIndex = otherIndices.random(kotlin.random.Random(currentState.sessionStartTime + currentState.currentIndex))
            _sessionState.value = currentState.copy(currentIndex = randomIndex)
            saveProgress()
        }
    } else {
        // 非随机模式：按顺序返回上一题
        if (currentState.currentIndex > 0) {
            _sessionState.value = currentState.copy(currentIndex = currentState.currentIndex - 1)
            saveProgress()
        }
    }
}
```

### 2. 优化 `nextQuestion()` 方法
同时优化了 `nextQuestion()` 方法，确保在所有题目都已回答完毕时，用户仍能在题目间随机跳转。

**修复前**：当所有题目答完后，用户无法继续跳转题目
**修复后**：当所有题目答完后，用户仍可以随机跳转到其他题目进行复习

## 修复逻辑详细说明
### 随机模式下的跳转逻辑
1. **prevQuestion()（左滑）**：
   - 从当前题目以外的所有题目中随机选择一个
   - 使用 `sessionStartTime + currentIndex` 作为随机种子，确保相同位置的随机结果一致

2. **nextQuestion()（右滑）**：
   - 优先跳转到未答题目中的随机一个
   - 如果所有题目都已回答，则跳转到其他题目中的随机一个

### 非随机模式保持原有逻辑
- prevQuestion()：按顺序返回上一题
- nextQuestion()：按顺序进入下一题

## 测试建议
1. **开启随机模式测试**：
   - 进入设置页面，开启练习随机开关
   - 进入练习答题界面
   - 测试左滑动作，确认能随机跳转到不同题目
   - 测试右滑动作，确认能随机跳转到未答题目

2. **关闭随机模式测试**：
   - 关闭练习随机开关
   - 测试左滑动作，确认按顺序返回上一题
   - 测试右滑动作，确认按顺序进入下一题

## 影响范围
- **修改文件**：`PracticeViewModel.kt`
- **影响功能**：练习答题界面的滑动切题功能
- **兼容性**：不影响现有的非随机模式功能

## 修复完成时间
2025年8月6日

## 状态
✅ **已完成** - 随机模式下的左滑切题功能已修复
