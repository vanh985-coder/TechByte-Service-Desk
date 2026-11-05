# Service Desk

## Docker deployment on Ubuntu VPS

FE: React/Vite + Node 22; BE: Spring Boot + Java 21; DB: PostgreSQL 16.
Docker Compose >= 2.24 is recommended.

Run these steps on the VPS:

1. Copy `infra/compose/env.example` to `infra/compose/.env`.
2. Set `POSTGRES_PASSWORD`, `JWT_SECRET` and `APP_URL=https://anhdang2k7.io.vn`.
3. Install Nginx directly on Ubuntu, the origin certificate/key in `/etc/nginx/ssl`,
   and `infra/nginx/default.conf` as `/etc/nginx/sites-available/anhdang2k7.io.vn`;
   enable the site through `sites-enabled`, test and reload Nginx.
4. Run `docker compose up --build -d --remove-orphans --wait --wait-timeout 180` from the repository root.
5. Check the HTTPS domain and `/api/ready`.

Follow [the Nginx/server runbook](docs/nginx-server.md) for Docker, Cloudflare,
origin TLS and troubleshooting. Ubuntu Nginx owns public ports 80/443 and proxies
to frontend on `127.0.0.1:8081` and backend on `127.0.0.1:8080`. Database has no
published port. Stop the old proxy container before enabling Ubuntu Nginx when migrating.

Both Dockerfiles use the repository root as build context. Database migrations
are included in the backend image and applied with Flyway before schema validation.
Database state persists in `db_volume`. Production demo seed is disabled.

## CI/CD

See [CI/CD setup and release runbook](docs/deployment.md). The workflow is
`.github/workflows/deploy.yml`: `dang` pushes and PRs run CI; `main` publishes
versioned GHCR images and deploys to the Ubuntu VPS over SSH.
