param(
    [string]$SourceDir = ".\imagenes_descargadas",
    [string]$OutputDir = ".\imagenes_512x512",
    [int]$Size = 512
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

function Get-OutputName {
    param(
        [string]$FileName
    )

    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($FileName).ToLowerInvariant()
    $normalized = $baseName -replace "[^a-z0-9]+", "_"
    $normalized = $normalized.Trim("_")

    if ([string]::IsNullOrWhiteSpace($normalized)) {
        $normalized = "imagen"
    }

    return "$normalized.png"
}

if (-not (Test-Path -LiteralPath $SourceDir)) {
    throw "La carpeta de origen no existe: $SourceDir"
}

if (-not (Test-Path -LiteralPath $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

$supportedExtensions = @(".jpg", ".jpeg", ".png", ".webp", ".bmp")
$files = Get-ChildItem -LiteralPath $SourceDir -File | Where-Object {
    $supportedExtensions -contains $_.Extension.ToLowerInvariant()
}

if ($files.Count -eq 0) {
    Write-Host "No se encontraron imagenes en $SourceDir"
    exit 0
}

foreach ($file in $files) {
    $image = $null
    $canvas = $null
    $graphics = $null

    try {
        $image = [System.Drawing.Image]::FromFile($file.FullName)

        $scale = [Math]::Max($Size / $image.Width, $Size / $image.Height)
        $scaledWidth = [int][Math]::Ceiling($image.Width * $scale)
        $scaledHeight = [int][Math]::Ceiling($image.Height * $scale)
        $offsetX = [int](($Size - $scaledWidth) / 2)
        $offsetY = [int](($Size - $scaledHeight) / 2)

        $canvas = New-Object System.Drawing.Bitmap($Size, $Size)
        $graphics = [System.Drawing.Graphics]::FromImage($canvas)
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        $graphics.DrawImage($image, $offsetX, $offsetY, $scaledWidth, $scaledHeight)

        $outputName = Get-OutputName -FileName $file.Name
        $outputPath = Join-Path $OutputDir $outputName
        $canvas.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)

        Write-Host "OK -> $($file.Name) => $outputName"
    }
    finally {
        if ($graphics) { $graphics.Dispose() }
        if ($canvas) { $canvas.Dispose() }
        if ($image) { $image.Dispose() }
    }
}

Write-Host "Proceso completado. Imagenes guardadas en: $OutputDir"
