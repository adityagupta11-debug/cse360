$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'build.ps1')
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force build/test-classes,build/reports | Out-Null
    $applicationClassPath = @('build/classes','lib/h2.jar') -join [IO.Path]::PathSeparator
    $testClassPath = @('build/test-classes','build/classes','lib/h2.jar') -join [IO.Path]::PathSeparator
    $testFiles = @(Get-ChildItem tests -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' })
    [IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'build/tests.args'), $testFiles, (New-Object Text.UTF8Encoding($false)))
    & javac -encoding UTF-8 --release 21 --module-path lib --add-modules javafx.controls -cp $applicationClassPath -d build/test-classes '@build/tests.args'
    if ($LASTEXITCODE -ne 0) { throw 'Kanish test compilation failed.' }
    foreach ($suite in @('kanishTests.ModelTests','kanishTests.CurrentRepoIntegrationTests','kanishTests.CurrentRepoWindowTests')) {
        & java --enable-native-access=javafx.graphics --module-path lib --add-modules javafx.controls -cp $testClassPath $suite 2>&1 | Tee-Object -FilePath "build/reports/$suite.txt"
        if ($LASTEXITCODE -ne 0) { throw "Failed: $suite" }
    }
} finally { Pop-Location }
