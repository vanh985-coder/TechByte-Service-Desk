# AI Usage

## Docker / Compose / Nginx assistance — 2026-11-04

- Goal: package the existing React and Spring Boot application, connect PostgreSQL,
  and document Nginx HTTPS deployment on an Ubuntu VPS.
- Output: FE/BE Dockerfiles, production Compose for the VPS, Nginx configurations,
  example environment, Docker/Git exclusions, Flyway dependencies and server runbook.
- Human verifier: pending review by the DevOps owner; DB/BE review migration setup.
- Verification so far: Maven verify passed all 9 existing backend tests (H2) and
  built the executable JAR in an isolated copy. React npm ci and production build passed; both production Compose entrypoints and configuration assertions passed.
- Runtime limit: this workstation has no usable Docker Engine; complete image builds,
  PostgreSQL startup, browser smoke tests and VPS HTTPS deployment require Docker.
