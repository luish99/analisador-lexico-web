$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'JDK não encontrado. Instale o JDK 17 ou superior e adicione javac ao PATH.'
}
New-Item -ItemType Directory -Force 'build' | Out-Null
$sources = @(Get-ChildItem 'src' -Recurse -Filter '*.java' | ForEach-Object FullName)
& javac -encoding UTF-8 -d build @sources
if ($LASTEXITCODE -ne 0) { throw 'Falha na compilação.' }
& java -cp build app.Server
