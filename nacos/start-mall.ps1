# 本地启动商城微服务集群（各服务独立进程，需先完成 Nacos 配置导入与 mvn install）
param(
    [switch]$ImportConfig,
    [switch]$SkipStatic,
    [switch]$DryRun
)

$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

if (-not $env:JWT_SECRET -or $env:JWT_SECRET.Length -lt 32) {
    Write-Warning "JWT_SECRET 未设置或不足 32 字符。示例:"
    Write-Host '  $env:JWT_SECRET = "dev-local-jwt-secret-must-be-at-least-32-chars"'
}
if (-not $env:INTERNAL_SERVICE_TOKEN) {
    Write-Warning "建议设置 INTERNAL_SERVICE_TOKEN，示例:"
    Write-Host '  $env:INTERNAL_SERVICE_TOKEN = "dev-internal-token"'
}

if ($ImportConfig) {
    Write-Host "Publishing mall Nacos configs..."
    & (Join-Path $PSScriptRoot "import-mall-config.ps1")
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

# 启动顺序：下游 Feign 依赖尽量先起；gateway 需单独启动
$Services = @(
    @{ Module = "starpivot-mall/starpivot-mall-member";    Port = 9206; Name = "member" }
    @{ Module = "starpivot-mall/starpivot-mall-product";   Port = 9207; Name = "product" }
    @{ Module = "starpivot-mall/starpivot-mall-ware";      Port = 9208; Name = "ware" }
    @{ Module = "starpivot-mall/starpivot-mall-promotion"; Port = 9212; Name = "promotion" }
    @{ Module = "starpivot-mall/starpivot-mall-order";     Port = 9209; Name = "order" }
)
if (-not $SkipStatic) {
    $Services += @{ Module = "starpivot-mall/starpivot-mall-app"; Port = 9205; Name = "static-bff" }
}

Write-Host ""
Write-Host "商城微服务启动（工作目录: $RepoRoot）"
Write-Host "网关 starpivot-gateway (8080) 请单独启动: mvn spring-boot:run -pl starpivot-gateway"
Write-Host ""

foreach ($svc in $Services) {
    $cmd = "cd `"$RepoRoot`"; `$env:SERVER_PORT=$($svc.Port); mvn spring-boot:run -pl $($svc.Module) -am"
    if ($DryRun) {
        Write-Host "[dry-run] $($svc.Name) :$($svc.Port)"
        Write-Host "  $cmd"
        continue
    }
    Write-Host "Starting $($svc.Name) on port $($svc.Port)..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
    Start-Sleep -Seconds 3
}

if (-not $DryRun) {
    Write-Host ""
    Write-Host "已在独立 PowerShell 窗口中启动各服务。"
    Write-Host "验证: http://localhost:8080/api/v1/portal/home/index (需 gateway + promotion + product)"
}
