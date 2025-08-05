Write-Host "开始删除项目中的所有日志语句..." -ForegroundColor Green

$projectPath = "app\src\main\java"
$totalFilesProcessed = 0
$totalLogsRemoved = 0
$filesModified = 0

# 获取所有Kotlin文件
$kotlinFiles = Get-ChildItem -Path $projectPath -Recurse -Filter "*.kt" | Where-Object { -not $_.Name.EndsWith(".backup") }

Write-Host "找到 $($kotlinFiles.Count) 个Kotlin文件" -ForegroundColor Yellow

foreach ($file in $kotlinFiles) {
    $totalFilesProcessed++
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $originalContent = $content
    $fileLogCount = 0
    
    Write-Host "处理文件: $($file.Name)" -ForegroundColor Cyan
    
    # 删除android.util.Log语句
    $matches = [regex]::Matches($content, 'android\.util\.Log\.[deivw]\s*\([^)]*\)')
    $fileLogCount += $matches.Count
    $content = [regex]::Replace($content, '\s*android\.util\.Log\.[deivw]\s*\([^)]*\)\s*(?:\r?\n)?', '')
    
    # 删除简化Log语句  
    $matches = [regex]::Matches($content, '(?<!android\.util\.)\bLog\.[deivw]\s*\([^)]*\)')
    $fileLogCount += $matches.Count
    $content = [regex]::Replace($content, '\s*(?<!android\.util\.)\bLog\.[deivw]\s*\([^)]*\)\s*(?:\r?\n)?', '')
    
    # 删除println语句
    $matches = [regex]::Matches($content, 'println\s*\([^)]*\)')
    $fileLogCount += $matches.Count
    $content = [regex]::Replace($content, '\s*println\s*\([^)]*\)\s*(?:\r?\n)?', '')
    
    # 删除System.out.println语句
    $matches = [regex]::Matches($content, 'System\.out\.println\s*\([^)]*\)')
    $fileLogCount += $matches.Count
    $content = [regex]::Replace($content, '\s*System\.out\.println\s*\([^)]*\)\s*(?:\r?\n)?', '')
    
    # 清理多余的空行
    $content = [regex]::Replace($content, '(\r?\n\s*){3,}', "`r`n`r`n")
    
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
        $filesModified++
        $totalLogsRemoved += $fileLogCount
        Write-Host "  ✓ 删除了 $fileLogCount 个日志语句" -ForegroundColor Green
    } else {
        Write-Host "  - 没有找到日志语句" -ForegroundColor Gray
    }
}

Write-Host "`n=================== 删除完成统计 ===================" -ForegroundColor Green
Write-Host "处理文件总数: $totalFilesProcessed" -ForegroundColor White
Write-Host "修改文件数量: $filesModified" -ForegroundColor White  
Write-Host "删除日志总数: $totalLogsRemoved" -ForegroundColor White
Write-Host "======================================================" -ForegroundColor Green
