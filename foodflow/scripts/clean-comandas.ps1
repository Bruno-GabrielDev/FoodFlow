<#
.SYNOPSIS
    Limpa as comandas (pedidos) acumuladas pelos testes de UI e libera as mesas.

.DESCRIPTION
    Apaga todos os registros das tabelas de comandas (order_item_addons, order_items, orders)
    e marca todas as mesas como AVAILABLE, sem tocar em usuarios, cardapio ou adicionais.

    Rode UMA VEZ antes de iniciar a suite de testes de UI, ja que o banco se encontra cheio
    de comandas criadas em execucoes anteriores.

.EXAMPLE
    ./clean-comandas.ps1

.NOTES
    Requer o container do banco (docker-compose) em execucao.
#>

[CmdletBinding()]
param(
    [string]$Container = "postgres-db",
    [string]$Database  = "foodflow",
    [string]$User      = "postgres"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlFile   = Join-Path $scriptDir "clean-comandas.sql"

if (-not (Test-Path $sqlFile)) {
    throw "Arquivo SQL nao encontrado: $sqlFile"
}

# Verifica se o container do banco esta rodando.
$running = docker ps --filter "name=$Container" --format "{{.Names}}"
if ($running -notcontains $Container) {
    throw "Container '$Container' nao esta rodando. Suba o banco com 'docker compose up -d db' antes."
}

Write-Host "--- Limpando comandas no banco '$Database' (container '$Container') ---" -ForegroundColor Cyan

# Envia o SQL para o psql dentro do container via stdin.
Get-Content -Raw $sqlFile | docker exec -i $Container psql -U $User -d $Database -v ON_ERROR_STOP=1

if ($LASTEXITCODE -ne 0) {
    throw "Falha ao executar a limpeza das comandas (exit code $LASTEXITCODE)."
}

Write-Host "--- Comandas limpas e mesas liberadas com sucesso ---" -ForegroundColor Green
