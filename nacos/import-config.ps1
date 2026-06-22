# 将 nacos/config 目录下的配置发布到 Nacos 配置中心
param(
    [string]$NacosServer = "127.0.0.1:8848",
    [string]$Group = "DEFAULT_GROUP",
    [string]$Namespace = "",
    [string]$Username = "nacos",
    [string]$Password = "nacos"
)

$ConfigDir = Join-Path $PSScriptRoot "config"
$BaseUrl = "http://${NacosServer}/nacos/v1/cs/configs"

Write-Host "Publishing configs to Nacos $NacosServer (group=$Group, namespace=$(if ($Namespace) { $Namespace } else { 'public' }))"

$pair = "${Username}:${Password}"
$bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
$base64 = [System.Convert]::ToBase64String($bytes)
$headers = @{ Authorization = "Basic $base64" }

Get-ChildItem "$ConfigDir\*.yaml" | ForEach-Object {
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
