# 将 nacos/config 目录下的配置发布到 Nacos 配置中心
#
# Nacos 3.x 说明:
#   - 认证 API (/nacos/v1/auth/login) 在主端口 8848
#   - 配置管理 API (/v3/console/cs/config) 在 Console 端口 (容器内 8080, 映射为宿主机 18080)
#
param(
    [ValidateSet("All", "Core", "Mall")]
    [string]$Profile = "All",
    [string]$NacosServer = "",
    [string]$NacosConsole = "",
    [string]$Group = "",
    [string]$Namespace = "",
    [string]$Username = "",
    [string]$Password = ""
)

if (-not $NacosServer)  { $NacosServer  = if ($env:NACOS_SERVER)  { $env:NACOS_SERVER }  else { "127.0.0.1:8848" } }
if (-not $NacosConsole) { $NacosConsole = if ($env:NACOS_CONSOLE) { $env:NACOS_CONSOLE } else { "127.0.0.1:18080" } }
if (-not $Group)        { $Group        = if ($env:NACOS_GROUP) { $env:NACOS_GROUP } else { "DEFAULT_GROUP" } }
if (-not $Namespace)    { $Namespace    = if ($env:NACOS_NAMESPACE) { $env:NACOS_NAMESPACE } else { "" } }
if (-not $Username)     { $Username     = if ($env:NACOS_USERNAME) { $env:NACOS_USERNAME } else { "nacos" } }
if (-not $Password)     { $Password     = if ($env:NACOS_PASSWORD) { $env:NACOS_PASSWORD } else { "nacos" } }

$ConfigDir = Join-Path $PSScriptRoot "config"

function Test-ConfigInProfile {
    param([string]$DataId, [string]$ProfileName)
    switch ($ProfileName) {
        "Mall" {
            return ($DataId -eq "common-config.yaml") -or ($DataId -eq "oss-config.yaml") -or ($DataId -eq "mq-config.yaml") -or ($DataId -like "starpivot-mall*.yaml")
        }
        "Core" {
            return ($DataId -notlike "starpivot-mall*.yaml")
        }
        default { return $true }
    }
}

# Nacos 3.x: Auth API 在主端口 8848 的 /nacos/v1/auth/login
function Get-NacosToken {
    $loginBody = "username=$Username&password=$Password"
    try {
        $loginUrl = "http://${NacosServer}/nacos/v1/auth/login"
        $resp = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginBody -ContentType "application/x-www-form-urlencoded"
        if ($resp.accessToken) { return $resp.accessToken }
    } catch {
        Write-Host "  WARNING: Auth failed: $_"
    }
    return $null
}

$accessToken = Get-NacosToken
if (-not $accessToken) {
    Write-Error "Failed to obtain Nacos access token"
    exit 1
}
$nsLabel = if ($Namespace) { $Namespace } else { "public" }

Write-Host "Publishing configs to Nacos (server=$NacosServer, console=$NacosConsole, profile=$Profile, group=$Group, namespace=$nsLabel)"

# Nacos 3.x Console API: 配置管理在 Console 端口 (8080), 路径 /v3/console/cs/config
# token 通过 query param accessToken 传递
$baseUrl = "http://${NacosConsole}/v3/console/cs/config"

Get-ChildItem "$ConfigDir\*.yaml" | Where-Object { Test-ConfigInProfile -DataId $_.Name -ProfileName $Profile } | ForEach-Object {
    $dataId = $_.Name
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    Write-Host "  -> $dataId"

    $apiUrl = "${baseUrl}?accessToken=$accessToken"
    $body = @{
        dataId      = $dataId
        groupName   = $Group
        namespaceId = $Namespace
        type        = "yaml"
        content     = $content
    }

    try {
        $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Body $body -ContentType "application/x-www-form-urlencoded"
        # v3 Console API 返回 { code: 0, message: "success", data: true }
        if ($response.code -eq 0) {
            Write-Host "     OK"
        } else {
            Write-Host "     FAILED: $($response.message)"
        }
    } catch {
        Write-Host "     ERROR: $_"
    }
}

Write-Host "Done."
