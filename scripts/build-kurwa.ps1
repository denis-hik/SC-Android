param([string]$Logo)
$ErrorActionPreference = 'Stop'
$repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (!$Logo) { $Logo = Join-Path $repo 'other/kurwa-local/author_logo.png' }
if (!(Test-Path -LiteralPath $Logo -PathType Leaf)) { throw "Missing local transparent logo: $Logo" }
$output = Join-Path $repo 'build/kurwa'
$stage = [IO.Path]::GetFullPath((Join-Path $output 'project'))
# Only the generated snapshot may be deleted. Never modify the source tree.
if ($stage -ne [IO.Path]::GetFullPath((Join-Path $repo 'build/kurwa/project'))) { throw 'Unexpected staging path' }
$lock = $null
New-Item -ItemType Directory -Force -Path $output | Out-Null
try {
    $lock = [IO.File]::Open((Join-Path $output 'build.lock'), 'OpenOrCreate', 'ReadWrite', 'None')
    if (Test-Path -LiteralPath $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
    New-Item -ItemType Directory -Force -Path (Join-Path $stage 'app') | Out-Null
    foreach ($name in @('build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','local.properties','gradle')) {
        Copy-Item -LiteralPath (Join-Path $repo $name) -Destination $stage -Recurse -Force
    }
    foreach ($name in @('build.gradle','proguard-rules.pro','src','libs')) {
        $source = Join-Path $repo "app/$name"
        if (Test-Path -LiteralPath $source) { Copy-Item -LiteralPath $source -Destination (Join-Path $stage 'app') -Recurse -Force }
    }
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    $textExtensions = @('.java','.kt','.xml','.json','.html','.js','.css','.txt','.properties','.pro','.gradle')
    $files = @(Get-ChildItem -LiteralPath (Join-Path $stage 'app/src') -File -Recurse)
    $files += Get-Item -LiteralPath (Join-Path $stage 'app/build.gradle')
    foreach ($file in $files) {
        if ($textExtensions -notcontains $file.Extension) { continue }
        $text = [IO.File]::ReadAllText($file.FullName)
        $updated = [regex]::Replace($text, 'denishik', 'kurwa', 'IgnoreCase')
        if ($updated -cne $text) { [IO.File]::WriteAllText($file.FullName, $updated, $utf8) }
    }
    Copy-Item -LiteralPath $Logo -Destination (Join-Path $stage 'app/src/main/res/drawable-xhdpi/author_logo.png') -Force
    # The author link image is private to the generated Kurwa source tree.
    Copy-Item -LiteralPath $Logo -Destination (Join-Path $stage 'app/src/main/res/drawable-xhdpi/kurwa_author.png') -Force
    # Preserve the photo colours: the regular monochrome logo is tinted in main.xml.
    $mainLayout = Join-Path $stage 'app/src/main/res/layout/main.xml'
    $layout = [IO.File]::ReadAllText($mainLayout)
    $layout = [regex]::Replace($layout, '<ImageView\b[^>]*android:src="@drawable/author_logo"[^>]*/>', {
        param($match)
        [regex]::Replace($match.Value, '\s+(?:app|android):tint="[^"]*"', '')
    })
    [IO.File]::WriteAllText($mainLayout, $layout, $utf8)
    Push-Location $stage
    try {
        & (Join-Path $stage 'gradlew.bat') -I (Join-Path $PSScriptRoot 'kurwa-release.init.gradle') :app:assembleRelease :app:testReleaseUnitTest --console=plain --no-daemon --max-workers=2
        if ($LASTEXITCODE -ne 0) { throw "Kurwa build failed (Gradle exit $LASTEXITCODE)" }
    } finally { Pop-Location }
    $apk = Join-Path $output 'SC-Kurwa.apk'
    Copy-Item -LiteralPath (Join-Path $stage 'app/build/outputs/apk/release/app-release.apk') -Destination $apk -Force
    Write-Host "Kurwa APK: $apk"
} finally {
    if ($null -ne $lock) { $lock.Dispose() }
}
