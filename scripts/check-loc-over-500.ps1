$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$exclude = @('\build\', '\.gradle\', '\bin\', '\.idea\')
Get-ChildItem -Path $root -Recurse -Include '*.kt','*.java' -File | ForEach-Object {
    $path = $_.FullName
    foreach ($ex in $exclude) {
        if ($path -like "*$ex*") { return }
    }
    $lineCount = (Get-Content $path | Measure-Object -Line).Lines
    if ($lineCount -gt 500) {
        $rel = $path
        if ($path.StartsWith($root)) {
            $rel = $path.Substring($root.Length).TrimStart('\', '/')
        }
        [PSCustomObject]@{
            Lines = $lineCount
            Path = $rel
        }
    }
} | Sort-Object Lines -Descending | Format-Table -AutoSize
