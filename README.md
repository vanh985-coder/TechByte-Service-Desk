# Service Desk

## Docker deployment on Ubuntu VPS

FE: React/Vite + Node 22; BE: Spring Boot + Java 21; DB: PostgreSQL 16.
Docker Compose >= 2.24 is recommended.

Run these steps on the VPS:

1. Copy `infra/compose/env.example` to `infra/compose/.env`.
2. Set random `POSTGRES_PASSWORD` and `JWT_SECRET`, the real `APP_DOMAIN` and `APP_URL`.
3. Install the origin certificate/key in the configured `TLS_DIR`.
4. Run `docker compose up --build -d --wait --wait-timeout 180` from the repository root.
5. Check the HTTPS domain and `/api/ready`.

Follow [the Nginx/server runbook](docs/nginx-server.md) for Docker, Cloudflare,
origin TLS and troubleshooting. Nginx runs in the `reverse-proxy` container on the VPS.

Both Dockerfiles use the repository root as build context. Database migrations
are included in the backend image and applied with Flyway before schema validation.
Database state persists in `db_volume`. Production demo seed is disabled.

## CI/CD

See [CI/CD setup and release runbook](docs/deployment.md). The workflow is
`.github/workflows/deploy.yml`: `dang` pushes and PRs run CI; `main` publishes
versioned GHCR images and deploys to the Ubuntu VPS over SSH.
