# 随机模式左滑调试增强

## 问题描述
用户反馈随机模式下左滑功能没有实现："随机模式下：左滑时随机跳转到当前题目以外的任何一题"

## 代码分析
当前的 `prevQuestion()` 方法在随机模式下的逻辑是正确的：

```kotlin
if (randomPracticeEnabled) {
    // 随机模式：随机跳转到一个不同的题目
    val otherIndices = (0 until currentState.questionsWithState.size).filter { it != currentState.currentIndex }
    if (otherIndices.isNotEmpty()) {
        val randomIndex = otherIndices.random(kotlin.random.Random(currentState.sessionStartTime + currentState.currentIndex))
        _sessionState.value = currentState.copy(currentIndex = randomIndex)
        saveProgress()
    }
}
```

## 修复措施
添加了详细的调试日志来帮助诊断问题：

1. **randomPracticeEnabled 状态检查**: 记录随机模式是否已启用
2. **当前索引和总题目数**: 记录当前状态信息
3. **可选择的其他题目索引**: 记录排除当前题目后的可选索引列表
4. **跳转动作**: 记录从哪个题目跳转到哪个题目
5. **边界情况**: 记录没有其他题目可选择的情况

## 调试日志输出示例
```
PracticeViewModel: prevQuestion: randomPracticeEnabled=true, currentIndex=5, totalQuestions=20
PracticeViewModel: prevQuestion: otherIndices=[0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]
PracticeViewModel: prevQuestion: jumping from 5 to 12
```

## 可能的问题原因
1. **randomPracticeEnabled 未正确设置**: 可能在设置页面切换后，值没有正确传递到 ViewModel
2. **只有一道题目**: 如果总题目数为1，则没有其他题目可以跳转
3. **UI层面问题**: 可能左滑手势没有正确调用 `prevQuestion()` 方法

## 验证步骤
1. 在练习界面开启随机模式
2. 左滑屏幕
3. 查看 Logcat 输出，确认：
   - randomPracticeEnabled = true
   - otherIndices 包含除当前索引外的所有索引
   - 实际执行了跳转操作

## 下一步
如果调试日志显示逻辑正确但功能仍不工作，需要检查：
1. UI层的手势识别是否正确调用了 `prevQuestion()`
2. 随机模式设置是否正确传递到 ViewModel
3. 是否存在其他代码覆盖了状态更新

## 完成时间
2025-08-06

## 重要提醒
✅ **保持原有功能不变**: 此次修复只添加了调试日志，完全保留了原有的功能逻辑
✅ **最小化修改**: 仅增强调试能力，不修改核心业务逻辑
