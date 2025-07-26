# 考试次数重置问题修复总结

## 问题描述
用户反映考试累计次数始终显示为"1"，无法正确累计。通过用户提供的日志发现：
- `incrementExamCount()` 方法正确地将考试次数从 0→1→2 递增
- 但在考试重新加载时，考试次数会重置回较小的值

## 根本原因
在 `ExamViewModel.kt` 的 `loadQuestions()` 方法中，每次加载考试时都会无条件地重新初始化考试次数：

```kotlin
// 问题代码：每次都重置考试次数
_cumulativeExamCount.value = historyExamCount
```

这导致：
1. 用户提交考试 → `incrementExamCount()` 正确递增考试次数
2. 页面重新加载 → `loadQuestions()` 被调用 → 考试次数被重置为历史记录数量
3. 累计次数丢失，始终显示为历史记录数量

## 修复方案
修改 `loadQuestions()` 方法中的初始化逻辑，只在考试次数为0（未初始化）时才设置为历史次数：

```kotlin
// 修复后的代码：只在未初始化时才设置
if (_cumulativeExamCount.value == 0) {
    val historyExamCount = getExamHistoryListByFileUseCase(quizId).firstOrNull()?.size ?: 0
    _cumulativeExamCount.value = historyExamCount
    android.util.Log.d("ExamViewModel", "初始化考试次数: 历史=$historyExamCount (本次考试完成时才会增加)")
} else {
    android.util.Log.d("ExamViewModel", "保持现有考试次数: ${_cumulativeExamCount.value} (避免重置)")
}
```

## 修复效果
- ✅ 保持 `incrementExamCount()` 方法的正确递增逻辑
- ✅ 防止 `loadQuestions()` 重置已递增的考试次数  
- ✅ 确保考试次数只通过提交交卷确认按钮来计算（符合用户需求）
- ✅ 在真正的新考试开始时仍能正确初始化为历史次数

## 测试建议
1. 启动考试，确认初始次数正确
2. 提交考试，确认次数递增
3. 重新加载页面，确认次数不会重置
4. 继续提交多次考试，确认累计次数正确递增

## 文件修改
- `app/src/main/java/com/example/testapp/presentation/screen/ExamViewModel.kt` (第139-154行)
