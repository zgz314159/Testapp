# Fails when PracticeViewModel.kt exceeds 500 LOC.
$path = Join-Path $PSScriptRoot "..\feature-practice\src\main\java\com\example\testapp\presentation\screen\practice\PracticeViewModel.kt"
$maxLines = 500
if (-not (Test-Path $path)) {
    Write-Error "PracticeViewModel.kt not found: $path"
    exit 1
}
$lineCount = (Get-Content $path | Measure-Object -Line).Lines
Write-Host "PracticeViewModel.kt: $lineCount lines (max $maxLines)"
if ($lineCount -gt $maxLines) {
    Write-Error "PracticeViewModel.kt exceeds $maxLines lines. See .ai/practice_viewmodel_decomposition.md."
    exit 1
}
exit 0
