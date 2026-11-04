#!/usr/bin/env bash
# API smoke test against disposable CI data or an explicitly provisioned lab account.
set -Eeuo pipefail
base_url="${1:?Supply the application base URL}"
base_url="${base_url%/}"
: "${SMOKE_EMAIL:?Set SMOKE_EMAIL to a lab ADMIN account}"
: "${SMOKE_PASSWORD:?Set SMOKE_PASSWORD}"
command -v curl > /dev/null
command -v jq > /dev/null

# Keep passwords and tokens out of shell traces and command arguments.
set +x
login_body="$(jq -n '{email: env.SMOKE_EMAIL, password: env.SMOKE_PASSWORD}')"
login_response="$(printf '%s' "$login_body" | curl --fail --silent --show-error \
  --connect-timeout 10 --max-time 30 --retry 3 --retry-delay 2 --retry-all-errors \
  -H 'Content-Type: application/json' \
  --data-binary @- "$base_url/api/auth/login")"
token="$(jq -er '.accessToken | select(type == "string" and length > 0)' <<< "$login_response")"
jq -e '.user.role == "ADMIN"' <<< "$login_response" > /dev/null

request() {
  local method="$1" path="$2" payload="${3:-}"
  local -a body=()
  if [[ -n "$payload" ]]; then body=(--data-raw "$payload"); fi
  # Read the Authorization header from stdin instead of exposing it in argv.
  printf 'Authorization: Bearer %s\n' "$token" | curl --fail --silent --show-error \
    --connect-timeout 10 --max-time 30 --request "$method" --header @- \
    --header 'Content-Type: application/json' "${body[@]}" "$base_url$path"
}

ticket_id=''
ticket_status=OPEN
cleanup() {
  local result=$?
  trap - EXIT
  if [[ -n "$ticket_id" && "$ticket_status" == OPEN ]]; then
    if ! request DELETE "/api/tickets/$ticket_id" > /dev/null; then
      echo "Smoke test cleanup failed for lab ticket $ticket_id" >&2
      result=1
    fi
  elif [[ -n "$ticket_id" ]]; then
    echo "Lab ticket $ticket_id retained in status $ticket_status for audit history."
  fi
  exit "$result"
}
trap cleanup EXIT

ticket="$(request POST /api/tickets \
  '{"title":"CI/CD smoke test","description":"Synthetic deployment verification","priority":"LOW"}')"
ticket_id="$(jq -er '.id | select(type == "number" and . > 0)' <<< "$ticket")"
[[ "$ticket_id" =~ ^[0-9]+$ ]]
updated="$(request PATCH "/api/tickets/$ticket_id/status" '{"status":"IN_PROGRESS"}')"
jq -e '.status == "IN_PROGRESS"' <<< "$updated" > /dev/null
ticket_status=IN_PROGRESS
comment="$(request POST "/api/tickets/$ticket_id/comments" '{"content":"CI/CD smoke test comment"}')"
jq -e '.content == "CI/CD smoke test comment"' <<< "$comment" > /dev/null
detail="$(request GET "/api/tickets/$ticket_id")"
jq -e '.status == "IN_PROGRESS" and (.comments | length > 0) and (.history | length > 0)' \
  <<< "$detail" > /dev/null
request GET /api/dashboard/summary | jq -e 'type == "object"' > /dev/null
for status in RESOLVED CLOSED; do
  updated="$(request PATCH "/api/tickets/$ticket_id/status" "{\"status\":\"$status\"}")"
  jq -e --arg status "$status" '.status == $status' <<< "$updated" > /dev/null
  ticket_status="$status"
done
echo 'PASS: login, create ticket, complete status workflow, comment, history and dashboard'
