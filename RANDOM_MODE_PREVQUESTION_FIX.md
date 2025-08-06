# 随机模式左滑切题问题修复

## 问题描述
设置页面的练习随机开关开启状态下，练习答题界面向左滑动屏幕不能转跳到下一题的问题。

## 问题原因
`prevQuestion()` 方法没有适配随机模式，仍然按照顺序模式（返回上一题）的逻辑执行，与随机模式的设计不符。

## 修复方案
**只修复 `prevQuestion()` 方法**，添加随机模式的支持，保持其他所有功能不变。

## 修复内容

### 修复前：
```kotlin
fun prevQuestion() {
    val currentState = _sessionState.value
    if (currentState.currentIndex > 0) {
        _sessionState.value = currentState.copy(currentIndex = currentState.currentIndex - 1)
        saveProgress()
    }
}
```

### 修复后：
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

## 修复逻辑
1. **随机模式**：左滑时随机跳转到当前题目以外的任何一题
2. **非随机模式**：保持原有逻辑，按顺序返回上一题

## 保留的原有功能
✅ 历史题目显示答案功能：完全保留
✅ 答题后自动跳转功能：完全保留  
✅ 所有其他业务逻辑：完全保留
✅ 非随机模式的所有功能：完全保留

## 测试建议
1. **开启随机模式**：
   - 进入练习答题界面
   - 测试向左滑动，确认能随机跳转到不同题目
   
2. **关闭随机模式**：
   - 测试向左滑动，确认按顺序返回上一题（原有功能）

## 影响范围
- **修改文件**：仅 `PracticeViewModel.kt` 的 `prevQuestion()` 方法
- **影响功能**：仅影响随机模式下的左滑切题功能
- **兼容性**：完全兼容，不影响任何现有功能

## 修复完成时间
2025年8月6日

## 状态
✅ **已完成** - 随机模式下的左滑切题功能已修复，所有原有功能完整保留
