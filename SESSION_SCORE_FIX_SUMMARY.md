# 练习得分显示问题修复总结

## 问题描述
用户反馈"本次练习答对数显示不对"，显示14（历史累计得分）而不是0（新练习的得分）。

## 问题根因分析
1. **问题表现**：练习页面显示"本次练习"得分为14，但用户期望新练习开始时应该显示0
2. **技术原因**：
   - `sessionCorrectCount`计算所有`showResult = true && isCorrect == true`的题目
   - 系统从本地存储恢复进度时，包含了之前练习会话的答题状态
   - 没有区分不同练习会话的时间边界
   - 新的练习会话继承了历史答题状态

## 修复方案

### 1. 数据模型改进
**文件**: `QuestionWithState.kt`
- 添加`sessionAnswerTime: Long = 0L`字段记录答题时间戳
- 修改`showResult()`方法，在显示结果时记录答题时间

### 2. 会话状态计算优化
**文件**: `PracticeSessionState.kt`  
- 修改`sessionCorrectCount`：只计算本次会话开始后答对的题目
- 修改`sessionAnsweredCount`：只计算本次会话开始后答题的数量  
- 修改`sessionWrongCount`：只计算本次会话开始后答错的题目
- 计算逻辑：`it.sessionAnswerTime >= sessionStartTime`

### 3. ViewModel业务逻辑更新
**文件**: `PracticeViewModel.kt`
- `setProgressId()`：设置新的`sessionStartTime`
- `loadWrongQuestions()`：每次加载错题时重置会话时间
- `loadFavoriteQuestions()`：每次加载收藏题时重置会话时间

## 修复后效果
1. **准确的会话统计**：`sessionCorrectCount`只显示本次练习会话的答对数
2. **时间边界清晰**：通过`sessionStartTime`和`sessionAnswerTime`区分不同练习会话
3. **向前兼容**：现有的历史数据依然有效，只是在新会话计算中被过滤
4. **数据一致性**：全局统计（`correctCount`）和会话统计（`sessionCorrectCount`）职责分离

## 技术实现细节

### 关键计算逻辑
```kotlin
// 本次会话答对数
val sessionCorrectCount: Int
    get() = questionsWithState.count { 
        it.showResult && it.isCorrect == true && it.sessionAnswerTime >= sessionStartTime 
    }

// 答题时记录时间戳
fun showResult(): QuestionWithState {
    return copy(
        showResult = true,
        sessionAnswerTime = if (sessionAnswerTime == 0L) System.currentTimeMillis() else sessionAnswerTime
    )
}
```

### 会话重置时机
- 调用`setProgressId()`开始新练习
- 加载错题练习`loadWrongQuestions()`
- 加载收藏题练习`loadFavoriteQuestions()`

## 测试验证
1. **新练习会话**：`sessionCorrectCount`应显示0
2. **继续答题**：每答对一题，`sessionCorrectCount`增加1
3. **重新进入**：新的练习会话，`sessionCorrectCount`重置为0
4. **历史数据**：`correctCount`依然显示历史累计答对数

## 兼容性保证
- 现有数据结构保持兼容
- 新增字段有默认值(`sessionAnswerTime = 0L`)
- 老版本数据通过时间戳判断自动过滤
- 不影响其他功能模块

## 解决的核心问题
✅ "本次练习"显示历史累计得分 → 只显示当前会话得分  
✅ 无法区分不同练习会话 → 通过时间戳明确区分  
✅ 状态持久化混淆新旧数据 → 时间边界清晰分离  
✅ 用户体验困惑 → 准确反映当前练习进度  

修复完成，现在"本次练习答对数"将正确显示为当前练习会话的实际答对数量。
