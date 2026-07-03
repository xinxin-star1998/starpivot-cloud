# 将商城微服务相关配置发布到 Nacos（common + mq + starpivot-mall*）
param(
    [string]$NacosServer = "",
    [string]$Group = "",
    [string]$Namespace = "",
    [string]$Username = "",
    [string]$Password = ""
)

if (-not $NacosServer) { $NacosServer = if ($env:NACOS_SERVER) { $env:NACOS_SERVER } else { "127.0.0.1:8848" } }
if (-not $Group)       { $Group       = if ($env:NACOS_GROUP) { $env:NACOS_GROUP } else { "DEFAULT_GROUP" } }
if (-not $Namespace)   { $Namespace   = if ($env:NACOS_NAMESPACE) { $env:NACOS_NAMESPACE } else { "" } }
if (-not $Username)    { $Username    = if ($env:NACOS_USERNAME) { $env:NACOS_USERNAME } else { "nacos" } }
if (-not $Password)    { $Password    = if ($env:NACOS_PASSWORD) { $env:NACOS_PASSWORD } else { "nacos" } }

$UploadScript = Join-Path $PSScriptRoot "upload-config.ps1"
if (-not (Test-Path -LiteralPath $UploadScript)) {
    Write-Error "upload-config.ps1 not found: $UploadScript"
    exit 1
}

# 按依赖顺序发布（common 必须先于各服务 yaml）
$MallConfigs = @(
    "common-config.yaml",
    "mq-config.yaml",
    "starpivot-mall.yaml",
    "starpivot-mall-member.yaml",
    "starpivot-mall-product.yaml",
    "starpivot-mall-ware.yaml",
    "starpivot-mall-order.yaml",
    "starpivot-mall-promotion.yaml"
)

Write-Host "Importing mall configs to Nacos $NacosServer (group=$Group)"
$failed = 0

foreach ($dataId in $MallConfigs) {
    $file = Join-Path (Join-Path $PSScriptRoot "config") $dataId
    if (-not (Test-Path -LiteralPath $file)) {
        Write-Host "  SKIP (missing): $dataId"
        continue
    }
    & $UploadScript -FilePath $file -DataId $dataId `
        -NacosServer $NacosServer -Group $Group -Namespace $Namespace `
        -Username $Username -Password $Password
    if ($LASTEXITCODE -ne 0) { $failed++ }
}

if ($failed -gt 0) {
    Write-Host "Finished with $failed failure(s)."
    exit 1
}
Write-Host "All mall configs published."
exit 0
