#!/usr/bin/env bash
# Usage: bash database/scripts/restore.sh backup.dump explicit-lab-container
# --clean replaces objects in the explicitly selected target database.
set -euo pipefail
if [[ "$#" -ne 2 ]]; then
  echo "Usage: $0 backup.dump explicit-lab-container" >&2
  exit 1
fi
backup="$1"
container="$2"
[[ -f "$backup" ]]
[[ "$(docker inspect --format '{{.State.Running}}' "$container")" == true ]]

# Never swallow a failed restore and report success.
docker exec -i --env "RESTORE_USER=${POSTGRES_USER:-}" --env "RESTORE_DB=${POSTGRES_DB:-}" \
  "$container" sh -c '
    user=${RESTORE_USER:-${POSTGRES_USER:-postgres}}
    db=${RESTORE_DB:-${POSTGRES_DB:-$user}}
    exec pg_restore -U "$user" -d "$db" --clean --if-exists \
      --no-owner --no-privileges --exit-on-error --single-transaction
  ' < "$backup"
echo "Restore completed successfully in explicitly selected container: $container"
