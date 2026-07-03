# 将单个配置文件发布到 Nacos 配置中心
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$FilePath,

    [string]$DataId = "",
    [string]$NacosServer = "",
    [string]$Group = "",
    [string]$Namespace = "",
    [string]$Username = "",
    [string]$Password = "",
    [string]$Type = "yaml"
)

if (-not $NacosServer) { $NacosServer = if ($env:NACOS_SERVER) { $env:NACOS_SERVER } else { "127.0.0.1:8848" } }
if (-not $Group)       { $Group       = if ($env:NACOS_GROUP) { $env:NACOS_GROUP } else { "DEFAULT_GROUP" } }
if (-not $Namespace)   { $Namespace   = if ($env:NACOS_NAMESPACE) { $env:NACOS_NAMESPACE } else { "" } }
if (-not $Username)    { $Username    = if ($env:NACOS_USERNAME) { $env:NACOS_USERNAME } else { "nacos" } }
if (-not $Password)    { $Password    = if ($env:NACOS_PASSWORD) { $env:NACOS_PASSWORD } else { "nacos" } }

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

$BaseUrl = "http://${NacosServer}/nacos/v1/cs/configs"
$nsLabel = if ($Namespace) { $Namespace } else { "public" }

Write-Host "Publishing $DataId to Nacos $NacosServer (group=$Group, namespace=$nsLabel)"

$pair = "${Username}:${Password}"
$bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
$base64 = [System.Convert]::ToBase64String($bytes)
$headers = @{ Authorization = "Basic $base64" }

$content = Get-Content $resolvedPath -Raw -Encoding UTF8
$body = @{
    dataId  = $DataId
    group   = $Group
    tenant  = $Namespace
    type    = $Type
    content = $content
}

try {
    $response = Invoke-RestMethod -Uri $BaseUrl -Method Post -Headers $headers -Body $body
    if ($response -eq "true") {
        Write-Host "OK"
        exit 0
    } else {
        Write-Host "FAILED: $response"
        exit 1
    }
} catch {
    Write-Host "ERROR: $_"
    exit 1
}
