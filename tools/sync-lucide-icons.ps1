param(
    [string]$VersionFile = (Join-Path $PSScriptRoot 'lucide-version.txt'),
    [string]$IconsFile = (Join-Path $PSScriptRoot 'lucide-icons.txt'),
    [string]$CacheDir = (Join-Path $PSScriptRoot '.cache'),
    [string]$OutputDir = (Join-Path $PSScriptRoot '..\app\src\main\res\drawable')
)

$ErrorActionPreference = 'Stop'

function ConvertTo-ResourceName {
    param([string]$Name)

    return ('lucide_{0}' -f ($Name -replace '-', '_'))
}

function Format-Number {
    param([double]$Value)

    return $Value.ToString('0.###', [System.Globalization.CultureInfo]::InvariantCulture)
}

function Get-CirclePathData {
    param(
        [double]$Cx,
        [double]$Cy,
        [double]$R
    )

    $k = 0.5522847498307936 * $R
    $x0 = $Cx + $R
    $y0 = $Cy
    $x1 = $Cx
    $y1 = $Cy + $R
    $x2 = $Cx - $R
    $y2 = $Cy
    $x3 = $Cx
    $y3 = $Cy - $R

    return @(
        "M {0} {1}" -f (Format-Number $x0), (Format-Number $y0)
        "C {0} {1} {2} {3} {4} {5}" -f (Format-Number $x0), (Format-Number ($Cy + $k)), (Format-Number ($Cx + $k)), (Format-Number $y1), (Format-Number $x1), (Format-Number $y1)
        "C {0} {1} {2} {3} {4} {5}" -f (Format-Number ($Cx - $k)), (Format-Number $y1), (Format-Number $x2), (Format-Number ($Cy + $k)), (Format-Number $x2), (Format-Number $y2)
        "C {0} {1} {2} {3} {4} {5}" -f (Format-Number $x2), (Format-Number ($Cy - $k)), (Format-Number ($Cx - $k)), (Format-Number $y3), (Format-Number $x1), (Format-Number $y3)
        "C {0} {1} {2} {3} {4} {5}" -f (Format-Number ($Cx + $k)), (Format-Number $y3), (Format-Number $x0), (Format-Number ($Cy - $k)), (Format-Number $x0), (Format-Number $y0)
    ) -join ' '
}

function Get-RectPathData {
    param(
        [double]$X,
        [double]$Y,
        [double]$Width,
        [double]$Height,
        [double]$Rx = 0,
        [double]$Ry = 0
    )

    if ($Rx -le 0 -and $Ry -le 0) {
        return "M {0} {1} H {2} V {3} H {0} Z" -f (Format-Number $X), (Format-Number $Y), (Format-Number ($X + $Width)), (Format-Number ($Y + $Height))
    }

    $rx = [Math]::Min($Rx, $Width / 2)
    $ry = [Math]::Min($Ry, $Height / 2)
    $x2 = $X + $Width
    $y2 = $Y + $Height

    return @(
        "M {0} {1}" -f (Format-Number ($X + $rx)), (Format-Number $Y)
        "H {0}" -f (Format-Number ($x2 - $rx))
        "A {0} {1} 0 0 1 {2} {3}" -f (Format-Number $rx), (Format-Number $ry), (Format-Number $x2), (Format-Number ($Y + $ry))
        "V {0}" -f (Format-Number ($y2 - $ry))
        "A {0} {1} 0 0 1 {2} {3}" -f (Format-Number $rx), (Format-Number $ry), (Format-Number ($x2 - $rx)), (Format-Number $y2)
        "H {0}" -f (Format-Number ($X + $rx))
        "A {0} {1} 0 0 1 {2} {3}" -f (Format-Number $rx), (Format-Number $ry), (Format-Number $X), (Format-Number ($y2 - $ry))
        "V {0}" -f (Format-Number ($Y + $ry))
        "A {0} {1} 0 0 1 {2} {3}" -f (Format-Number $rx), (Format-Number $ry), (Format-Number ($X + $rx)), (Format-Number $Y)
        "Z"
    ) -join ' '
}

function Get-PathDataFromPoints {
    param(
        [string]$Points,
        [switch]$Closed
    )

    $chunks = ($Points -split '[,\s]+' | Where-Object { $_ })
    if ($chunks.Count -lt 2) {
        return $null
    }

    $pairs = @()
    for ($index = 0; $index -lt $chunks.Count; $index += 2) {
        if ($index + 1 -ge $chunks.Count) {
            break
        }
        $pairs += "{0} {1}" -f (Format-Number ([double]$chunks[$index])), (Format-Number ([double]$chunks[$index + 1]))
    }

    if ($pairs.Count -eq 0) {
        return $null
    }

    $path = "M {0}" -f $pairs[0]
    if ($pairs.Count -gt 1) {
        $path += " L {0}" -f ($pairs[1..($pairs.Count - 1)] -join ' ')
    }

    if ($Closed) {
        $path += " Z"
    }

    return $path
}

