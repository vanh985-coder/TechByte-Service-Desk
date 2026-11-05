param (
    [ValidatePattern('^[a-zA-Z0-9][a-zA-Z0-9_.-]*$')]
    [string]$ContainerName = 'database',
    [string]$DbUser = $env:POSTGRES_USER,
    [string]$DbName = $env:POSTGRES_DB,
    [string]$BackupDir = '.\database\backups'
)
$ErrorActionPreference = 'Stop'
$running = docker inspect --format '{{.State.Running}}' $ContainerName
if ($LASTEXITCODE -ne 0 -or $running -ne 'true') { throw 'Database container is not running.' }
if (-not $DbUser) {
    $value = docker exec $ContainerName printenv POSTGRES_USER
    if ($LASTEXITCODE -gt 1) { throw 'Cannot read container database settings.' }
    $DbUser = ($value -join '').Trim()
    if (-not $DbUser) { $DbUser = 'postgres' }
}
if (-not $DbName) {
    $value = docker exec $ContainerName printenv POSTGRES_DB
    if ($LASTEXITCODE -gt 1) { throw 'Cannot read container database settings.' }
    $DbName = ($value -join '').Trim()
    if (-not $DbName) { $DbName = $DbUser }
}
New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
$id = [guid]::NewGuid().ToString('N')
$temporaryFile = "/tmp/techbyte-backup-$id.dump"
$backupFile = Join-Path $BackupDir ("techbyte_backup_" + (Get-Date -Format 'yyyyMMdd_HHmmss') + "_$id.dump")
try {
    # docker cp preserves bytes; Windows PowerShell 5 stdout redirection does not.
    docker exec $ContainerName pg_dump -U $DbUser -d $DbName -Fc -b -f $temporaryFile
    if ($LASTEXITCODE -ne 0) { throw 'pg_dump failed.' }
    docker exec $ContainerName pg_restore --list $temporaryFile | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Backup archive validation failed.' }
    docker cp "${ContainerName}:$temporaryFile" $backupFile
    if ($LASTEXITCODE -ne 0) { throw 'Cannot copy backup from container.' }
    if ((Get-Item -LiteralPath $backupFile).Length -eq 0) { throw 'Backup file is empty.' }
    Write-Output "Backup archive validated: $backupFile"
} finally {
    docker exec $ContainerName rm -f -- $temporaryFile | Out-Null
}
