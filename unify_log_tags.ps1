# 统一日志标签脚本
# 将所有不规范的日志标签统一为 DataCleanup, ProgressTracking, Navigation

Write-Host "开始统一日志标签..."

# 定义要替换的标签映射
$tagMappings = @{
    # ViewModel相关 -> ProgressTracking
    '"ExamDebug"' = '"ProgressTracking"'
    '"PracticeDebug"' = '"ProgressTracking"'
    '"ExamViewModel"' = '"ProgressTracking"'
    '"PracticeViewModel"' = '"ProgressTracking"'
    
    # 数据清理相关 -> DataCleanup
    '"ClearPracticeProgressByFileNameUseCase"' = '"DataCleanup"'
    '"ClearExamProgressByFileNameUseCase"' = '"DataCleanup"'
    '"HomeViewModel"' = '"DataCleanup"'
    
    # 导航相关保持Navigation
    '"AppNavHost"' = '"Navigation"'
    
    # Screen相关
    '"ExamScreen"' = '"ProgressTracking"'
    '"NoteScreen"' = '"ProgressTracking"'
    '"DeepSeekAskScreen"' = '"ProgressTracking"'
    '"SparkAskScreen"' = '"ProgressTracking"'
    '"BaiduAskScreen"' = '"ProgressTracking"'
    
    # 其他
    '"QuestionAnalysisRepo"' = '"DataCleanup"'
    '"BaiduQianfanViewModel"' = '"ProgressTracking"'
}

# 获取所有.kt文件
$kotlinFiles = Get-ChildItem -Path "app\src\main\java" -Recurse -Filter "*.kt"

foreach ($file in $kotlinFiles) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # 应用所有标签替换
    foreach ($oldTag in $tagMappings.Keys) {
        $newTag = $tagMappings[$oldTag]
        $content = $content -replace [regex]::Escape($oldTag), $newTag
    }
    
    # 如果内容有变化，写回文件
    if ($content -ne $originalContent) {
        Set-Content $file.FullName $content -NoNewline
        Write-Host "已更新: $($file.FullName)"
    }
}

Write-Host "日志标签统一完成！"
Write-Host ""
Write-Host "现在您可以使用以下标签过滤日志："
Write-Host "  DataCleanup     - 所有数据删除清理相关"
Write-Host "  ProgressTracking - 所有进度保存加载相关"
Write-Host "  Navigation      - 所有导航相关"
Write-Host ""
Write-Host "在Android Studio的Logcat中，使用过滤器："
Write-Host "  tag:DataCleanup|ProgressTracking|Navigation"
