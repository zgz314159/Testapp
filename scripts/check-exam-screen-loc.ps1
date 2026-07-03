# Fails when ExamScreenContent.kt exceeds 500 LOC.
$path = Join-Path $PSScriptRoot "..\feature-exam\src\main\java\com\example\testapp\presentation\screen\exam\ExamScreenContent.kt"
$maxLines = 500
if (-not (Test-Path $path)) {
    Write-Error "ExamScreenContent.kt not found: $path"
    exit 1
}
$lineCount = (Get-Content $path | Measure-Object -Line).Lines
Write-Host "ExamScreenContent.kt: $lineCount lines (max $maxLines)"
if ($lineCount -gt $maxLines) {
    Write-Error "ExamScreenContent.kt exceeds $maxLines lines. See .ai/practice_screen_decomposition.md (Exam section)."
    exit 1
}
exit 0
