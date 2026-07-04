# 导入商城五域业务库（首次或重建后执行）
# 用法：.\sql\import_mall_databases.ps1
# 可选：$env:MYSQL_HOST='127.0.0.1'; $env:MYSQL_PORT='3307'; $env:MYSQL_PASSWORD='root'

param(
    [string]$Host = $(if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { '127.0.0.1' }),
    [string]$Port = $(if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { '3307' }),
    [string]$User = $(if ($env:MYSQL_USER) { $env:MYSQL_USER } else { 'root' }),
    [string]$Password = $(if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { 'root' })
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot

$imports = @(
    @{ Db = 'star_pivot_product';  File = 'star_pivot_product.sql' },
    @{ Db = 'star_pivot_ware';     File = 'star_pivot_ware.sql' },
    @{ Db = 'star_pivot_order';    File = 'star_pivot_order.sql' },
    @{ Db = 'star_pivot_member';   File = 'star_pivot_member.sql' },
    @{ Db = 'star_pivot_promotion'; File = 'star_pivot_promotion.sql' }
)

foreach ($item in $imports) {
    $path = Join-Path $Root "sql\$($item.File)"
    if (-not (Test-Path $path)) {
        throw "SQL file not found: $path"
    }
    Write-Host "Importing $($item.Db) <= $($item.File) ..."
    Get-Content -Raw -Encoding UTF8 $path | mysql -h $Host -P $Port -u $User "-p$Password" $item.Db
    if ($LASTEXITCODE -ne 0) {
        throw "mysql import failed for $($item.Db)"
    }
}

Write-Host 'Done. Mall databases imported.'
