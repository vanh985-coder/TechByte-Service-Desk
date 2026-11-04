# syntax=docker/dockerfile:1
# Build context: repository root (docker build -f backend/Dockerfile .).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace/backend

COPY backend/pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
  mvn --batch-mode --no-transfer-progress dependency:go-offline
COPY backend/src/ ./src/
COPY database/migrations/ /workspace/database/migrations/
# CI runs tests separately; the image build only packages the application.
RUN --mount=type=cache,target=/root/.m2 \
  mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN apk add --no-cache curl \
  && addgroup -S -g 10001 app \
  && adduser -S -D -H -u 10001 -G app app
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/backend/target/service-desk-backend.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=prod \
    SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
    SPRING_FLYWAY_LOCATIONS=classpath:db/migration \
    SPRING_FLYWAY_BASELINE_ON_MIGRATE=false \
    APP_SEED_ENABLED=false \
    SEED_DEMO_DATA=false \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD curl --fail --silent http://127.0.0.1:8080/ready > /dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
