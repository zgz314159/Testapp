# 随机模式设置不生效问题修复总结

## 问题描述
当设置界面中的随机开关按钮处于关闭位置（即关掉随机模式）时，练习和考试的出题仍然会采用随机模式出题。

## 问题原因
存在时序问题和状态同步问题：

1. **PracticeViewModel 问题**：
   - 在 `PracticeScreen` 中，虽然调用了 `viewModel.setRandomPractice(randomPractice)`，但在 `setProgressId` 方法中没有传递 `random` 参数
   - `setProgressId` 依赖于之前设置的 `randomPracticeEnabled` 变量，可能存在时序问题

2. **ExamViewModel 问题**：
   - 在 `ExamScreen` 中，调用了 `viewModel.setRandomExam(randomExam)`，但在某些导航方法中仍然使用旧的 `randomExamEnabled` 值
   - `loadWrongQuestions` 和 `loadFavoriteQuestions` 方法没有正确设置 `randomExamEnabled` 变量

## 修复方案

### 1. PracticeViewModel 修复
- **修改 `setProgressId` 方法**：添加 `random` 参数并在方法内直接设置 `randomPracticeEnabled`
- **更新 PracticeScreen 调用**：在所有 `setProgressId` 调用中传递 `randomPractice` 参数

### 2. ExamViewModel 修复
- **修改 `loadQuestions` 方法**：在方法开始时直接设置 `randomExamEnabled = random`
- **修改 `loadWrongQuestions` 方法**：在方法开始时直接设置 `randomExamEnabled = random`
- **修改 `loadFavoriteQuestions` 方法**：在方法开始时直接设置 `randomExamEnabled = random`

## 具体修改

### PracticeViewModel.kt
```kotlin
fun setProgressId(
    id: String,
    questionsId: String = id,
    loadQuestions: Boolean = true,
    questionCount: Int = 0,
    random: Boolean = randomPracticeEnabled  // 新增参数
) {
    // ...
    randomPracticeEnabled = random  // 直接设置
    // ...
}
```

### PracticeScreen.kt
```kotlin
// 在所有 setProgressId 调用中添加 random 参数
viewModel.setProgressId(id = quizId, questionsId = quizId, questionCount = practiceCount, random = randomPractice)
```

### ExamViewModel.kt
```kotlin
fun loadQuestions(quizId: String, count: Int, random: Boolean) {
    // ...
    randomExamEnabled = random  // 直接设置
    // ...
}

fun loadWrongQuestions(fileName: String, count: Int, random: Boolean) {
    // ...
    randomExamEnabled = random  // 直接设置
    // ...
}

fun loadFavoriteQuestions(fileName: String, count: Int, random: Boolean) {
    // ...
    randomExamEnabled = random  // 直接设置
    // ...
}
```

## 技术要点

1. **消除时序依赖**：不再依赖于先调用 `setRandomPractice/setRandomExam` 再调用加载方法的顺序
2. **直接参数传递**：确保随机设置直接通过参数传递，避免状态同步问题
3. **统一设置时机**：在每个加载方法的开始就设置正确的随机模式状态

## 影响范围
- 练习模式的随机出题功能
- 考试模式的随机出题功能
- 错题练习/考试的随机功能
- 收藏题练习/考试的随机功能

## 测试建议
1. 在设置界面关闭随机模式，然后进入练习，验证题目顺序是固定的
2. 在设置界面开启随机模式，然后进入练习，验证题目顺序是随机的
3. 在考试模式下测试相同的场景
4. 测试错题练习和收藏题练习的随机设置是否生效

## 修复日期
2025-07-31
