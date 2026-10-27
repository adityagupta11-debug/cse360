$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME/bin/javadoc.exe")) {
        $docExe = "$env:JAVA_HOME/bin/javadoc.exe"
    } else {
        $javaSettings = & java -XshowSettings:properties -version 2>&1 | Out-String
        $jdkHome = [regex]::Match($javaSettings,'java.home = (.+)').Groups[1].Value.Trim()
        $docExe = Join-Path $jdkHome 'bin/javadoc.exe'
    }
    if (-not (Test-Path -LiteralPath $docExe)) { throw 'Set JAVA_HOME to a JDK with javadoc.' }
    $packages = @('guiContributor','guiLessonSupport','guiLessonCreate','guiLessonDetails','guiLessonEdit','guiLessonDelete','guiExperienceAdd')
    $guiSources = @(foreach ($pkg in $packages) {
        Get-ChildItem -LiteralPath "src/$pkg" -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' }
    })
    [IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'build/gui-docs.args'),$guiSources,(New-Object Text.UTF8Encoding($false)))
    $classPath = @('build/test-classes','build/classes','lib/h2.jar') -join [IO.Path]::PathSeparator
    & $docExe -quiet -Xdoclint:none -private -notimestamp -encoding UTF-8 -charset UTF-8 -d Contributor_Javadocs --module-path lib --add-modules javafx.controls -classpath $classPath '@build/gui-docs.args'
    if ($LASTEXITCODE -ne 0) { throw 'GUI Javadoc failed.' }
    $testSources = @(Get-ChildItem -LiteralPath tests/kanishTests -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' })
    [IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'build/test-docs.args'),$testSources,(New-Object Text.UTF8Encoding($false)))
    & $docExe -quiet -Xdoclint:none -private -notimestamp -encoding UTF-8 -charset UTF-8 -d ContributorTests_Javadocs --module-path lib --add-modules javafx.controls -classpath $classPath '@build/test-docs.args'
    if ($LASTEXITCODE -ne 0) { throw 'Test Javadoc failed.' }
    & java --enable-native-access=javafx.graphics --module-path lib --add-modules javafx.controls -cp $classPath kanishTests.SourceEvidence
    if ($LASTEXITCODE -ne 0) { throw 'Source evidence capture failed.' }
} finally { Pop-Location }
