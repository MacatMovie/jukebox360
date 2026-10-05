$ErrorActionPreference='Stop'
$Root = Split-Path -Parent $PSScriptRoot
$TargetsDir = Join-Path $Root 'targets'
$GlobalGradleDir = 'C:\Gradle'
function Resolve-JavaHome([int]$Version) {
    $envName="JAVA${Version}_HOME"; $v=[Environment]::GetEnvironmentVariable($envName)
    if ($v -and (Test-Path (Join-Path $v 'bin\java.exe'))) { return $v }
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) { return $env:JAVA_HOME }
    foreach($r in @('C:\Program Files\Java','C:\Program Files\Eclipse Adoptium','C:\Program Files\Microsoft','C:\Program Files\Zulu')) {
      if(Test-Path $r) { foreach($d in Get-ChildItem $r -Directory -ErrorAction SilentlyContinue) { if($d.Name -match "(jdk|temurin|openjdk|zulu).*$Version|^jdk-$Version" -and (Test-Path (Join-Path $d.FullName 'bin\java.exe'))) { return $d.FullName } } }
    }
    throw "Java $Version not found; set $envName."
}
function Get-Gradle([string]$Version) {
    New-Item -ItemType Directory -Force -Path $GlobalGradleDir|Out-Null
    $gradleHome=Join-Path $GlobalGradleDir "gradle-$Version"; $exe=Join-Path $gradleHome 'bin\gradle.bat'
    if(!(Test-Path $exe)) {
        $zip=Join-Path $GlobalGradleDir "gradle-$Version-bin.zip"
        if(!(Test-Path $zip)){
            Write-Host "Downloading Gradle $Version to C:\Gradle..." -ForegroundColor Yellow
            Invoke-WebRequest -UseBasicParsing "https://services.gradle.org/distributions/gradle-$Version-bin.zip" -OutFile $zip
        }
        Expand-Archive $zip $GlobalGradleDir -Force
    }
    return $exe
}
foreach($dir in Get-ChildItem $TargetsDir -Directory | Sort-Object Name) {
    $gv=(Get-Content (Join-Path $dir.FullName 'gradle-version.txt') -Raw).Trim(); $jv=[int](Get-Content (Join-Path $dir.FullName 'java-version.txt') -Raw).Trim()
    $jh=Resolve-JavaHome $jv; $old=$env:JAVA_HOME; try { $env:JAVA_HOME=$jh; $g=Get-Gradle $gv; Push-Location $dir.FullName; try { & $g wrapper --gradle-version $gv --distribution-type bin; if($LASTEXITCODE -ne 0){throw "Wrapper generation failed for $($dir.Name)"} } finally {Pop-Location} } finally {$env:JAVA_HOME=$old}
}
Write-Host 'Official Gradle wrappers generated for all targets. Gradle distributions are stored in C:\Gradle.' -ForegroundColor Green
