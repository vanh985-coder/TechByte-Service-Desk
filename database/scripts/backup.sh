#!/usr/bin/env bash
# Usage: bash database/scripts/backup.sh [backup-directory]
set -euo pipefail
umask 077
container="${DB_CONTAINER:-database}"
backup_dir="${1:-./database/backups}"
[[ "$(docker inspect --format '{{.State.Running}}' "$container")" == true ]]
mkdir -p "$backup_dir"
backup="$backup_dir/techbyte_backup_$(date +'%Y%m%d_%H%M%S')_$$.dump"

# No TTY: pg_dump emits a binary archive. Read actual container DB settings.
docker exec --env "BACKUP_USER=${POSTGRES_USER:-}" --env "BACKUP_DB=${POSTGRES_DB:-}" \
  "$container" sh -c '
    user=${BACKUP_USER:-${POSTGRES_USER:-postgres}}
    db=${BACKUP_DB:-${POSTGRES_DB:-$user}}
    exec pg_dump -U "$user" -d "$db" --format=custom --blobs
  ' > "$backup" 2> "$backup.log"
[[ -s "$backup" ]]
docker exec -i "$container" pg_restore --list < "$backup" > /dev/null
echo "Backup archive validated: $backup"
