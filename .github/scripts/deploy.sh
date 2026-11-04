#!/usr/bin/env bash
# Executed on the VPS over SSH by .github/workflows/deploy.yml.
set -Eeuo pipefail
umask 077

release_id="${1:?Missing release ID}"
app_url="${2:?Missing HTTPS application URL}"
[[ "$release_id" =~ ^[0-9a-f]{40}-[0-9]+-[0-9]+$ ]]
[[ "$app_url" =~ ^https://[a-zA-Z0-9][a-zA-Z0-9.-]*(:[0-9]+)?$ ]]

root=/opt/techbyte
release_dir="$root/releases/$release_id"
export APP_ENV_FILE="$root/shared/.env"
export TLS_DIR="$root/shared/tls"
# Image values must come from the release manifest, not the login environment.
unset FRONTEND_IMAGE BACKEND_IMAGE RELEASE_SHA

for command in docker curl jq flock tar; do
  command -v "$command" > /dev/null || { echo "Missing VPS command: $command" >&2; exit 1; }
done
docker compose version > /dev/null
[[ -r "$APP_ENV_FILE" && -d "$TLS_DIR" ]]
[[ ! -e "$root/current" || -L "$root/current" ]]
[[ ! -e "$root/previous" || -L "$root/previous" ]]
install -d -m 750 "$root/releases" "$root/backups"

exec 9> "$root/deploy.lock"
flock -w 600 9

previous_dir=''
if [[ -L "$root/current" ]]; then
  previous_dir="$(readlink -f "$root/current")"
  [[ "$previous_dir" =~ ^/opt/techbyte/releases/[0-9a-f]{40}-[0-9]+-[0-9]+$ ]]
  [[ -f "$previous_dir/release.env" ]]
fi
# A retry uses a new run-attempt ID; never overwrite a release directory.
[[ ! -e "$release_dir" ]]
install -d -m 750 "$release_dir"
tar -xzf "$root/incoming/$release_id.tgz" -C "$release_dir"
[[ -f "$release_dir/release.env" ]]
# Keep private runtime configuration on the VPS, alongside the image manifest.
# A rollback must not reuse a newly edited shared env file or certificate pair.
install -m 600 "$APP_ENV_FILE" "$release_dir/runtime.env"
install -d -m 700 "$release_dir/tls"
install -m 600 "$TLS_DIR/origin.crt" "$release_dir/tls/origin.crt"
install -m 600 "$TLS_DIR/origin.key" "$release_dir/tls/origin.key"

compose_at() {
  local directory="$1"
  shift
  local runtime_env="$directory/runtime.env" cert_dir="$directory/tls"
  [[ -r "$runtime_env" && -r "$cert_dir/origin.crt" && -r "$cert_dir/origin.key" ]] || {
    echo 'Release is missing its private runtime configuration; manual recovery is required.' >&2
    return 1
  }
  APP_ENV_FILE="$runtime_env" TLS_DIR="$cert_dir" docker compose --project-name techbyte \
    --env-file "$runtime_env" --env-file "$directory/release.env" \
    -f "$directory/infra/compose/compose.prod.yml" "$@"
}

record() {
  printf '%s release=%s result=%s\n' "$(date -u +'%Y-%m-%dT%H:%M:%SZ')" \
    "$release_id" "$1" >> "$root/releases.log"
}

smoke_test() {
  local readiness directory="${1:-$release_dir}"
  curl --fail --silent --show-error --connect-timeout 10 --max-time 30 \
    --retry 5 --retry-delay 3 --retry-all-errors "$app_url/" -o /dev/null || return 1
  readiness="$(curl --fail --silent --show-error --connect-timeout 10 --max-time 30 \
    --retry 5 --retry-delay 3 --retry-all-errors "$app_url/api/ready")" || return 1
  jq -e '.status == "READY" and .database == "UP"' <<< "$readiness" > /dev/null || return 1
  # Only synthetic lab accounts/data should be used for the optional write test.
  if [[ -r "$root/shared/smoke.env" ]]; then
    (
      set -a
      # This operator-owned file must contain trusted shell variable assignments.
      source "$root/shared/smoke.env"
      set +a
      bash "$directory/.github/scripts/smoke-test.sh" "$app_url"
    ) || return 1
  fi
}

changed=0
recover() {
  local result="${1:-$?}"
  trap - ERR INT TERM
  [[ "$result" -ne 0 ]] || result=1
  record failed
  echo "Release failed; see $root/releases.log and server container logs." >&2
  # Keep diagnostic logs on the VPS; application logs may contain private data.
  compose_at "$release_dir" ps > "$release_dir/container-status.txt" 2>&1 || true
  compose_at "$release_dir" logs --no-color --tail=100 \
    > "$release_dir/failure.log" 2>&1 || true
  if [[ "$changed" == 1 && -n "$previous_dir" ]]; then
    # Automatic rollback is an operator opt-in after reviewing schema compatibility.
    if [[ -f "$root/shared/allow-image-rollback" ]]; then
      echo 'Restoring the previous images and configuration.' >&2
      if compose_at "$previous_dir" up -d --no-build --wait --wait-timeout 180 database backend frontend &&
         compose_at "$previous_dir" up -d --no-deps --no-build --force-recreate \
           --wait --wait-timeout 90 reverse-proxy && smoke_test "$previous_dir"; then
        record rolled-back
      else
        record rollback-failed
        echo 'Rollback failed; operator intervention is required.' >&2
      fi
    else
      record rollback-needs-schema-review
      echo 'Automatic rollback disabled: review migration compatibility first.' >&2
    fi
  elif [[ "$changed" == 1 ]]; then
    echo 'First deployment: there is no previous release to restore.' >&2
  fi
  exit "$result"
}
trap recover ERR
trap 'recover 130' INT
trap 'recover 143' TERM

record started
compose_at "$release_dir" config --quiet
compose_at "$release_dir" pull

# Back up the existing database before any containers or migrations are changed.
database_id="$(docker ps -q \
  --filter label=com.docker.compose.project=techbyte \
  --filter label=com.docker.compose.service=database)"
if [[ -n "$previous_dir" && -z "$database_id" ]]; then
  echo 'Existing release has no running database; repair it before deploying.' >&2
  false
fi
if [[ -n "$database_id" ]]; then
  backup="$root/backups/$release_id.dump"
  docker exec "$database_id" sh -c \
    'exec pg_dump --format=custom --username="$POSTGRES_USER" --dbname="$POSTGRES_DB"' \
    > "$backup"
  [[ -s "$backup" ]]
  # Validate the archive table of contents without changing any database.
  docker exec -i "$database_id" pg_restore --list < "$backup" > /dev/null
fi

changed=1
# Flyway is run by Spring Boot at startup; readiness must include the database.
compose_at "$release_dir" up -d --no-build --wait --wait-timeout 180 database backend frontend
# Recreate Nginx so upstream DNS resolves the newly created app containers.
compose_at "$release_dir" up -d --no-deps --no-build --force-recreate \
  --wait --wait-timeout 90 reverse-proxy
compose_at "$release_dir" exec -T reverse-proxy nginx -t
smoke_test

if [[ -n "$previous_dir" ]]; then
  ln -sfn "$previous_dir" "$root/previous"
fi
ln -sfn "$release_dir" "$root/current"
record succeeded
trap - ERR INT TERM
echo "Deployed $release_id successfully."
