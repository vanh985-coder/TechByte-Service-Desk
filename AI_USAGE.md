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

## Integration audit and CI/CD — 2026-11-05 (Asia/Bangkok)

- Goal: review source/configuration after merging main 203f643 into dang and
  complete Docker/Compose/GitHub Actions for the actual application contract.
- Fixes used: login email/accessToken mapping, ticket enum/DTO mapping, embedded
  history/comments, technician permissions, dashboard counts, lint failures,
  JWT failure handling, startup readiness and canonical Flyway resources in JAR.
- Operations fixes: database env precedence, production schema validation,
  disabled automatic baseline/demo seed, binary-safe backup/restore and explicit
  restore error handling; workflow uses immutable images and versioned VPS configs.
- Verification: frontend lint, 7 regression tests and production build passed;
  PostgreSQL 16.15 migration/schema validation and the full ticket API workflow
  passed using a temporary DB, including restart with existing data. PowerShell
  binary-preservation/error propagation checked with a Docker CLI mock.
- Final checks: 14 backend tests, 7 frontend tests, frontend lint/build, actionlint,
  13 workflow Bash blocks, both Compose entrypoints, canonical SQL in JAR, and
  Bash/PowerShell backup byte-preservation and restore-failure regression checks passed.
  Docker CLI tests used mocks; the workflow performs real Docker backup/restore on CI.
- Limits: Docker image execution, Docker backup/restore and real VPS HTTPS release
  are configured for CI/server verification; no production server was changed.
- Human verification: pending DevOps/FE/BE/DB owner review and GitHub Actions run.