function Convert-SvgElementToPathData {
    param([System.Xml.XmlElement]$Element)

    switch ($Element.LocalName) {
        'path' { return $Element.GetAttribute('d') }
        'circle' {
            return Get-CirclePathData -Cx ([double]$Element.GetAttribute('cx')) -Cy ([double]$Element.GetAttribute('cy')) -R ([double]$Element.GetAttribute('r'))
        }
        'line' {
            return "M {0} {1} L {2} {3}" -f (Format-Number ([double]$Element.GetAttribute('x1'))), (Format-Number ([double]$Element.GetAttribute('y1'))), (Format-Number ([double]$Element.GetAttribute('x2'))), (Format-Number ([double]$Element.GetAttribute('y2')))
        }
        'polyline' {
            return Get-PathDataFromPoints -Points $Element.GetAttribute('points')
        }
        'polygon' {
            return Get-PathDataFromPoints -Points $Element.GetAttribute('points') -Closed
        }
        'rect' {
            return Get-RectPathData -X ([double]$Element.GetAttribute('x')) -Y ([double]$Element.GetAttribute('y')) -Width ([double]$Element.GetAttribute('width')) -Height ([double]$Element.GetAttribute('height')) -Rx ([double]$Element.GetAttribute('rx')) -Ry ([double]$Element.GetAttribute('ry'))
        }
        default { return $null }
    }
}

function Get-AttributeOrDefault {
    param(
        [System.Xml.XmlElement]$Element,
        [string]$Name,
        [string]$DefaultValue
    )

    $value = $Element.GetAttribute($Name)
    if ([string]::IsNullOrWhiteSpace($value)) {
        return $DefaultValue
    }
    return $value
}

$version = (Get-Content $VersionFile -Raw).Trim()
if ([string]::IsNullOrWhiteSpace($version)) {
    throw "Lucide version file is empty: $VersionFile"
}

$icons = Get-Content $IconsFile | ForEach-Object { $_.Trim() } | Where-Object { $_ -and -not $_.StartsWith('#') }
if (-not $icons) {
    throw "No icon names found in $IconsFile"
}

New-Item -ItemType Directory -Force -Path $CacheDir | Out-Null
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$packageDir = Join-Path $CacheDir "lucide-static-$version"
$tarballPath = Join-Path $CacheDir "lucide-static-$version.tgz"

if (-not (Test-Path $tarballPath)) {
    Push-Location $CacheDir
    try {
        $downloaded = npm pack "lucide-static@$version" --silent
        if ([string]::IsNullOrWhiteSpace($downloaded)) {
            throw "npm pack did not return a tarball path."
        }
        $actualPath = Join-Path (Get-Location) ($downloaded.Trim())
        if ($actualPath -ne $tarballPath) {
            if (Test-Path $tarballPath) {
                Remove-Item $tarballPath -Force
            }
            Move-Item -LiteralPath $actualPath -Destination $tarballPath
        }
    }
    finally {
        Pop-Location
    }
}

if (Test-Path $packageDir) {
    Remove-Item $packageDir -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $packageDir | Out-Null
tar -xf $tarballPath -C $packageDir

foreach ($icon in $icons) {
    $svgPath = Join-Path $packageDir "package/icons/$icon.svg"
    if (-not (Test-Path $svgPath)) {
        throw "Missing SVG for icon '$icon' in $tarballPath"
    }

    [xml]$svg = Get-Content $svgPath -Raw
    $root = $svg.DocumentElement
    $viewBox = ($root.GetAttribute('viewBox') -split '\s+') | Where-Object { $_ }
    if ($viewBox.Count -lt 4) {
        throw "Unexpected SVG viewBox in $svgPath"
    }

    $viewportWidth = Format-Number ([double]$viewBox[2])
    $viewportHeight = Format-Number ([double]$viewBox[3])
    $defaultStrokeWidth = Get-AttributeOrDefault -Element $root -Name 'stroke-width' -DefaultValue '2'
    $defaultStrokeLineCap = Get-AttributeOrDefault -Element $root -Name 'stroke-linecap' -DefaultValue 'round'
    $defaultStrokeLineJoin = Get-AttributeOrDefault -Element $root -Name 'stroke-linejoin' -DefaultValue 'round'

    $paths = New-Object System.Collections.Generic.List[string]
    foreach ($node in $svg.SelectNodes('//*[local-name()="path" or local-name()="circle" or local-name()="line" or local-name()="polyline" or local-name()="polygon" or local-name()="rect"]')) {
        $pathData = Convert-SvgElementToPathData -Element $node
        if ([string]::IsNullOrWhiteSpace($pathData)) {
            continue
        }

        $strokeWidth = Get-AttributeOrDefault -Element $node -Name 'stroke-width' -DefaultValue $defaultStrokeWidth
        $strokeLineCap = Get-AttributeOrDefault -Element $node -Name 'stroke-linecap' -DefaultValue $defaultStrokeLineCap
        $strokeLineJoin = Get-AttributeOrDefault -Element $node -Name 'stroke-linejoin' -DefaultValue $defaultStrokeLineJoin

        $paths.Add(@"
    <path
        android:fillColor="#00000000"
        android:strokeColor="#FF000000"
        android:strokeWidth="$strokeWidth"
        android:strokeLineCap="$strokeLineCap"
        android:strokeLineJoin="$strokeLineJoin"
        android:pathData="$pathData" />
"@.TrimEnd())
    }

    $resourceName = ConvertTo-ResourceName $icon
    $content = @"
<?xml version="1.0" encoding="utf-8"?>
<!-- MACHINE-GENERATED from lucide-static v$version. Do not edit by hand. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="$viewportWidth"
    android:viewportHeight="$viewportHeight">
$($paths -join "`n")
</vector>
"@
    $content = ($content.TrimEnd() -replace "`r`n", "`n") + "`n"
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText((Join-Path $OutputDir "$resourceName.xml"), $content, $utf8NoBom)
}

Remove-Item $packageDir -Recurse -Force
