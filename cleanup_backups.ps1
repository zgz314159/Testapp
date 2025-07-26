# 清理备份文件的脚本
# 使用方法: .\cleanup_backups.ps1

Write-Host "开始清理备份文件..." -ForegroundColor Green

$sourceDir = "app\src\main\java"

# 查找所有备份文件
$backupFiles = Get-ChildItem -Path $sourceDir -Filter "*.backup" -Recurse

if ($backupFiles.Count -eq 0) {
    Write-Host "未找到备份文件。" -ForegroundColor Yellow
    exit 0
}

Write-Host "找到 $($backupFiles.Count) 个备份文件:" -ForegroundColor Yellow

foreach ($backup in $backupFiles) {
    Write-Host "  - $($backup.FullName)" -ForegroundColor Gray
}

# 询问用户确认
$confirmation = Read-Host "`n确定要删除所有备份文件吗？输入 'y' 或 'yes' 确认"

if ($confirmation -eq 'y' -or $confirmation -eq 'yes') {
    $backupFiles | Remove-Item -Force
    Write-Host "所有备份文件已删除！" -ForegroundColor Green
} else {
    Write-Host "操作已取消。" -ForegroundColor Yellow
}
