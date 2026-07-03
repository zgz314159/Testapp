# Fails when PracticeScreen.kt exceeds the architecture redline (500 LOC).
$path = Join-Path $PSScriptRoot "..\app\src\main\java\com\example\testapp\presentation\screen\practice\PracticeScreen.kt"
$maxLines = 500
if (-not (Test-Path $path)) {
    Write-Error "PracticeScreen.kt not found: $path"
    exit 1
}
$lineCount = (Get-Content $path | Measure-Object -Line).Lines
Write-Host "PracticeScreen.kt: $lineCount lines (max $maxLines)"
if ($lineCount -gt $maxLines) {
    Write-Error "PracticeScreen.kt exceeds $maxLines lines. Extract composables/pipelines before adding code. See .ai/practice_screen_decomposition.md"
    exit 1
}
exit 0
