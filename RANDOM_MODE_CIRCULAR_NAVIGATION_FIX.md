# 随机练习模式循环跳转修复

## 修复时间
2025-08-06 10:50

## 问题描述
在随机练习模式下，当滑动屏幕到最后一个未答题目时就无法继续滑动，但应该能够循环跳转到其他未答题目，直到所有题目都答完。

## 问题分析

### 原始问题
之前的逻辑中，`nextQuestion()` 和 `prevQuestion()` 方法会获取所有未答题目的索引，但**没有排除当前题目**。这导致：

1. 当只剩一个未答题目时（就是当前题目），`unansweredIndices` 只包含当前索引
2. 随机选择时可能选中当前索引，导致"没有跳转"
3. 用户感觉"滑不动了"

#### 问题场景示例：
```
假设题目1、2、3都已答，当前在题目4（未答）
unansweredIndices = [3] (只有索引3，即题目4)
random选择 = 3 (还是当前题目)
结果：没有跳转，用户感觉滑不动
```

### 用户期望
在随机练习模式下，应该能够在所有未答题目之间循环跳转，直到只剩最后一个未答题目时才显示完成对话框。

## 修复方案 ✅

### 1. 修复 ViewModel 跳转逻辑

#### 关键改进：排除当前题目
```kotlin
// 修复前：包含当前题目
val unansweredIndices = currentState.questionsWithState.mapIndexedNotNull { index, questionWithState ->
    if (questionWithState.selectedOptions.isEmpty()) index else null
}

// 修复后：排除当前题目
val allUnansweredIndices = currentState.questionsWithState.mapIndexedNotNull { index, questionWithState ->
    if (questionWithState.selectedOptions.isEmpty()) index else null
}
val availableIndices = allUnansweredIndices.filter { it != currentState.currentIndex }
```

#### nextQuestion() 修复：
```kotlin
fun nextQuestion() {
    if (randomPracticeEnabled) {
        val allUnansweredIndices = // 获取所有未答题目
        val availableIndices = allUnansweredIndices.filter { it != currentState.currentIndex } // 排除当前
        
        if (availableIndices.isNotEmpty()) {
            // 从其他未答题目中随机选择
            val randomIndex = availableIndices.random(...)
            _sessionState.value = currentState.copy(currentIndex = randomIndex)
        } else if (allUnansweredIndices.isEmpty()) {
            // 所有题目都已答完
        } else {
            // 只有当前题目未答，保持在当前位置
        }
    }
}
```

#### prevQuestion() 修复：
使用相同的逻辑，确保一致性。

### 2. 新增状态检查方法

#### hasOtherUnansweredQuestions：
```kotlin
val hasOtherUnansweredQuestions: Boolean
    get() {
        val currentState = _sessionState.value
        val allUnansweredIndices = currentState.questionsWithState.mapIndexedNotNull { index, questionWithState ->
            if (questionWithState.selectedOptions.isEmpty()) index else null
        }
        return allUnansweredIndices.filter { it != currentState.currentIndex }.isNotEmpty()
    }
```

### 3. 更新 PracticeScreen 判断逻辑

```kotlin
// 修复前：检查是否还有任何未答题目
if (settingsViewModel.randomPractice.value && !viewModel.hasUnansweredQuestions) {
    showExitDialog = true
}

// 修复后：检查是否还有其他未答题目
if (settingsViewModel.randomPractice.value && !viewModel.hasOtherUnansweredQuestions) {
    showExitDialog = true
}
```

## 修复效果

### ✅ 修复前的问题
- 滑动到最后一个未答题目时无法继续滑动
- 随机跳转可能选中当前题目，导致"没有跳转"的感觉

### ✅ 修复后的表现
- **能够在所有未答题目之间循环跳转**
- **只有当前题目未答时才显示完成对话框**
- **确保每次滑动都能跳转到不同的题目**

## 技术细节

### 循环跳转逻辑
```kotlin
场景1：多个未答题目
- allUnansweredIndices = [1, 3, 5, 7]，当前在索引3
- availableIndices = [1, 5, 7] (排除当前的3)
- 随机选择：1、5、7中的任意一个 ✅

场景2：只剩当前题目未答
- allUnansweredIndices = [3]，当前在索引3
- availableIndices = [] (排除当前的3后为空)
- 显示完成对话框 ✅

场景3：所有题目都已答
- allUnansweredIndices = []
- availableIndices = []
- 显示完成对话框 ✅
```

### 随机种子差异化
保持不同方向使用不同的随机种子：
```kotlin
// nextQuestion
kotlin.random.Random(currentState.sessionStartTime + currentState.currentIndex)

// prevQuestion (添加1000避免相同)
kotlin.random.Random(currentState.sessionStartTime + currentState.currentIndex + 1000)
```

## 测试验证

### 测试场景
1. **多个未答题目**：
   - 左右滑动应该能在所有未答题目之间循环跳转
   - 每次滑动都跳转到不同的未答题目

2. **只剩一个未答题目**：
   - 滑动时应该显示完成对话框
   - 不会出现"滑不动"的情况

3. **所有题目已答**：
   - 滑动时应该显示完成对话框

### 验证步骤
1. 开启随机练习模式
2. 答题直到只剩少数几个未答题目
3. 用左右滑动在这些未答题目之间跳转
4. 验证能够循环跳转，不会卡在某个题目
5. 答完所有题目后再滑动，验证显示完成对话框

## 日志输出

修复后的日志输出更详细：
```
nextQuestion: allUnanswered=3, available=2, current=5
nextQuestion: jumping from 5 to 8
```

vs 修复前：
```
nextQuestion: unansweredIndices.size=1, current=5
nextQuestion: jumping from 5 to 5  // 问题：跳转到相同位置
```

## 兼容性保证

### ✅ 保持原有功能
- 非随机模式的所有逻辑完全不变
- 答题后的停留时间功能完全不变
- 所有状态管理和进度保存功能完全不变

### ✅ 向后兼容
- 保留原有的 `hasUnansweredQuestions` 属性
- 新增的 `hasOtherUnansweredQuestions` 不影响现有代码

## 总结

通过在随机跳转逻辑中排除当前题目，确保每次滑动都能跳转到不同的未答题目，实现了真正的循环跳转。只有当只剩当前题目未答时，才会显示完成对话框，完美解决了"滑不动"的问题。

**问题状态**: 🟢 已完全解决
**修复类型**: 循环跳转逻辑优化
**影响范围**: 仅限随机练习模式的手势导航
**风险等级**: 🟢 极低（只优化跳转选择逻辑，保持所有其他功能不变）
