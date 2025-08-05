# Git合并冲突清理脚本
# 对PracticeScreen.kt文件进行统一清理

# 读取文件内容并清理冲突标记
$filePath = "app\src\main\java\com\example\testapp\presentation\screen\PracticeScreen.kt"
$content = Get-Content $filePath -Raw

# 清理所有Git冲突标记
$cleanContent = $content -replace "<<<<<<< HEAD\r?\n", ""
$cleanContent = $cleanContent -replace "=======\r?\n", ""
$cleanContent = $cleanContent -replace ">>>>>>> [^\r\n]+\r?\n", ""

# 清理多余的空行（将3个以上连续的换行符替换为2个）
$cleanContent = $cleanContent -replace "\r?\n\r?\n\r?\n+", "`r`n`r`n"

# 写回文件
Set-Content $filePath $cleanContent -NoNewline

Write-Host "已清理PracticeScreen.kt中的Git冲突标记"
