$screens = @(
    "app\src\main\java\com\example\testapp\presentation\screen\BaiduAskScreen.kt",
    "app\src\main\java\com\example\testapp\presentation\screen\BaiduScreen.kt", 
    "app\src\main\java\com\example\testapp\presentation\screen\DeepSeekAskScreen.kt",
    "app\src\main\java\com\example\testapp\presentation\screen\DeepSeekScreen.kt",
    "app\src\main\java\com\example\testapp\presentation\screen\SparkAskScreen.kt",
    "app\src\main\java\com\example\testapp\presentation\screen\SparkScreen.kt"
)

foreach ($screen in $screens) {
    if (Test-Path $screen) {
        $content = Get-Content $screen -Raw
        $content = $content -replace 'java\.net\.URLEncoder\.encode\(selected, "UTF-8"\)', 'com.example.testapp.util.safeEncode(selected)'
        $content | Set-Content $screen
        Write-Host "Fixed URL encoding in $screen"
    }
}

Write-Host "Completed fixing URL encoding in all screen files"
