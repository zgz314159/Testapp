# Enhanced Log Removal Script for Android Project
# Usage: .\remove_all_logs.ps1

Write-Host "Starting enhanced log removal process..." -ForegroundColor Green

$sourceDir = "app\src\main\java"

if (-not (Test-Path $sourceDir)) {
    Write-Host "Error: Source directory $sourceDir not found" -ForegroundColor Red
    exit 1
}

$totalFiles = 0
$modifiedFiles = 0
$totalLogsRemoved = 0

# Get all Kotlin and Java files
$files = Get-ChildItem -Path $sourceDir -Include "*.kt", "*.java" -Recurse

Write-Host "Found $($files.Count) source files to process..." -ForegroundColor Yellow

foreach ($file in $files) {
    $totalFiles++
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $originalContent = $content
    $fileLogsRemoved = 0
    
    Write-Host "Processing file: $($file.Name)" -ForegroundColor Cyan
    
    # Pattern 1: android.util.Log statements with complex parameters
    $pattern1 = 'android\.util\.Log\.[deivwDEIVW]\s*\([^)]*\)\s*'
    $matches1 = [regex]::Matches($content, $pattern1, [System.Text.RegularExpressions.RegexOptions]::Singleline)
    if ($matches1.Count -gt 0) {
        $fileLogsRemoved += $matches1.Count
        $content = [regex]::Replace($content, $pattern1, '', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        Write-Host "  Removed $($matches1.Count) android.util.Log statements" -ForegroundColor Gray
    }
    
    # Pattern 2: Simple Log.* statements  
    $pattern2 = '(?<!android\.util\.)Log\.[deivwDEIVW]\s*\([^)]*\)\s*'
    $matches2 = [regex]::Matches($content, $pattern2, [System.Text.RegularExpressions.RegexOptions]::Singleline)
    if ($matches2.Count -gt 0) {
        $fileLogsRemoved += $matches2.Count
        $content = [regex]::Replace($content, $pattern2, '', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        Write-Host "  Removed $($matches2.Count) Log statements" -ForegroundColor Gray
    }
    
    # Pattern 3: println statements
    $pattern3 = 'println\s*\([^)]*\)\s*'
    $matches3 = [regex]::Matches($content, $pattern3, [System.Text.RegularExpressions.RegexOptions]::Singleline)
    if ($matches3.Count -gt 0) {
        $fileLogsRemoved += $matches3.Count
        $content = [regex]::Replace($content, $pattern3, '', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        Write-Host "  Removed $($matches3.Count) println statements" -ForegroundColor Gray
    }
    
    # Pattern 4: System.out.println statements
    $pattern4 = 'System\.out\.println\s*\([^)]*\)\s*'
    $matches4 = [regex]::Matches($content, $pattern4, [System.Text.RegularExpressions.RegexOptions]::Singleline)
    if ($matches4.Count -gt 0) {
        $fileLogsRemoved += $matches4.Count
        $content = [regex]::Replace($content, $pattern4, '', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        Write-Host "  Removed $($matches4.Count) System.out.println statements" -ForegroundColor Gray
    }
    
    # Clean up consecutive empty lines
    $content = [regex]::Replace($content, '\r?\n\s*\r?\n\s*\r?\n', "`r`n`r`n", [System.Text.RegularExpressions.RegexOptions]::Multiline)
    
    # Write file if content changed
    if ($originalContent -ne $content) {
        $modifiedFiles++
        $totalLogsRemoved += $fileLogsRemoved
        
        # Backup original file
        $backupPath = $file.FullName + ".backup"
        Copy-Item $file.FullName $backupPath
        
        # Write modified content
        $content | Set-Content $file.FullName -Encoding UTF8 -NoNewline
        
        Write-Host "  ✅ Modified: Removed $fileLogsRemoved log statements" -ForegroundColor Green
    } else {
        Write-Host "  ⭕ No changes: No log statements found" -ForegroundColor Gray
    }
}

Write-Host "`n🎉 Log removal completed!" -ForegroundColor Green
Write-Host "📊 Statistics:" -ForegroundColor Yellow
Write-Host "  - Total files: $totalFiles" -ForegroundColor White
Write-Host "  - Modified files: $modifiedFiles" -ForegroundColor White  
Write-Host "  - Total logs removed: $totalLogsRemoved" -ForegroundColor White

if ($modifiedFiles -gt 0) {
    Write-Host "`n⚠️  Important notes:" -ForegroundColor Yellow
    Write-Host "  - Original files backed up as .backup files" -ForegroundColor White
    Write-Host "  - Please test the modified code" -ForegroundColor White
    Write-Host "  - Use backup files to restore if needed" -ForegroundColor White
    Write-Host "`n🧹 Command to remove all backup files:" -ForegroundColor Yellow
    Write-Host "  Get-ChildItem -Path '$sourceDir' -Filter '*.backup' -Recurse | Remove-Item -Force" -ForegroundColor Gray
    Write-Host "`n🔄 Example to restore a single file:" -ForegroundColor Yellow
    Write-Host "  Copy-Item 'ExamScreen.kt.backup' 'ExamScreen.kt' -Force" -ForegroundColor Gray
}
