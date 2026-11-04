# ==============================================================================
# PowerShell Script Restore Database — TechByte Service Desk (Windows)
# ==============================================================================
# Sử dụng: .\database\scripts\restore.ps1 -BackupFile .\database\backups\xxx.dump [-ContainerName database]
# ==============================================================================

param (
    [Parameter(Mandatory=$true)]
    [string]$BackupFile,
    [string]$ContainerName = "database",
    [string]$DbUser = "postgres",
    [string]$DbName = "postgres"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $BackupFile)) {
    Write-Error "❌ Lỗi: File backup '$BackupFile' không tồn tại!"
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [RESTORE] Phục hồi dữ liệu TechByte Service Desk" -ForegroundColor Cyan
Write-Host " File nguồn : $BackupFile"
Write-Host " Container  : $ContainerName"
Write-Host " Database   : $DbName"
Write-Host "==========================================================" -ForegroundColor Cyan

# Đọc file dump và truyền vào stdin của docker exec pg_restore
cmd /c "docker exec -i $ContainerName pg_restore -U $DbUser -d $DbName --clean --if-exists --no-owner --no-privileges -v < `"$BackupFile`""

Write-Host "🔍 Kiểm tra lại số lượng bản ghi sau khi phục hồi:" -ForegroundColor Yellow
docker exec -t $ContainerName psql -U $DbUser -d $DbName -c @"
  SELECT 'users' AS bang, count(*) AS so_luong FROM users
  UNION ALL SELECT 'tickets', count(*) FROM tickets
  UNION ALL SELECT 'ticket_comments', count(*) FROM ticket_comments
  UNION ALL SELECT 'ticket_history', count(*) FROM ticket_history;
"@

Write-Host "✅ [SUCCESS] Phục hồi dữ liệu hoàn tất thành công!" -ForegroundColor Green

