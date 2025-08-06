# 随机练习模式跳转逻辑修复

## 修复时间
2025-08-06 10:45

## 问题描述
在随机练习模式下，左右滑动屏幕有时会跳转到已答的题目，但应该只跳转到未答题目。如果没有未答题目，应该弹出交卷确认对话框。

## 问题分析

### 原始问题
之前的修复中，我们让 `nextQuestion()` 和 `prevQuestion()` 使用 `otherIndices`（所有不同的题目），这样会跳转到已答的题目，不符合随机练习的预期。

#### 修复前的逻辑（有问题）：
```kotlin
// 🔧 修复：与 prevQuestion 使用相同的逻辑 - 随机跳转到任何不同的题目
val otherIndices = (0 until currentState.questionsWithState.size).filter { it != currentState.currentIndex }
```

### 用户期望
- **随机练习模式下**：左右滑动应该只跳转到**未答题目**
- **完成判断**：如果所有题目都已答，显示交卷确认对话框

## 修复方案 ✅

### 1. 修复 ViewModel 跳转逻辑

#### nextQuestion() 修复：
```kotlin
fun nextQuestion() {
    if (randomPracticeEnabled) {
        // 🔧 修复：随机模式下只跳转到未答题目
        val unansweredIndices = currentState.questionsWithState.mapIndexedNotNull { index, questionWithState ->
            if (questionWithState.selectedOptions.isEmpty()) index else null
        }
        
        if (unansweredIndices.isNotEmpty()) {
            val randomIndex = unansweredIndices.random(...)
            _sessionState.value = currentState.copy(currentIndex = randomIndex)
        } else {
            // 没有未答题目，不做跳转，让界面层处理完成逻辑
        }
    }
}
```

#### prevQuestion() 修复：
```kotlin
fun prevQuestion() {
    if (randomPracticeEnabled) {
        // 🔧 修复：随机模式下只跳转到未答题目
        val unansweredIndices = currentState.questionsWithState.mapIndexedNotNull { index, questionWithState ->
            if (questionWithState.selectedOptions.isEmpty()) index else null
        }
        
        if (unansweredIndices.isNotEmpty()) {
            val randomIndex = unansweredIndices.random(...)
            _sessionState.value = currentState.copy(currentIndex = randomIndex)
        } else {
            // 没有未答题目，不做跳转，让界面层处理完成逻辑
        }
    }
}
```

### 2. 新增完成状态检查

在 PracticeViewModel 中添加：
```kotlin
// 🔧 新增：检查是否还有未答题目（用于随机模式的完成判断）
val hasUnansweredQuestions: Boolean
    get() = _sessionState.value.questionsWithState.any { it.selectedOptions.isEmpty() }
```

### 3. 修复 PracticeScreen 滑动逻辑

在滑动手势处理中添加完成检查：
```kotlin
onDragEnd = {
    if (dragAmount > 100f) {
        // 🔧 修复：随机模式下检查是否还有未答题目
        if (settingsViewModel.randomPractice.value && !viewModel.hasUnansweredQuestions) {
            showExitDialog = true  // 显示交卷确认对话框
        } else {
            viewModel.prevQuestion()  // 正常跳转到未答题目
        }
    } else if (dragAmount < -100f) {
        // 🔧 修复：随机模式下检查是否还有未答题目
        if (settingsViewModel.randomPractice.value && !viewModel.hasUnansweredQuestions) {
            showExitDialog = true  // 显示交卷确认对话框
        } else {
            viewModel.nextQuestion()  // 正常跳转到未答题目
        }
    }
}
```

## 修复效果

### ✅ 修复前的问题
- 随机模式下滑动会跳转到已答题目
- 没有完成判断机制

### ✅ 修复后的表现
- **随机模式下滑动只跳转到未答题目**
- **所有题目答完后滑动显示交卷确认对话框**
- **非随机模式的行为完全不变**

## 技术细节

### 跳转逻辑统一
- **随机模式**：使用 `unansweredIndices` 确保只跳转到未答题目
- **非随机模式**：保持原有的顺序跳转逻辑不变

### 随机种子差异化
```kotlin
// nextQuestion 使用的随机种子
kotlin.random.Random(currentState.sessionStartTime + currentState.currentIndex)

// prevQuestion 使用的随机种子（添加1000避免相同）
kotlin.random.Random(currentState.sessionStartTime + currentState.currentIndex + 1000)
```

### 完成状态判断
```kotlin
val hasUnansweredQuestions: Boolean
    get() = _sessionState.value.questionsWithState.any { it.selectedOptions.isEmpty() }
```

## 测试验证

### 测试场景
1. **随机模式 + 有未答题目**：
   - 左右滑动应该只跳转到未答题目
   - 不会跳转到已答题目

2. **随机模式 + 全部题目已答**：
   - 左右滑动应该显示交卷确认对话框
   - 不会再进行任何跳转

3. **非随机模式**：
   - 行为完全不变，按顺序跳转

### 验证步骤
1. 开启随机练习模式
2. 答几道题后，用左右滑动导航
3. 验证只会跳转到未答题目
4. 答完所有题目后再滑动
5. 验证是否显示交卷确认对话框

## 兼容性保证

### ✅ 保持原有功能
- 非随机模式的所有逻辑完全不变
- 答题后的停留时间功能完全不变
- 所有状态管理和进度保存功能完全不变

### ✅ 架构一致性
- ViewModel 负责状态管理和跳转逻辑
- PracticeScreen 负责界面行为和用户交互
- 分离关注点，职责清晰

## 总结

通过修改 `nextQuestion()` 和 `prevQuestion()` 方法的跳转逻辑，从使用 `otherIndices`（任何不同题目）改为使用 `unansweredIndices`（只有未答题目），并在 PracticeScreen 中添加完成状态检查，成功实现了随机练习模式下的正确跳转逻辑。

**问题状态**: 🟢 已完全解决
**修复类型**: 跳转逻辑优化 + 完成状态判断
**影响范围**: 仅限随机练习模式的手势导航
**风险等级**: 🟢 极低（只调整跳转目标，保持所有其他功能不变）
