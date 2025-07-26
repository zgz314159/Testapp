# 批量删除Android项目中所有日志语句的PowerShell脚本
# 使用方法: .\remove_logs.ps1

Write-Host "开始删除项目中的所有日志语句..." -ForegroundColor Green

# 定义要处理的文件扩展名
$extensions = @("*.kt", "*.java")

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

# 定义要删除的日志模式（正则表达式）
$logPatterns = @(
    'android\.util\.Log\.[deivw]\([^)]*\)\s*',           # android.util.Log.d/e/i/v/w(...)
    'Log\.[deivw]\([^)]*\)\s*',                          # Log.d/e/i/v/w(...)
    'println\([^)]*\)\s*',                               # println(...)
    'System\.out\.println\([^)]*\)\s*'                   # System.out.println(...)
)

# 获取所有要处理的文件
$files = Get-ChildItem -Path $sourceDir -Include $extensions -Recurse

Write-Host "找到 $($files.Count) 个源码文件需要处理..." -ForegroundColor Yellow

foreach ($file in $files) {
    $totalFiles++
    $originalContent = Get-Content $file.FullName -Raw -Encoding UTF8
    $modifiedContent = $originalContent
    $fileLogsRemoved = 0
    
    # 应用每个日志模式
    foreach ($pattern in $logPatterns) {
        $logMatches = [regex]::Matches($modifiedContent, $pattern)
        if ($logMatches.Count -gt 0) {
            $fileLogsRemoved += $logMatches.Count
            $modifiedContent = [regex]::Replace($modifiedContent, $pattern, '')
        }
    }
    
    # 删除空的日志行（只包含空白字符的行）
    $lines = $modifiedContent -split "`n"
    $cleanedLines = @()
    
    for ($i = 0; $i -lt $lines.Length; $i++) {
        $line = $lines[$i]
        $trimmedLine = $line.Trim()
        
        # 如果是空行，检查前后是否都是代码行，如果是则可能是删除日志后留下的空行
        if ([string]::IsNullOrWhiteSpace($trimmedLine)) {
            # 保留必要的空行，删除多余的空行
            if ($cleanedLines.Count -gt 0 -and -not [string]::IsNullOrWhiteSpace($cleanedLines[-1].Trim())) {
                $cleanedLines += $line
            }
        } else {
            $cleanedLines += $line
        }
    }
    
    $finalContent = $cleanedLines -join "`n"
    
    # 如果内容有变化，则写入文件
    if ($originalContent -ne $finalContent) {
        $modifiedFiles++
        $totalLogsRemoved += $fileLogsRemoved
        
        # 备份原文件
        $backupPath = $file.FullName + ".backup"
        Copy-Item $file.FullName $backupPath
        
        # 写入修改后的内容
        $finalContent | Set-Content $file.FullName -Encoding UTF8 -NoNewline
        
        Write-Host "  修改文件: $($file.Name) (删除了 $fileLogsRemoved 个日志语句)" -ForegroundColor Cyan
    }
}

Write-Host "`n删除完成!" -ForegroundColor Green
Write-Host "统计信息:" -ForegroundColor Yellow
Write-Host "  - 总文件数: $totalFiles" -ForegroundColor White
Write-Host "  - 修改文件数: $modifiedFiles" -ForegroundColor White
Write-Host "  - 删除日志总数: $totalLogsRemoved" -ForegroundColor White

if ($modifiedFiles -gt 0) {
    Write-Host "`n注意:" -ForegroundColor Yellow
    Write-Host "  - 原文件已备份为 .backup 文件" -ForegroundColor White
    Write-Host "  - 请检查修改后的代码是否正确" -ForegroundColor White
    Write-Host "  - 如需恢复，可以使用备份文件" -ForegroundColor White
    Write-Host "`n删除备份文件的命令:" -ForegroundColor Yellow
    Write-Host "  Get-ChildItem -Path '$sourceDir' -Filter '*.backup' -Recurse | Remove-Item" -ForegroundColor Gray
}
