$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'build.ps1')
Push-Location $PSScriptRoot
try {
    # Vishwam, use the host operating system's classpath separator so this shared script works on
    # both the team's Windows machines and this local macOS integration copy.
    $applicationClassPath = @('build/classes', 'lib/h2.jar') -join [IO.Path]::PathSeparator
    $testClassPath = @('build/test-classes', 'build/classes', 'lib/h2.jar') -join [IO.Path]::PathSeparator
    New-Item -ItemType Directory -Force -Path build/test-classes,build/reports | Out-Null
    Get-ChildItem tests -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' } | Set-Content -Encoding utf8 build/tests.args
    & javac -encoding UTF-8 --release 21 --module-path lib --add-modules javafx.controls -cp $applicationClassPath -d build/test-classes '@build/tests.args'
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
    & java --module-path lib --add-modules javafx.controls -cp $testClassPath database.DeletionTests | Tee-Object build/reports/database-tests.txt
    if ($LASTEXITCODE -ne 0) { throw 'Database tests failed.' }
    & java --module-path lib --add-modules javafx.controls -cp $testClassPath guiDeleteUser.DeletionViewTests | Tee-Object build/reports/view-tests.txt
    if ($LASTEXITCODE -ne 0) { throw 'View tests failed.' }
    & java --module-path lib --add-modules javafx.controls -cp $testClassPath guiUserLogin.LoginInputTests | Tee-Object build/reports/invitation-tests.txt
    if ($LASTEXITCODE -ne 0) { throw 'Invitation tests failed.' }
    # Vishwam, exercise the live JavaFX password-state transitions fixed in this integration.
    & java --enable-native-access=javafx.graphics --module-path lib --add-modules javafx.controls -cp $testClassPath passwordPopUpWindow.DynamicPasswordViewTests | Tee-Object build/reports/password-view-tests.txt
    if ($LASTEXITCODE -ne 0) { throw 'Dynamic password view tests failed.' }
    # Vishwam, verify first-user account completion returns to login and clears authorization.
    & java --enable-native-access=javafx.graphics --module-path lib --add-modules javafx.controls -cp $testClassPath guiUserUpdate.FirstUserReloginTests | Tee-Object build/reports/first-user-relogin-tests.txt
    if ($LASTEXITCODE -ne 0) { throw 'First-user relogin tests failed.' }
    & java --module-path lib --add-modules javafx.controls -cp $applicationClassPath testingAutomation.TP1RegressionSuite | Tee-Object build/reports/team-regression.txt
    if ($LASTEXITCODE -ne 0) { throw 'Team regression suite failed.' }

} finally { Pop-Location }
