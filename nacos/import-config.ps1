# 将 nacos/config 目录下的配置发布到 Nacos 配置中心
param(
    [ValidateSet("All", "Core", "Mall")]
    [string]$Profile = "All",
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

$ConfigDir = Join-Path $PSScriptRoot "config"
$BaseUrl = "http://${NacosServer}/nacos/v1/cs/configs"

function Test-ConfigInProfile {
    param([string]$DataId, [string]$ProfileName)
    switch ($ProfileName) {
        "Mall" {
            return ($DataId -eq "common-config.yaml") -or ($DataId -eq "mq-config.yaml") -or ($DataId -like "starpivot-mall*.yaml")
        }
        "Core" {
            return ($DataId -notlike "starpivot-mall*.yaml")
        }
        default { return $true }
    }
}

Write-Host "Publishing configs to Nacos $NacosServer (profile=$Profile, group=$Group, namespace=$(if ($Namespace) { $Namespace } else { 'public' }))"

$pair = "${Username}:${Password}"
$bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
$base64 = [System.Convert]::ToBase64String($bytes)
$headers = @{ Authorization = "Basic $base64" }

Get-ChildItem "$ConfigDir\*.yaml" | Where-Object { Test-ConfigInProfile -DataId $_.Name -ProfileName $Profile } | ForEach-Object {
    $dataId = $_.Name
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    Write-Host "  -> $dataId"

    $body = @{
        dataId  = $dataId
        group   = $Group
        tenant  = $Namespace
        type    = "yaml"
        content = $content
    }

    try {
        $response = Invoke-RestMethod -Uri $BaseUrl -Method Post -Headers $headers -Body $body
        if ($response -eq "true") {
            Write-Host "     OK"
        } else {
            Write-Host "     FAILED: $response"
        }
    } catch {
        Write-Host "     ERROR: $_"
    }
}

Write-Host "Done."
