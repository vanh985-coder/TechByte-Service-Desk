param (
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-zA-Z0-9][a-zA-Z0-9_.-]*$')]
    [string]$ContainerName,
    [string]$DbUser = $env:POSTGRES_USER,
    [string]$DbName = $env:POSTGRES_DB
)
# --clean replaces objects in the explicitly selected lab target database.
$ErrorActionPreference = 'Stop'
if (-not (Test-Path -LiteralPath $BackupFile -PathType Leaf)) { throw 'Backup file does not exist.' }
$running = docker inspect --format '{{.State.Running}}' $ContainerName
if ($LASTEXITCODE -ne 0 -or $running -ne 'true') { throw 'Target container is not running.' }
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
$temporaryFile = '/tmp/techbyte-restore-' + [guid]::NewGuid().ToString('N') + '.dump'
try {
    docker cp $BackupFile "${ContainerName}:$temporaryFile"
    if ($LASTEXITCODE -ne 0) { throw 'Cannot transfer backup to target container.' }
    docker exec $ContainerName pg_restore --list $temporaryFile | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Invalid backup archive.' }
    docker exec $ContainerName pg_restore -U $DbUser -d $DbName --clean --if-exists --no-owner --no-privileges --exit-on-error --single-transaction $temporaryFile
    if ($LASTEXITCODE -ne 0) { throw 'Database restore failed.' }
    Write-Output "Restore completed successfully in explicitly selected container: $ContainerName"
} finally {
    docker exec $ContainerName rm -f -- $temporaryFile | Out-Null
}
