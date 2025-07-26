# Ultra Advanced Log Removal Script - Handles complex nested patterns
# Usage: .\remove_logs_ultimate.ps1

Write-Host "Starting ultimate log removal process..." -ForegroundColor Green

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
    
    # Helper function to match balanced parentheses
    function Remove-LogStatements {
        param([string]$text, [string]$logPrefix)
        
        $result = $text
        $removedCount = 0
        
        # Find all log statement start positions
        $pattern = "$logPrefix\s*\("
        $foundMatches = [regex]::Matches($result, $pattern)
        
        # Process matches from end to start to avoid index shifts
        for ($i = $foundMatches.Count - 1; $i -ge 0; $i--) {
            $match = $foundMatches[$i]
            $startPos = $match.Index
            $openParenPos = $match.Index + $match.Length - 1
            
            # Find the matching closing parenthesis
            $parenCount = 1
            $pos = $openParenPos + 1
            $inString = $false
            $stringChar = $null
            $escaped = $false
            
            while ($pos -lt $result.Length -and $parenCount -gt 0) {
                $char = $result[$pos]
                
                if ($escaped) {
                    $escaped = $false
                } elseif ($char -eq '\') {
                    $escaped = $true
                } elseif (!$inString -and ($char -eq '"' -or $char -eq "'")) {
                    $inString = $true
                    $stringChar = $char
                } elseif ($inString -and $char -eq $stringChar) {
                    $inString = $false
                    $stringChar = $null
                } elseif (!$inString) {
                    if ($char -eq '(') {
                        $parenCount++
                    } elseif ($char -eq ')') {
                        $parenCount--
                    }
                }
                $pos++
            }
            
            if ($parenCount -eq 0) {
                # Found complete log statement, remove it
                $endPos = $pos
                $length = $endPos - $startPos
                $result = $result.Remove($startPos, $length)
                $removedCount++
            }
        }
        
        return @{
            Text = $result
            Count = $removedCount
        }
    }
    
    # Remove android.util.Log statements
    $result1 = Remove-LogStatements $content "android\.util\.Log\.[deivwDEIVW]"
    if ($result1.Count -gt 0) {
        $content = $result1.Text
        $fileLogsRemoved += $result1.Count
        Write-Host "  Removed $($result1.Count) android.util.Log statements" -ForegroundColor Gray
    }
    
    # Remove simple Log statements (but not android.util.Log)
    $result2 = Remove-LogStatements $content "(?<!android\.util\.)Log\.[deivwDEIVW]"
    if ($result2.Count -gt 0) {
        $content = $result2.Text
        $fileLogsRemoved += $result2.Count
        Write-Host "  Removed $($result2.Count) Log statements" -ForegroundColor Gray
    }
    
    # Remove println statements
    $result3 = Remove-LogStatements $content "println"
    if ($result3.Count -gt 0) {
        $content = $result3.Text
        $fileLogsRemoved += $result3.Count
        Write-Host "  Removed $($result3.Count) println statements" -ForegroundColor Gray
    }
    
    # Remove System.out.println statements
    $result4 = Remove-LogStatements $content "System\.out\.println"
    if ($result4.Count -gt 0) {
        $content = $result4.Text
        $fileLogsRemoved += $result4.Count
        Write-Host "  Removed $($result4.Count) System.out.println statements" -ForegroundColor Gray
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

Write-Host "`n🎉 Ultimate log removal completed!" -ForegroundColor Green
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
