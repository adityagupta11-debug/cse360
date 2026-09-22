$ErrorActionPreference = 'Stop'
foreach ($jar in @('javafx-base.jar','javafx-graphics.jar','javafx-controls.jar','h2.jar')) {
    if (-not (Test-Path (Join-Path $PSScriptRoot "lib/$jar"))) {
        throw 'Missing dependencies. Run python setup_dependencies.py from TP1, then retry.'
    }
}
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force -Path build/classes | Out-Null
    $sourceFiles = Get-ChildItem src -Recurse -Filter *.java
    $sourceFiles | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' } | Set-Content -Encoding utf8 build/sources.args
    & javac -encoding UTF-8 --release 21 --module-path lib -d build/classes '@build/sources.args'
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
    Get-ChildItem src -Recurse -Filter *.css | ForEach-Object {
        $relative = $_.FullName.Substring((Join-Path $PSScriptRoot 'src').Length + 1)
        $destination = Join-Path 'build/classes' $relative
        New-Item -ItemType Directory -Force -Path (Split-Path $destination) | Out-Null
        Copy-Item -LiteralPath $_.FullName -Destination $destination
    }
    Write-Output 'Application compiled successfully.'
} finally { Pop-Location }
