param(
    [ValidateSet("infra", "services", "ui", "full")]
    [string]$Mode = "infra",

    [switch]$Build,
    [switch]$Down
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$ComposeBase = @("-f", "docker-compose.yml")
$ComposeServices = @("-f", "docker-compose.yml", "-f", "docker-compose.services.yml")
$ComposeFull = @("-f", "docker-compose.yml", "-f", "docker-compose.services.yml", "-f", "docker-compose.ui.yml")

function Invoke-Compose {
    param([string[]]$Files, [string[]]$ComposeArgs)
    & docker compose @Files @ComposeArgs
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if ($Down) {
    Write-Host "Stopping StarPivot Docker stack..."
    Invoke-Compose -Files $ComposeFull -ComposeArgs @("down")
    exit 0
}

$buildFlag = if ($Build) { @("--build") } else { @() }

switch ($Mode) {
    "infra" {
        Write-Host "Starting infrastructure only (Nacos, MySQL, Redis, RabbitMQ, Zipkin)..."
        Invoke-Compose -Files $ComposeBase -ComposeArgs (@("up", "-d") + $buildFlag)
        Write-Host ""
        Write-Host "Next: import Nacos config -> .\nacos\import-config.ps1"
        Write-Host "Then: mvn spring-boot:run -pl <module>  (local dev)"
    }
    "services" {
        Write-Host "Starting infrastructure + all microservices..."
        Invoke-Compose -Files $ComposeServices -ComposeArgs (@("up", "-d") + $buildFlag)
        Write-Host ""
        Write-Host "API Gateway: http://localhost:8080"
        Write-Host "Nacos:       http://localhost:8848/nacos"
    }
    "ui" {
        Write-Host "Starting infrastructure + microservices + frontend..."
        Invoke-Compose -Files $ComposeFull -ComposeArgs (@("up", "-d") + $buildFlag)
        Write-Host ""
        Write-Host "Frontend:    http://localhost:3000"
        Write-Host "API Gateway: http://localhost:8080"
    }
    "full" {
        Write-Host "Starting full stack..."
        Invoke-Compose -Files $ComposeFull -ComposeArgs @("up", "-d", "--build")
        Write-Host ""
        Write-Host "Frontend:    http://localhost:3000"
        Write-Host "API Gateway: http://localhost:8080"
        Write-Host "Nacos:       http://localhost:8848/nacos"
        Write-Host "Default login: admin / 123456"
    }
}

Write-Host ""
Write-Host "Done. Check status: docker compose -f docker-compose.yml -f docker-compose.services.yml ps"
