# 全面删除项目中所有日志语句的脚本
# 支持的日志格式：
# 1. android.util.Log.d/e/i/v/w()
# 2. Log.d/e/i/v/w() 
# 3. 多行日志语句

param(
    [string]$ProjectPath = "app\src\main\java"
)

Write-Host "开始分析和删除项目中的所有日志语句..." -ForegroundColor Green
Write-Host "项目路径: $ProjectPath" -ForegroundColor Yellow

# 统计信息
$totalFilesProcessed = 0
$totalLogsRemoved = 0
$filesModified = 0

# 获取所有Kotlin文件
$kotlinFiles = Get-ChildItem -Path $ProjectPath -Recurse -Filter "*.kt" | Where-Object { -not $_.Name.EndsWith(".backup") }

Write-Host "找到 $($kotlinFiles.Count) 个Kotlin文件" -ForegroundColor Yellow

foreach ($file in $kotlinFiles) {
    $totalFilesProcessed++
    $originalContent = Get-Content $file.FullName -Raw -Encoding UTF8
    $modifiedContent = $originalContent
    $fileLogCount = 0
    
    Write-Host "处理文件: $($file.Name)" -ForegroundColor Cyan
    
    # 模式1: 删除android.util.Log的各种方法调用（包括多行）
    $pattern1 = '(?s)\s*android\.util\.Log\.[deivw]\s*\([^)]*(?:\([^)]*\)[^)]*)*\)\s*(?:\r?\n)?'
    $matches1 = [regex]::Matches($modifiedContent, $pattern1)
    $fileLogCount += $matches1.Count
    if ($matches1.Count -gt 0) {
        $modifiedContent = [regex]::Replace($modifiedContent, $pattern1, "")
        Write-Host "  - 删除了 $($matches1.Count) 个 android.util.Log 语句" -ForegroundColor Green
    }
    
    # 模式2: 删除简化Log的各种方法调用（包括多行）
    # 使用负向先行断言确保不匹配android.util.Log
    $pattern2 = '(?s)(?<!android\.util\.)\bLog\.[deivw]\s*\([^)]*(?:\([^)]*\)[^)]*)*\)\s*(?:\r?\n)?'
    $matches2 = [regex]::Matches($modifiedContent, $pattern2)
    $fileLogCount += $matches2.Count
    if ($matches2.Count -gt 0) {
        $modifiedContent = [regex]::Replace($modifiedContent, $pattern2, "")
        Write-Host "  - 删除了 $($matches2.Count) 个简化Log语句" -ForegroundColor Green
    }
    
    # 模式3: 删除println()调用
    $pattern3 = '\s*println\s*\([^)]*\)\s*(?:\r?\n)?'
    $matches3 = [regex]::Matches($modifiedContent, $pattern3)
    $fileLogCount += $matches3.Count
    if ($matches3.Count -gt 0) {
        $modifiedContent = [regex]::Replace($modifiedContent, $pattern3, "")
        Write-Host "  - 删除了 $($matches3.Count) 个println语句" -ForegroundColor Green
    }
    
    # 模式4: 删除System.out.println()调用
    $pattern4 = '\s*System\.out\.println\s*\([^)]*\)\s*(?:\r?\n)?'
    $matches4 = [regex]::Matches($modifiedContent, $pattern4)
    $fileLogCount += $matches4.Count
    if ($matches4.Count -gt 0) {
        $modifiedContent = [regex]::Replace($modifiedContent, $pattern4, "")
        Write-Host "  - 删除了 $($matches4.Count) 个System.out.println语句" -ForegroundColor Green
    }
    
    # 删除多余的空行（连续的空行变成单个空行）
    $modifiedContent = [regex]::Replace($modifiedContent, '(\r?\n\s*){3,}', "`r`n`r`n")
    
    # 如果内容有变化，保存文件
    if ($modifiedContent -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $modifiedContent -Encoding UTF8 -NoNewline
        $filesModified++
        $totalLogsRemoved += $fileLogCount
        Write-Host "  ✓ 文件已修改，删除了 $fileLogCount 个日志语句" -ForegroundColor Green
    } else {
        Write-Host "  - 没有找到日志语句" -ForegroundColor Gray
    }
}

# 额外处理：删除可能残留的Log import语句（如果没有使用Log的话）
Write-Host "`n检查并删除未使用的Log import语句..." -ForegroundColor Yellow

foreach ($file in $kotlinFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    
    # 检查是否还有Log使用
    $hasLogUsage = $content -match '\bLog\.[deivw]' -or $content -match 'android\.util\.Log'
    
    # 如果没有Log使用，删除import语句
    if (-not $hasLogUsage) {
        $originalImportContent = $content
        # 删除Log相关的import
        $content = $content -replace '(?m)^\s*import\s+android\.util\.Log\s*$\r?\n?', ''
        
        if ($content -ne $originalImportContent) {
            Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
            Write-Host "  ✓ 删除了 $($file.Name) 中未使用的Log import" -ForegroundColor Green
        }
    }
}

Write-Host "`n=================== 删除完成统计 ===================" -ForegroundColor Green
Write-Host "处理文件总数: $totalFilesProcessed" -ForegroundColor White
Write-Host "修改文件数量: $filesModified" -ForegroundColor White  
Write-Host "删除日志总数: $totalLogsRemoved" -ForegroundColor White
Write-Host "======================================================" -ForegroundColor Green

Write-Host "`n建议：请检查修改后的代码是否编译正常，如有问题可以从备份文件恢复。" -ForegroundColor Yellow
