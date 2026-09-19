$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$res = Join-Path $root 'app/src/main/res'

function Read-Strings([string]$folder) {
    $result = @{}
    foreach ($file in Get-ChildItem (Join-Path $res "$folder/*.xml")) {
        [xml]$document = Get-Content $file.FullName -Raw -Encoding utf8
        foreach ($entry in $document.resources.string) {
            if (!$entry.name) { continue }
            if ($result.ContainsKey($entry.name)) { throw "$folder duplicate key: $($entry.name)" }
            $result[$entry.name] = [string]$entry.InnerText
        }
    }
    return $result
}

$baseline = Read-Strings 'values'
[xml]$config = Get-Content (Join-Path $res 'xml/locales_config.xml') -Raw -Encoding utf8
$tags = @($config.'locale-config'.locale | ForEach-Object { $_.GetAttribute('name', 'http://schemas.android.com/apk/res/android') })
$enumText = Get-Content (Join-Path $root 'app/src/main/java/com/hayhak/turkcesozluk/data/db/AppLocale.kt') -Raw -Encoding utf8
$enumTags = @([regex]::Matches($enumText, '\w+\("([a-z]+(?:-[A-Z]+)?)",') | ForEach-Object { $_.Groups[1].Value })
if (Compare-Object $tags $enumTags) { throw 'Locale picker and locales_config differ' }

$html = Get-Content (Join-Path $root 'app/src/main/assets/privacy-policy.html') -Raw -Encoding utf8
$privacyMatch = [regex]::Match($html, '(?m)^\s*const I18N = (.+);\s*$')
if (!$privacyMatch.Success) { throw 'Bundled privacy translations not found' }
$privacy = $privacyMatch.Groups[1].Value | ConvertFrom-Json
$privacyTags = @($privacy.PSObject.Properties.Name)
if (Compare-Object $tags $privacyTags) { throw 'Bundled privacy locales differ' }

$formatPattern = '%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z]'
foreach ($tag in $tags) {
    $qualifier = switch ($tag) { 'id' { 'in' } 'zh-CN' { 'zh-rCN' } default { $tag } }
    $strings = Read-Strings "values-$qualifier"
    foreach ($key in $baseline.Keys) {
        if (!$strings.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($strings[$key])) {
            throw "$tag missing or empty: $key"
        }
        $expected = @([regex]::Matches($baseline[$key], $formatPattern).Value | Sort-Object)
        $actual = @([regex]::Matches($strings[$key], $formatPattern).Value | Sort-Object)
        if (($expected -join '|') -cne ($actual -join '|')) { throw "$tag placeholder mismatch: $key" }
    }
    foreach ($key in $strings.Keys) {
        if (!$baseline.ContainsKey($key)) { throw "$tag unknown key: $key" }
    }
    $policy = $privacy.$tag
    if (!$policy.title -or !$policy.intro -or $policy.sections.Count -ne 12) { throw "$tag incomplete privacy policy" }
    foreach ($section in $policy.sections) {
        if (!$section.title -or !(@($section.paragraphs).Count + @($section.items).Count)) {
            throw "$tag empty privacy section"
        }
    }
    Write-Output "$tag`: $($strings.Count) strings, placeholders OK, bundled privacy OK"
}
$gradle = Get-Content (Join-Path $root 'app/build.gradle.kts') -Raw
if ($gradle -notmatch 'enableSplit\s*=\s*false') { throw 'Offline language packaging disabled' }
Write-Output "PASS: $($tags.Count) complete locales; language splits disabled. This is a structural check, not device visual QA."
