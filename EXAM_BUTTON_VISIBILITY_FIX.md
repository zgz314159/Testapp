# 考试界面"上一题"、"下一题"按钮显示逻辑修复

## 修复要求
只在多选题的时候才显示"上一题"、"下一题"按钮，其他题型都隐藏这些按钮及区域。

## 需要修改的文件
**文件路径**：`app/src/main/java/com/example/testapp/presentation/screen/ExamScreen.kt`

## 具体修改位置
在文件的第855-857行左右，找到以下代码：

```kotlin
        // "提交答案"按钮
        // 底部导航按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 上一题按钮
            Button(
                onClick = {
                    if (selectedOption.isNotEmpty()) {
                        answeredThisSession = true
                    }
                    viewModel.prevQuestion()
                },
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "上一题",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 下一题按钮
            Button(
                onClick = {
                    if (selectedOption.isNotEmpty()) {
                        answeredThisSession = true
                    }
                    viewModel.nextQuestion()
                },
                enabled = currentIndex < questions.size - 1,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "下一题",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
```

## 修改后的代码
将上述代码替换为：

```kotlin
        // 底部导航按钮 - 只在多选题时显示
        if (question.type == "多选题") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 上一题按钮
                Button(
                    onClick = {
                        if (selectedOption.isNotEmpty()) {
                            answeredThisSession = true
                        }
                        viewModel.prevQuestion()
                    },
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "上一题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 下一题按钮
                Button(
                    onClick = {
                        if (selectedOption.isNotEmpty()) {
                            answeredThisSession = true
                        }
                        viewModel.nextQuestion()
                    },
                    enabled = currentIndex < questions.size - 1,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "下一题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }
```

## 修改要点
1. **添加条件判断**：使用 `if (question.type == "多选题")` 包围整个按钮Row布局
2. **更新注释**：将注释从"提交答案"改为"底部导航按钮 - 只在多选题时显示"
3. **保持功能不变**：按钮的点击逻辑、启用/禁用逻辑、样式等保持完全一致

## 预期效果
- **多选题**：显示"上一题"、"下一题"按钮，用户可以导航
- **单选题/判断题等其他题型**：完全隐藏按钮区域，界面更加清洁
- **功能保持**：多选题中的导航功能完全保持，包括答案保存、session状态更新等

## 测试建议
1. 在多选题中验证按钮显示和功能正常
2. 在单选题中验证按钮完全隐藏
3. 确认题目类型切换时按钮显示状态正确更新

## 技术说明
这个修改通过添加条件判断 `if (question.type == "多选题")` 来控制按钮的显示，当题目类型不是多选题时，整个Row布局都不会被渲染，从而完全隐藏按钮区域。这种方法比使用visibility modifier更高效，因为不会创建不必要的UI组件。
