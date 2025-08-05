# 专门清理剩余日志的精确脚本
# 使用方法: .\remove_remaining_logs.ps1

Write-Host "开始清理剩余的日志语句..." -ForegroundColor Green

# 定义项目源码目录
$sourceDir = "app\src\main\java"

# 获取所有Kotlin和Java文件
$files = Get-ChildItem -Path $sourceDir -Include "*.kt", "*.java" -Recurse

$totalFiles = 0
$modifiedFiles = 0
$totalLogsRemoved = 0

foreach ($file in $files) {
    $totalFiles++
    $lines = Get-Content $file.FullName -Encoding UTF8
    $newLines = @()
    $fileLogsRemoved = 0
    $inMultiLineLog = $false
    $logBuffer = ""
    
    Write-Host "处理文件: $($file.Name)" -ForegroundColor Cyan
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $trimmedLine = $line.Trim()
        $shouldKeepLine = $true
        
        # 检查是否是日志行的开始
        if ($trimmedLine -match '^android\.util\.Log\.[deivwWTF]\s*\(' -or 
            $trimmedLine -match '^Log\.[deivwWTF]\s*\(' -or
            $trimmedLine -match '^println\s*\(' -or
            $trimmedLine -match '^System\.out\.println\s*\(') {
            
            $shouldKeepLine = $false
            $fileLogsRemoved++
            $logPreview = $trimmedLine.Substring(0, [Math]::Min(60, $trimmedLine.Length))
            Write-Host "  删除日志: $logPreview..." -ForegroundColor Gray
            
            # 检查这行是否完整（以 ) 结尾且括号匹配）
            $openParens = ($line.ToCharArray() | Where-Object { $_ -eq '(' }).Count
            $closeParens = ($line.ToCharArray() | Where-Object { $_ -eq ')' }).Count
            
            if ($openParens -gt $closeParens) {
                $inMultiLineLog = $true
                $logBuffer = $line
            }
        }
        # 如果在多行日志中
        elseif ($inMultiLineLog) {
            $shouldKeepLine = $false
            $logBuffer += " " + $line
            
            # 检查是否到了多行日志的结束
            $openParens = ($logBuffer.ToCharArray() | Where-Object { $_ -eq '(' }).Count
            $closeParens = ($logBuffer.ToCharArray() | Where-Object { $_ -eq ')' }).Count
            
            if ($closeParens -ge $openParens) {
                $inMultiLineLog = $false
                $logBuffer = ""
                Write-Host "  完成多行日志删除" -ForegroundColor Gray
            }
        }
        # 检查行中是否包含内嵌的日志语句
        elseif ($line -match 'android\.util\.Log\.[deivwWTF]\(' -or 
                $line -match 'Log\.[deivwWTF]\(' -or
                $line -match 'println\(' -or
                $line -match 'System\.out\.println\(') {
            
            # 尝试移除日志语句但保留其他代码
            $cleanedLine = $line
            $cleanedLine = $cleanedLine -replace 'android\.util\.Log\.[deivwWTF]\([^)]*\)\s*', ''
            $cleanedLine = $cleanedLine -replace 'Log\.[deivwWTF]\([^)]*\)\s*', ''
            $cleanedLine = $cleanedLine -replace 'println\([^)]*\)\s*', ''
            $cleanedLine = $cleanedLine -replace 'System\.out\.println\([^)]*\)\s*', ''
            
            if ($cleanedLine.Trim() -ne $line.Trim()) {
                $fileLogsRemoved++
                Write-Host "  清理内嵌日志: $($line.Trim())" -ForegroundColor Gray
                
                # 如果清理后行变成空行或只有空白，跳过
                if ($cleanedLine.Trim() -eq "") {
                    $shouldKeepLine = $false
                } else {
                    $newLines += $cleanedLine
                    $shouldKeepLine = $false
                }
            }
        }
        
        if ($shouldKeepLine) {
            $newLines += $line
        }
    }
    
    # 如果有日志被删除，写入文件
    if ($fileLogsRemoved -gt 0) {
        $modifiedFiles++
        $totalLogsRemoved += $fileLogsRemoved
        
        # 备份原文件
        $backupPath = $file.FullName + ".backup2"
        Copy-Item $file.FullName $backupPath
        
        # 写入修改后的内容
        $newLines | Set-Content $file.FullName -Encoding UTF8
        
        Write-Host "  修改完成: 删除了 $fileLogsRemoved 个日志语句" -ForegroundColor Green
    } else {
        Write-Host "  无需修改: 未发现日志语句" -ForegroundColor Gray
    }
}

Write-Host "`n清理完成!" -ForegroundColor Green
Write-Host "统计信息:" -ForegroundColor Yellow
Write-Host "  - 总文件数: $totalFiles" -ForegroundColor White
Write-Host "  - 修改文件数: $modifiedFiles" -ForegroundColor White  
Write-Host "  - 删除日志总数: $totalLogsRemoved" -ForegroundColor White

if ($modifiedFiles -gt 0) {
    Write-Host "`n注意事项:" -ForegroundColor Yellow
    Write-Host "  - 原文件已备份为 .backup2 文件" -ForegroundColor White
    Write-Host "  - 请测试修改后的代码是否正常运行" -ForegroundColor White
}
