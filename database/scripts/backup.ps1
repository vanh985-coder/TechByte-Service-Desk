# ==============================================================================
# PowerShell Script Backup Database — TechByte Service Desk (Windows)
# ==============================================================================
# Sử dụng: .\database\scripts\backup.ps1 [-BackupDir .\database\backups]
# ==============================================================================

param (
    [string]$ContainerName = "database",
    [string]$DbUser = "postgres",
    [string]$DbName = "postgres",
    [string]$BackupDir = ".\database\backups"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = Join-Path $BackupDir "techbyte_backup_$timestamp.dump"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [BACKUP] Bắt đầu sao lưu database TechByte Service Desk" -ForegroundColor Cyan
Write-Host " Container : $ContainerName"
Write-Host " Database  : $DbName"
Write-Host " File đích : $backupFile"
Write-Host "==========================================================" -ForegroundColor Cyan

docker exec -t $ContainerName pg_dump -U $DbUser -d $DbName -F c -b -v > $backupFile

if ((Get-Item $backupFile).Length -gt 100) {
    Write-Host "✅ [SUCCESS] Sao lưu thành công!" -ForegroundColor Green
    Write-Host " Dung lượng: $((Get-Item $backupFile).Length) bytes"
    Write-Host " File dump : $backupFile"
} else {
    Write-Error "❌ [FAILED] File sao lưu rỗng hoặc kích thước bất thường!"
}

