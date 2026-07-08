# Arranca el backend evitando el bug de "spring-boot:run" con rutas que contienen
# caracteres no-ASCII (el goal `run` del plugin falla con ClassNotFoundException
# porque pasa el classpath completo, incluida la ruta del proyecto, como argumento
# a un proceso hijo). Empaquetar y ejecutar el jar sí funciona siempre.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "Compilando (sin tests)..." -ForegroundColor Cyan
& .\mvnw.cmd package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "El empaquetado ha fallado." }

$jar = Get-ChildItem "target\*.jar" | Select-Object -First 1
Write-Host "Arrancando $($jar.Name)..." -ForegroundColor Cyan
java -jar $jar.FullName
