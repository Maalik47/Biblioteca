# ==========================================================================
#  iniciar-proyecto.ps1
#  Levanta el proyecto completo: backend Spring Boot (8080) + frontend (5500)
#  Uso:  powershell -ExecutionPolicy Bypass -File .\iniciar-proyecto.ps1
# ==========================================================================

$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path
$puertoApi = 8080
$puertoWeb = 5500

function Test-PuertoOcupado([int]$puerto) {
    return [bool](Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue)
}

Write-Host ''
Write-Host '  ============================================================' -ForegroundColor Cyan
Write-Host '   GESTION DE BIBLIOTECA - Inicio del proyecto' -ForegroundColor Cyan
Write-Host '  ============================================================' -ForegroundColor Cyan
Write-Host ''

# --------------------------------------------------------------------------
# 1. Verificar puertos
# --------------------------------------------------------------------------
if (Test-PuertoOcupado $puertoApi) {
    Write-Host "  [AVISO] El puerto $puertoApi ya esta ocupado." -ForegroundColor Yellow
    $proc = Get-NetTCPConnection -LocalPort $puertoApi -State Listen |
            Select-Object -First 1 -ExpandProperty OwningProcess
    $nombre = (Get-Process -Id $proc -ErrorAction SilentlyContinue).ProcessName
    Write-Host "          Proceso: $nombre (PID $proc)" -ForegroundColor Yellow
    Write-Host '          Si es Apache (httpd), detengalo como Administrador:' -ForegroundColor Yellow
    Write-Host '              Stop-Service PEMHTTPD-x64' -ForegroundColor Yellow
    exit 1
}

# --------------------------------------------------------------------------
# 2. Backend Spring Boot
# --------------------------------------------------------------------------
$jar = Get-ChildItem -Path "$raiz\backend\target" -Filter '*.jar' -ErrorAction SilentlyContinue |
       Select-Object -First 1

if ($jar) {
    Write-Host '  [1/2] Iniciando backend (JAR compilado)...' -ForegroundColor Green
    Start-Process -FilePath 'java' `
        -ArgumentList '-jar', $jar.FullName `
        -WorkingDirectory "$raiz\backend" `
        -RedirectStandardOutput "$raiz\backend.log" `
        -RedirectStandardError "$raiz\backend-error.log" `
        -WindowStyle Hidden
} else {
    Write-Host '  [1/2] Iniciando backend (compilando con Maven)...' -ForegroundColor Green
    Start-Process -FilePath 'mvn' `
        -ArgumentList 'spring-boot:run' `
        -WorkingDirectory "$raiz\backend" `
        -RedirectStandardOutput "$raiz\backend.log" `
        -RedirectStandardError "$raiz\backend-error.log" `
        -WindowStyle Hidden
}

# Esperar a que la API responda
Write-Host '        Esperando a la API en http://localhost:' + $puertoApi + '/api ...' -ForegroundColor Gray
$apiLista = $false
for ($i = 0; $i -lt 40; $i++) {
    Start-Sleep -Seconds 1
    if (Test-PuertoOcupado $puertoApi) { $apiLista = $true; break }
}
if (-not $apiLista) {
    Write-Host '  [ERROR] El backend no levanto. Revise backend.log' -ForegroundColor Red
    exit 1
}
Write-Host '        API lista.' -ForegroundColor Green

# --------------------------------------------------------------------------
# 3. Frontend estatico
# --------------------------------------------------------------------------
Write-Host '  [2/2] Iniciando frontend en http://localhost:' + $puertoWeb + ' ...' -ForegroundColor Green
Start-Process -FilePath 'cmd.exe' `
    -ArgumentList '/c', "npx --yes http-server frontend -p $puertoWeb -c-1 > frontend.log 2>&1" `
    -WorkingDirectory $raiz `
    -WindowStyle Hidden

$webListo = $false
for ($i = 0; $i -lt 20; $i++) {
    Start-Sleep -Seconds 1
    if (Test-PuertoOcupado $puertoWeb) { $webListo = $true; break }
}

Write-Host ''
Write-Host '  ------------------------------------------------------------' -ForegroundColor DarkCyan
if ($webListo) {
    Write-Host '   Proyecto iniciado correctamente' -ForegroundColor Green
} else {
    Write-Host '   Frontend no responde en el puerto esperado' -ForegroundColor Yellow
}
Write-Host '   Frontend : http://localhost:5500/index.html' -ForegroundColor White
Write-Host '   API      : http://localhost:8080/api' -ForegroundColor White
Write-Host '   H2 Console: http://localhost:8080/h2-console' -ForegroundColor White
Write-Host ''
Write-Host '   Credenciales locales (solo la primera vez):' -ForegroundColor White
Write-Host '     admin / admin123      (rol ADMIN)' -ForegroundColor Gray
Write-Host '   (En la nube se configuran con APP_ADMIN_USERNAME/APP_ADMIN_PASSWORD)' -ForegroundColor DarkGray
Write-Host '  ------------------------------------------------------------' -ForegroundColor DarkCyan
Write-Host ''
