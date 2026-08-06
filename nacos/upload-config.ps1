# 将单个配置文件发布到 Nacos 配置中心
#
# Nacos 3.x 说明:
#   - 认证 API (/nacos/v1/auth/login) 在主端口 8848
#   - 配置管理 API (/v3/console/cs/config) 在 Console 端口 (容器内 8080, 映射为宿主机 18080)
#
# 示例:
#   .\nacos\upload-config.ps1 common-config.yaml
#   .\nacos\upload-config.ps1 starpivot-mall-order.yaml
#   .\nacos\import-config.ps1 -Profile Mall # 批量发布商城相关配置（common + oss + mq + starpivot-mall*）
#
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$FilePath,

    [string]$DataId = "",
    [string]$NacosServer = "",
    [string]$NacosConsole = "",
    [string]$Group = "",
    [string]$Namespace = "",
    [string]$Username = "",
    [string]$Password = "",
    [string]$Type = "yaml"
)

if (-not $NacosServer)  { $NacosServer  = if ($env:NACOS_SERVER)  { $env:NACOS_SERVER }  else { "127.0.0.1:8848" } }
if (-not $NacosConsole) { $NacosConsole = if ($env:NACOS_CONSOLE) { $env:NACOS_CONSOLE } else { "127.0.0.1:18080" } }
if (-not $Group)        { $Group        = if ($env:NACOS_GROUP) { $env:NACOS_GROUP } else { "DEFAULT_GROUP" } }
if (-not $Namespace)    { $Namespace    = if ($env:NACOS_NAMESPACE) { $env:NACOS_NAMESPACE } else { "" } }
if (-not $Username)     { $Username     = if ($env:NACOS_USERNAME) { $env:NACOS_USERNAME } else { "nacos" } }
if (-not $Password)     { $Password     = if ($env:NACOS_PASSWORD) { $env:NACOS_PASSWORD } else { "nacos" } }

$resolvedPath = $FilePath
if (-not (Test-Path -LiteralPath $resolvedPath)) {
    $candidate = Join-Path (Join-Path $PSScriptRoot "config") $FilePath
    if (Test-Path -LiteralPath $candidate) {
        $resolvedPath = $candidate
    } else {
        Write-Error "File not found: $FilePath"
        exit 1
    }
}

$resolvedPath = (Resolve-Path -LiteralPath $resolvedPath).Path
if (-not $DataId) {
    $DataId = [System.IO.Path]::GetFileName($resolvedPath)
}

$nsLabel = if ($Namespace) { $Namespace } else { "public" }

# Nacos 3.x: Auth API 在主端口 8848 的 /nacos/v1/auth/login
function Get-NacosToken {
    $loginBody = "username=$Username&password=$Password"
    try {
        $loginUrl = "http://${NacosServer}/nacos/v1/auth/login"
        $resp = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginBody -ContentType "application/x-www-form-urlencoded"
        if ($resp.accessToken) { return $resp.accessToken }
    } catch {
        Write-Host "WARNING: Auth failed: $_"
    }
    return $null
}

$accessToken = Get-NacosToken
if (-not $accessToken) {
    Write-Error "Failed to obtain Nacos access token"
    exit 1
}

Write-Host "Publishing $DataId to Nacos (server=$NacosServer, console=$NacosConsole, group=$Group, namespace=$nsLabel)"

$content = Get-Content $resolvedPath -Raw -Encoding UTF8

# Nacos 3.x Console API: 配置管理在 Console 端口 (8080), 路径 /v3/console/cs/config
# token 通过 query param accessToken 传递
$apiUrl = "http://${NacosConsole}/v3/console/cs/config?accessToken=$accessToken"
$body = @{
    dataId      = $DataId
    groupName   = $Group
    namespaceId = $Namespace
    type        = $Type
    content     = $content
}

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Body $body -ContentType "application/x-www-form-urlencoded"
    # v3 Console API 返回 { code: 0, message: "success", data: true }
    if ($response.code -eq 0) {
        Write-Host "OK"
        exit 0
    } else {
        Write-Host "FAILED: $($response.message)"
        exit 1
    }
} catch {
    Write-Host "ERROR: $_"
    exit 1
}
