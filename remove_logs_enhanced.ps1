# 增强版日志删除脚本 - 处理复杂的多行和带参数的日志语句
# 使用方法: .\remove_logs_enhanced.ps1

Write-Host "开始删除项目中的所有日志语句（包括多行和带参数的）..." -ForegroundColor Green

$sourceDir = "app\src\main\java"

if (-not (Test-Path $sourceDir)) {
    Write-Host "错误: 找不到源码目录 $sourceDir" -ForegroundColor Red
    exit 1
}

$totalFiles = 0
$modifiedFiles = 0
$totalLogsRemoved = 0

# 获取所有Kotlin和Java文件
$files = Get-ChildItem -Path $sourceDir -Include "*.kt", "*.java" -Recurse

Write-Host "找到 $($files.Count) 个源码文件需要处理..." -ForegroundColor Yellow

foreach ($file in $files) {
    $totalFiles++
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $originalContent = $content
    $fileLogsRemoved = 0
    
    Write-Host "处理文件: $($file.Name)" -ForegroundColor Cyan
    
    # 使用更强大的正则表达式来匹配所有类型的日志语句
    # 支持嵌套括号、字符串插值、多行日志等复杂情况
    
    # 1. 匹配 android.util.Log.* 语句（包括复杂参数）
    $pattern1 = 'android\.util\.Log\.[deivwDEIVW]\s*\(\s*"[^"]*"\s*,\s*"[^"]*(?:\$[^"]*)*"[^)]*\)\s*'
    $matches1 = [regex]::Matches($content, $pattern1, [System.Text.RegularExpressions.RegexOptions]::Singleline)
    if ($matches1.Count -gt 0) {
        $fileLogsRemoved += $matches1.Count
        $content = [regex]::Replace($content, $pattern1, '', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        Write-Host "  删除了 $($matches1.Count) 个 android.util.Log 语句（带参数）" -ForegroundColor Gray
    }
    
    # 2. 更通用的 android.util.Log 匹配（处理各种复杂情况）
    $pattern2 = 'android\.util\.Log\.[deivwDEIVW]\s*\([^;]*?\)\s*(?=\s*$|\s*\}|\s*[a-zA-Z])'
    $matches2 = [regex]::Matches($content, $pattern2, [System.Text.RegularExpressions.RegexOptions]::Multiline)
    if ($matches2.Count -gt 0) {
        $fileLogsRemoved += $matches2.Count
        $content = [regex]::Replace($content, $pattern2, '', [System.Text.RegularExpressions.RegexOptions]::Multiline)
        Write-Host "  删除了 $($matches2.Count) 个复杂 android.util.Log 语句" -ForegroundColor Gray
    }
    
    # 3. 删除简化的 Log.* 语句
    $pattern3 = '(?<!android\.util\.)Log\.[deivwDEIVW]\s*\([^;]*?\)\s*(?=\s*$|\s*\}|\s*[a-zA-Z])'
    $matches3 = [regex]::Matches($content, $pattern3, [System.Text.RegularExpressions.RegexOptions]::Multiline)
    if ($matches3.Count -gt 0) {
        $fileLogsRemoved += $matches3.Count
        $content = [regex]::Replace($content, $pattern3, '', [System.Text.RegularExpressions.RegexOptions]::Multiline)
        Write-Host "  删除了 $($matches3.Count) 个 Log 语句" -ForegroundColor Gray
    }
    
    # 4. 删除 println 语句
    $pattern4 = 'println\s*\([^;]*?\)\s*(?=\s*$|\s*\}|\s*[a-zA-Z])'
    $matches4 = [regex]::Matches($content, $pattern4, [System.Text.RegularExpressions.RegexOptions]::Multiline)
    if ($matches4.Count -gt 0) {
        $fileLogsRemoved += $matches4.Count
        $content = [regex]::Replace($content, $pattern4, '', [System.Text.RegularExpressions.RegexOptions]::Multiline)
        Write-Host "  删除了 $($matches4.Count) 个 println 语句" -ForegroundColor Gray
    }
    
    # 5. 删除 System.out.println 语句
    $pattern5 = 'System\.out\.println\s*\([^;]*?\)\s*(?=\s*$|\s*\}|\s*[a-zA-Z])'
    $matches5 = [regex]::Matches($content, $pattern5, [System.Text.RegularExpressions.RegexOptions]::Multiline)
    if ($matches5.Count -gt 0) {
        $fileLogsRemoved += $matches5.Count
        $content = [regex]::Replace($content, $pattern5, '', [System.Text.RegularExpressions.RegexOptions]::Multiline)
        Write-Host "  删除了 $($matches5.Count) 个 System.out.println 语句" -ForegroundColor Gray
    }
    
    # 清理连续的空行
    $content = [regex]::Replace($content, '\n\s*\n\s*\n', "`n`n", [System.Text.RegularExpressions.RegexOptions]::Multiline)
    
    # 如果内容有变化，则写入文件
    if ($originalContent -ne $content) {
        $modifiedFiles++
        $totalLogsRemoved += $fileLogsRemoved
        
        # 备份原文件
        $backupPath = $file.FullName + ".backup"
        Copy-Item $file.FullName $backupPath
        
        # 写入修改后的内容
        $content | Set-Content $file.FullName -Encoding UTF8 -NoNewline
        
        Write-Host "  ✅ 修改完成: 删除了 $fileLogsRemoved 个日志语句" -ForegroundColor Green
    } else {
        Write-Host "  ⭕ 无需修改: 未发现日志语句" -ForegroundColor Gray
    }
}

Write-Host "`n🎉 删除完成!" -ForegroundColor Green
Write-Host "📊 统计信息:" -ForegroundColor Yellow
Write-Host "  - 总文件数: $totalFiles" -ForegroundColor White
Write-Host "  - 修改文件数: $modifiedFiles" -ForegroundColor White  
Write-Host "  - 删除日志总数: $totalLogsRemoved" -ForegroundColor White

if ($modifiedFiles -gt 0) {
    Write-Host "`n⚠️  注意事项:" -ForegroundColor Yellow
    Write-Host "  - 原文件已备份为 .backup 文件" -ForegroundColor White
    Write-Host "  - 请测试修改后的代码是否正常运行" -ForegroundColor White
    Write-Host "  - 如需恢复原文件，可使用备份文件" -ForegroundColor White
    Write-Host "`n🧹 删除所有备份文件的命令:" -ForegroundColor Yellow
    Write-Host "  Get-ChildItem -Path '$sourceDir' -Filter '*.backup' -Recurse | Remove-Item -Force" -ForegroundColor Gray
    Write-Host "`n🔄 恢复单个文件的示例:" -ForegroundColor Yellow
    Write-Host "  Copy-Item 'ExamScreen.kt.backup' 'ExamScreen.kt' -Force" -ForegroundColor Gray
}

Write-Host "`n💡 如果还有遗漏的日志，请检查并手动删除，或者联系开发者改进脚本。" -ForegroundColor Cyan
