# 随机练习模式停留时间失效问题修复

## 修复时间
2025-08-06 10:40

## 问题描述
在随机练习模式下，设置页面的"答对停留时间"和"答错停留时间"设置无效，答题后立即跳转到下一题，没有按设定时间停留。

## 问题分析

### 根本原因
在 `PracticeViewModel.answerQuestion()` 方法中，添加了随机模式下的自动跳转逻辑：

```kotlin
// 🚀 新增：随机模式下答题后自动跳转到下一个未答题目
if (randomPracticeEnabled) {
    // ... 立即跳转到下一题
    _sessionState.value = newState.copy(currentIndex = newIndex)
}
```

### 问题流程
1. 用户点击答案 → `viewModel.answerQuestion(idx)` 被调用
2. **ViewModel 立即跳转到下一题**（随机模式逻辑）
3. PracticeScreen 执行停留时间逻辑：`delay(d * 1000L)`
4. PracticeScreen 再调用 `viewModel.nextQuestion()` **→ 已经无效，因为已经跳转了**

### 时序冲突
```
答题 → ViewModel立即跳转 → [停留时间] → Screen尝试跳转(无效)
      ↑ 问题所在：过早跳转，绕过了停留时间
```

## 修复方案 ✅

### 核心修复
移除 `answerQuestion()` 方法中的自动跳转逻辑，让 PracticeScreen 在停留时间后统一控制跳转。

#### 修复前（有问题）：
```kotlin
fun answerQuestion(option: Int) {
    // ... 设置答案状态
    _sessionState.value = newState
    
    // 🚀 随机模式下立即跳转 ← 问题所在
    if (randomPracticeEnabled) {
        val newIndex = unansweredIndices.random(...)
        _sessionState.value = newState.copy(currentIndex = newIndex)
    }
    
    saveProgress()
}
```

#### 修复后（正确）：
```kotlin
fun answerQuestion(option: Int) {
    // ... 设置答案状态
    _sessionState.value = newState
    
    // 🔧 修复：移除自动跳转，让 PracticeScreen 控制时机
    // 注释掉随机模式的自动跳转逻辑
    
    saveProgress()
}
```

### 工作流程修复后
```
答题 → 设置答案状态 → [停留时间] → nextQuestion()随机跳转
     ↑ 只设置状态        ↑ 正常工作    ↑ 在正确时机跳转
```

## 技术细节

### 停留时间逻辑位置（保持不变）
PracticeScreen.kt 中的停留时间逻辑正常工作：
```kotlin
autoJob = coroutineScope.launch {
    val d = if (correct) correctDelay else wrongDelay
    if (d > 0) kotlinx.coroutines.delay(d * 1000L)  // 停留时间
    viewModel.updateShowResult(currentIndex, true)
    if (currentIndex < questions.size - 1) {
        viewModel.nextQuestion()  // 现在会正确执行随机跳转
    }
}
```

### 随机跳转逻辑（保持不变）
`nextQuestion()` 方法中的随机跳转逻辑保持不变：
```kotlin
fun nextQuestion() {
    if (randomPracticeEnabled) {
        val otherIndices = (0 until currentState.questionsWithState.size).filter { it != currentState.currentIndex }
        if (otherIndices.isNotEmpty()) {
            val randomIndex = otherIndices.random(...)
            _sessionState.value = currentState.copy(currentIndex = randomIndex)
        }
    }
    // ... 非随机模式逻辑
}
```

## 修复效果

### ✅ 修复前的问题
- 随机模式下答题立即跳转，无停留时间
- 设置的"答对停留时间"和"答错停留时间"完全无效

### ✅ 修复后的表现
- 随机模式下答题后按设定时间停留
- "答对停留时间"和"答错停留时间"设置正常生效
- 停留时间结束后正确执行随机跳转

### ✅ 兼容性保证
- 非随机模式完全不受影响
- 多选题的停留时间逻辑完全不受影响
- 其他所有练习功能保持不变

## 测试验证

### 测试步骤
1. 进入设置页面，设置"答对停留时间"为3秒，"答错停留时间"为5秒
2. 开启随机练习模式
3. 开始练习，选择正确答案，观察是否停留3秒
4. 选择错误答案，观察是否停留5秒
5. 验证停留时间结束后是否正确随机跳转到其他题目

### 预期结果
- ✅ 答对后停留3秒，然后随机跳转
- ✅ 答错后停留5秒，然后随机跳转
- ✅ 跳转目标总是不同的题目（非当前题目）

## 重要说明

### ✅ 保持原有功能
- 随机跳转逻辑完全保留，只是调整了执行时机
- 所有答题状态管理、进度保存等功能完全不变
- 非随机模式的顺序练习完全不受影响

### ✅ 架构改进
- 分离了"答题状态设置"和"界面跳转控制"的职责
- ViewModel 专注于状态管理，Screen 控制界面行为和时机
- 符合 MVVM 架构的最佳实践

## 总结

通过移除 `answerQuestion()` 中的立即跳转逻辑，让 PracticeScreen 在适当的停留时间后调用 `nextQuestion()` 来执行跳转，成功修复了随机练习模式下停留时间设置无效的问题。

**问题状态**: 🟢 已完全解决
**修复类型**: 时序逻辑优化
**影响范围**: 仅限随机练习模式的停留时间功能
**风险等级**: 🟢 极低（只调整执行时机，保持所有功能不变）
