$filePath = "app\src\main\java\com\example\testapp\presentation\navigation\AppNavHost.kt"
$content = Get-Content $filePath -Raw
$content = $content -replace 'java\.net\.URLEncoder\.encode\(text, "UTF-8"\)', 'safeEncode(text)'
$content | Set-Content $filePath
Write-Host "Fixed URL encoding calls in AppNavHost.kt"
