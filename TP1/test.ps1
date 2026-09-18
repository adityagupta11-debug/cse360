$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'build.ps1')
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force -Path build/test-classes,build/reports | Out-Null
    Get-ChildItem tests -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' } | Set-Content -Encoding utf8 build/tests.args
    & javac -encoding UTF-8 --release 22 --module-path lib --add-modules javafx.controls -cp 'build/classes;lib/h2.jar' -d build/test-classes '@build/tests.args'
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
    & java --module-path lib --add-modules javafx.controls -cp 'build/test-classes;build/classes;lib/h2.jar' database.DeletionTests | Tee-Object build/reports/database-tests.txt
    if ($LASTEXITCODE -ne 0) { throw 'Database tests failed.' }
    & java --module-path lib --add-modules javafx.controls -cp 'build/test-classes;build/classes;lib/h2.jar' guiDeleteUser.DeletionViewTests | Tee-Object build/reports/view-tests.txt
    if ($LASTEXITCODE -ne 0) { throw 'View tests failed.' }
    & java --module-path lib --add-modules javafx.controls -cp 'build/test-classes;build/classes;lib/h2.jar' guiUserLogin.LoginInputTests | Tee-Object build/reports/invitation-tests.txt
    if ($LASTEXITCODE -ne 0) { throw 'Invitation tests failed.' }
    & java --module-path lib --add-modules javafx.controls -cp 'build/classes;lib/h2.jar' testingAutomation.TP1RegressionSuite | Tee-Object build/reports/team-regression.txt
    if ($LASTEXITCODE -ne 0) { throw 'Team regression suite failed.' }

} finally { Pop-Location }
