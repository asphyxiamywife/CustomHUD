$ErrorActionPreference = 'Stop'
$version = '26.2.4'
$archive = Join-Path $env:RUNNER_TEMP 'mesa.7z'
$destination = Join-Path $env:RUNNER_TEMP 'mesa'
Invoke-WebRequest "https://github.com/pal1000/mesa-dist-win/releases/download/$version/mesa3d-$version-release-msvc.7z" -OutFile $archive
$expected = '351fc8c8b695878ffb3eaa044b3ead08672a48b1a045e3c3e3975811df0f6695'
if ((Get-FileHash $archive -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expected) {
    throw 'Mesa archive checksum mismatch'
}
& 7z x $archive "-o$destination" -y
if ($LASTEXITCODE -ne 0) { throw 'Failed to extract Mesa' }
$dlls = Join-Path $destination 'x64'
if (!(Test-Path (Join-Path $dlls 'opengl32.dll'))) { throw 'Mesa OpenGL library missing' }
Copy-Item (Join-Path $dlls '*.dll') (Join-Path $env:JAVA_HOME 'bin') -Force
