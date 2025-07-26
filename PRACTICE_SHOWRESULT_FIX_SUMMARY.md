# PracticeViewModel showResult状态持久化修复

## 问题描述
用户反馈在练习模式中，已答题目的结果状态显示持久保存和再次历史进度恢复存在问题。当重新进入练习模式时，之前已答的题目不会自动显示结果状态。

## 根本原因
PracticeViewModel的`loadProgress()`方法在恢复进度时，只是简单地恢复了`showResult`字段的原始值，没有像ExamViewModel那样实现智能的showResult状态恢复逻辑。对于历史已答但未显示结果的题目，应该智能地显示结果。

## 解决方案

### 1. 新格式状态映射的智能恢复
在`loadProgress()`方法中为新格式的题目状态映射添加了智能showResult恢复逻辑：

```kotlin
// 🚀 核心修复：智能showResult状态恢复
val shouldShowResult = if (savedState.selectedOptions.isNotEmpty()) {
    // 如果题目已答且之前显示了结果，恢复显示状态
    if (savedState.showResult) {
        android.util.Log.d("FixedOrderDebug", "恢复已答题目显示结果状态: questionId=$questionId")
        true
    } else {
        // 历史进度中已答但没有显示结果的题目，智能判断是否显示
        val wasAnsweredInPreviousSession = savedState.sessionAnswerTime > 0L && 
            savedState.sessionAnswerTime < currentState.sessionStartTime
        if (wasAnsweredInPreviousSession) {
            android.util.Log.d("FixedOrderDebug", "历史已答题目智能显示结果: questionId=$questionId")
            true
        } else {
            savedState.showResult
        }
    }
} else {
    savedState.showResult
}
```

### 2. 兼容旧格式的智能恢复
为了确保向后兼容，也为旧格式的基于位置的状态恢复添加了类似的智能逻辑：

```kotlin
// 🚀 核心修复：智能showResult状态恢复（兼容旧格式）
val shouldShowResult = if (selectedOptions.isNotEmpty()) {
    // 如果题目已答且之前显示了结果，恢复显示状态
    if (originalShowResult) {
        android.util.Log.d("FixedOrderDebug", "恢复已答题目显示结果状态: index=$index")
        true
    } else {
        // 历史进度中已答但没有显示结果的题目，智能判断是否显示
        android.util.Log.d("FixedOrderDebug", "历史已答题目智能显示结果: index=$index")
        true
    }
} else {
    originalShowResult
}
```

### 3. 增强的调试日志
添加了详细的调试日志来追踪showResult状态的恢复和保存过程：

```kotlin
// 恢复时的统计日志
val answeredCount = updatedQuestionsWithState.count { it.selectedOptions.isNotEmpty() }
val showResultCount = updatedQuestionsWithState.count { it.showResult }
android.util.Log.d("FixedOrderDebug", "✅ 练习模式进度加载完成 - 当前题目: $newCurrentIndex, 已答题目: $answeredCount, 显示结果: $showResultCount")

// 保存时的统计日志
android.util.Log.d("FixedOrderDebug", "💾 练习模式保存进度统计 - 已答题目: $answeredCount, 显示结果: $showResultCount")
```

## 修复后的行为

### 进度恢复时
1. **已显示结果的题目**：直接恢复showResult=true状态
2. **历史已答但未显示的题目**：智能设置showResult=true，让用户重新进入时能看到之前的答题结果
3. **未答题目**：保持showResult=false状态

### 状态保存时
- 完整保存所有题目的showResult状态到数据库
- 同时支持新格式的ID映射和旧格式的位置映射，确保向后兼容

## 验证方法
用户可以通过以下步骤验证修复效果：
1. 在练习模式中答几道题并显示结果
2. 退出练习模式
3. 重新进入同一练习
4. 检查之前已答的题目是否自动显示结果状态

## 技术特点
- **智能恢复**：自动判断历史已答题目应该显示结果
- **向后兼容**：同时支持新旧两种数据格式
- **详细日志**：便于调试和验证修复效果
- **一致性**：与ExamViewModel的修复保持逻辑一致

修复完成后，PracticeViewModel现在具备了与ExamViewModel相同的智能showResult状态持久化能力。
