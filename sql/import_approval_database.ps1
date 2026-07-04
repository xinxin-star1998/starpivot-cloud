# 导入审批独立业务库（首次或重建后执行）
# 用法：.\sql\import_approval_database.ps1
# 可选：$env:MYSQL_HOST='127.0.0.1'; $env:MYSQL_PORT='3307'; $env:MYSQL_PASSWORD='root'

param(
    [string]$Host = $(if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { '127.0.0.1' }),
    [string]$Port = $(if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { '3307' }),
    [string]$User = $(if ($env:MYSQL_USER) { $env:MYSQL_USER } else { 'root' }),
    [string]$Password = $(if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { 'root' })
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot

Write-Host 'Creating database star_pivot_approval (if not exists) ...'
mysql -h $Host -P $Port -u $User "-p$Password" -e "CREATE DATABASE IF NOT EXISTS star_pivot_approval CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if ($LASTEXITCODE -ne 0) {
    throw 'Failed to create star_pivot_approval database'
}

$path = Join-Path $Root 'sql\star_pivot_approval.sql'
if (-not (Test-Path $path)) {
    throw "SQL file not found: $path"
}

Write-Host 'Importing star_pivot_approval <= star_pivot_approval.sql ...'
Get-Content -Raw -Encoding UTF8 $path | mysql -h $Host -P $Port -u $User "-p$Password" star_pivot_approval
if ($LASTEXITCODE -ne 0) {
    throw 'mysql import failed for star_pivot_approval'
}

Write-Host 'Done. Approval database imported.'
