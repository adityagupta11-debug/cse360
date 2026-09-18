$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'build.ps1')
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force -Path data | Out-Null
    # Keep the default run's database with this project, not in the user's home folder.
    & java --module-path lib --add-modules javafx.controls '-Dcse360.db.url=jdbc:h2:./data/Team53' -cp 'build/classes;lib/h2.jar' applicationMain.FoundationsMain
    if ($LASTEXITCODE -ne 0) { throw 'Application exited with an error.' }
} finally { Pop-Location }
