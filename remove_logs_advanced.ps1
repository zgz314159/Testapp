# 改进版本的日志删除脚本 - 修复多行日志和嵌套日志问题
# 使用方法: .\remove_logs_advanced.ps1

Write-Host "开始删除项目中的所有日志语句（改进版）..." -ForegroundColor Green

# 定义项目源码目录
$sourceDir = "app\src\main\java"

# 检查目录是否存在
if (-not (Test-Path $sourceDir)) {
    Write-Host "错误: 找不到源码目录 $sourceDir" -ForegroundColor Red
    exit 1
}

# 统计变量
$totalFiles = 0
$modifiedFiles = 0
$totalLogsRemoved = 0

# 获取所有Kotlin和Java文件
$files = Get-ChildItem -Path $sourceDir -Include "*.kt", "*.java" -Recurse

Write-Host "找到 $($files.Count) 个源码文件需要处理..." -ForegroundColor Yellow

foreach ($file in $files) {
    $totalFiles++
    $content = Get-Content $file.FullName -Encoding UTF8 -Raw
    $originalContent = $content
    $fileLogsRemoved = 0
    
    Write-Host "处理文件: $($file.Name)" -ForegroundColor Cyan
    
    # 定义更全面的日志正则表达式模式
    $logPatterns = @(
        # 标准的android.util.Log语句（可能跨多行）
        'android\.util\.Log\.[deivw]\s*\([^)]*\)\s*',
        # 简短的Log语句
        'Log\.[deivw]\s*\([^)]*\)\s*',
        # println语句
        'println\s*\([^)]*\)\s*',
        # System.out.println语句
        'System\.out\.println\s*\([^)]*\)\s*'
    )
    
    # 使用更复杂的正则表达式来处理跨行的日志语句
    # 匹配 android.util.Log.d("tag", "message") 包括跨行的情况
    $advancedLogPattern = '(?s)android\.util\.Log\.[deivw]\s*\(\s*"[^"]*"\s*,\s*"[^"]*"\s*\)'
    
    # 先处理高级模式（跨行日志）
    $logMatches = [regex]::Matches($content, $advancedLogPattern)
    if ($logMatches.Count -gt 0) {
        $fileLogsRemoved += $logMatches.Count
        $content = [regex]::Replace($content, $advancedLogPattern, '')
        foreach ($match in $logMatches) {
            $preview = $match.Value.Replace("`n", " ").Replace("`r", "")
            if ($preview.Length -gt 80) { $preview = $preview.Substring(0, 80) + "..." }
            Write-Host "  删除跨行日志: $preview" -ForegroundColor Gray
        }
    }
    
    # 处理简单的单行日志模式
    foreach ($pattern in $logPatterns) {
        $logMatches = [regex]::Matches($content, $pattern)
        if ($logMatches.Count -gt 0) {
            $fileLogsRemoved += $logMatches.Count
            $content = [regex]::Replace($content, $pattern, '')
            foreach ($match in $logMatches) {
                $preview = $match.Value.Trim()
                if ($preview.Length -gt 60) { $preview = $preview.Substring(0, 60) + "..." }
                Write-Host "  删除单行日志: $preview" -ForegroundColor Gray
            }
        }
    }
    
    # 清理空行（删除日志后可能留下的空行）
    $lines = $content -split "`r?`n"
    $cleanedLines = @()
    $previousLineEmpty = $false
    
    foreach ($line in $lines) {
        $trimmedLine = $line.Trim()
        
        # 如果是空行
        if ([string]::IsNullOrWhiteSpace($trimmedLine)) {
            # 避免连续的多个空行，只保留一个
            if (-not $previousLineEmpty) {
                $cleanedLines += $line
                $previousLineEmpty = $true
            }
        } else {
            $cleanedLines += $line
            $previousLineEmpty = $false
        }
    }
    
    $finalContent = $cleanedLines -join "`n"
    
    # 如果内容有变化，写入文件
    if ($originalContent -ne $finalContent) {
        $modifiedFiles++
        $totalLogsRemoved += $fileLogsRemoved
        
        # 备份原文件
        $backupPath = $file.FullName + ".backup"
        Copy-Item $file.FullName $backupPath
        
        # 写入修改后的内容
        $finalContent | Set-Content $file.FullName -Encoding UTF8 -NoNewline
        
        Write-Host "  修改完成: 删除了 $fileLogsRemoved 个日志语句" -ForegroundColor Green
    } else {
        Write-Host "  无需修改: 未发现日志语句" -ForegroundColor Gray
    }
}

Write-Host "`n删除完成!" -ForegroundColor Green
Write-Host "统计信息:" -ForegroundColor Yellow
Write-Host "  - 总文件数: $totalFiles" -ForegroundColor White
Write-Host "  - 修改文件数: $modifiedFiles" -ForegroundColor White  
Write-Host "  - 删除日志总数: $totalLogsRemoved" -ForegroundColor White

if ($modifiedFiles -gt 0) {
    Write-Host "`n注意事项:" -ForegroundColor Yellow
    Write-Host "  - 原文件已备份为 .backup 文件" -ForegroundColor White
    Write-Host "  - 请测试修改后的代码是否正常运行" -ForegroundColor White
    Write-Host "  - 如需恢复原文件，可使用备份文件" -ForegroundColor White
    Write-Host "`n删除所有备份文件的命令:" -ForegroundColor Yellow
    Write-Host "  Get-ChildItem -Path '$sourceDir' -Filter '*.backup' -Recurse | Remove-Item -Force" -ForegroundColor Gray
}
