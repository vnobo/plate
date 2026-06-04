# Plate Platform

<div align="center">

[![Build](https://img.shields.io/github/actions/workflow/status/vnobo/plate/gradle-build.yml?branch=main)](https://github.com/vnobo/plate/actions)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Angular](https://img.shields.io/badge/Angular-21-red.svg)](https://angular.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17+-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.0+-red.svg)](https://redis.io/)

A reactive enterprise management platform built with **Spring Boot 4** and **Angular 21**.

</div>

## Features

- **Multi-tenant Architecture** — Full tenant isolation with dedicated resources
- **User & Group Management** — Complete lifecycle with roles, permissions, and profiles
- **RBAC** — Granular permission system with inheritance and constraints
- **Menu Management** — Dynamic menu configuration with access control
- **Audit Logging** — System activity tracking with search and analytics
- **OAuth2** — GitHub and extensible OAuth2 provider integration
- **Captcha** — Visual verification for authentication security
- **Reactive Stack** — Spring WebFlux + R2DBC for non-blocking I/O
- **SSR & PWA** — Angular server-side rendering and progressive web app support
- **Flyway Migrations** — Automated database schema management

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4.0.6, WebFlux, R2DBC, Spring Security, Lombok |
| Frontend | Angular 21 (ng-plate) / 22 (ng-web), Tabler UI, ng-zorro-antd |
| Database | PostgreSQL 17+ (reactive), Flyway |
| Cache | Redis 7.0+ (sessions, caching) |
| Build | Gradle (Kotlin DSL), pnpm |
| CI/CD | GitHub Actions |
| Testing | JUnit 5, Testcontainers, Vitest |
| Native | GraalVM native image (0.11.5) |

## Prerequisites

- **Java** 25+ (OpenJDK)
- **PostgreSQL** 17+
- **Redis** 7.0+
- **Node.js** 22+ with **pnpm** 10+

## Quick Start

### Backend

```bash
# Clone
git clone https://github.com/vnobo/plate.git && cd plate

# Create database
sudo -u postgres psql -c "CREATE DATABASE plate;"
sudo -u postgres psql -c "CREATE USER plate WITH PASSWORD 'your_password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE plate TO plate;"

# Configure (edit boot/platform/src/main/resources/application.yml)

# Build & run
cd boot
./gradlew bootRun
```

Backend starts at `http://localhost:8080`.

### Frontend

```bash
# ng-plate (production app with SSR)
cd ui/ng-plate
pnpm install
pnpm start         # dev server at http://localhost:4200
pnpm build          # production build

# ng-web (standalone Angular 22 scaffold)
cd ui/ng-web
pnpm install
pnpm start
```

## Project Structure

```
plate/
├── boot/                              # Backend (Gradle monorepo)
│   ├── platform/                      # Core platform module
│   │   ├── src/main/java/com/plate/boot/
│   │   │   ├── security/             # OAuth2, RBAC, captcha, user/group/tenant mgmt
│   │   │   ├── relational/           # Menus, audit logging, dictionaries
│   │   │   ├── commons/              # Query builders, caching, exceptions, utilities
│   │   │   └── config/               # Security, Redis, R2DBC, WebFlux configuration
│   │   └── src/main/resources/
│   │       ├── application.yml       # Main configuration
│   │       └── db/migration/         # Flyway SQL migrations
│   ├── build.gradle                  # Root build configuration
│   └── settings.gradle               # Module settings
├── ui/
│   ├── ng-plate/                     # Production Angular 21 app (Tabler UI, SSR, PWA)
│   │   └── src/app/
│   │       ├── core/                 # HTTP interceptor, auth guard, storage services
│   │       ├── layout/               # Sidebar, header, base layout components
│   │       ├── pages/                # Dashboard, users, tenants, login, errors
│   │       └── plugins/              # Modals, toasts, data table, transfer components
│   └── ng-web/                       # Standalone Angular 22 scaffold (minimal)
├── .github/workflows/                # CI: Gradle build, tests, cache cleanup
└── LICENSE                           # Apache 2.0
```

## Configuration

Backend configuration lives in `boot/platform/src/main/resources/application.yml`.

Key settings to customize for local development:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgres://localhost:5432/plate
    username: plate
    password: your_password
  data.redis:
    host: localhost
  security.oauth2.client.registration.github:
    client-id: your_github_client_id
    client-secret: your_github_client_secret
```

Frontend environment configs: `ui/ng-plate/src/envs/`

## API

RESTful reactive endpoints with two path prefixes:

| Prefix | Scope |
|--------|-------|
| `/sec/*` | Users, groups, tenants, authentication |
| `/rel/*` | Menus, audit logs, dictionaries |

API docs available at `/swagger-ui.html` when running (via springdoc).

## Testing

```bash
# Backend (JUnit 5 + Testcontainers)
cd boot && ./gradlew test

# Frontend (Vitest)
cd ui/ng-plate && pnpm test
```

## Deployment

```bash
# Docker image (backend)
cd boot && ./gradlew bootBuildImage --imageName=ghcr.io/vnobo/plate

# Multi-platform build (amd64 + arm64)
cd boot && ./gradlew bootBuildImage --imageName=ghcr.io/vnobo/plate --platform=linux/amd64,linux/arm64
```

## Contributing

1. Fork → Branch (`feature/xxx`) → Commit → PR
2. Follow Conventional Commits: `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`
3. Backend: Google Java Style | Frontend: Angular Style Guide
4. Include tests for new features

## License

[Apache License 2.0](./LICENSE)
