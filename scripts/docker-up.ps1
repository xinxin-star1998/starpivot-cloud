param(
    [switch]$Down
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$ComposeFiles = @("-f", "docker-compose.yml")

function Invoke-Compose {
    param([string[]]$ComposeArgs)
    & docker compose @ComposeFiles @ComposeArgs
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if ($Down) {
    Write-Host "Stopping StarPivot infrastructure..."
    Invoke-Compose -ComposeArgs @("down")
    exit 0
}

Write-Host "Starting infrastructure (Nacos, MySQL, Redis, RabbitMQ, Zipkin)..."
Invoke-Compose -ComposeArgs @("up", "-d")
Write-Host ""
Write-Host "Next: import Nacos config -> .\nacos\import-config.ps1"
Write-Host "Then: mvn spring-boot:run -pl <module>  (local dev)"
Write-Host ""
Write-Host "Done. Check status: docker compose ps"
