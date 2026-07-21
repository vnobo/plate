# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Plate is a monorepo containing a reactive multi-tenant backend management platform and its Angular frontend.

- **Backend** (`boot/`): Spring Boot 4.1 / WebFlux / R2DBC / Redis — Java 25, Gradle 9.5.x
- **Frontend** (`ui/ng-plate/`): Angular 22 SSR — TypeScript 6, pnpm 11.12.0, Tabler UI

## Build & Run Commands

### Backend (run from `boot/`)

```bash
./gradlew build                                          # Full build with tests
./gradlew :platform:build                                # Platform module only
./gradlew :platform:compileJava                          # Compile only, skip tests
./gradlew :platform:bootRun --args='--spring.profiles.active=local'  # Run locally (port 8080)
./gradlew test                                           # All tests (requires Docker)
./gradlew :platform:test                                 # Platform tests only
./gradlew :platform:test --tests "com.plate.boot.security.SecurityManagerTest"  # Single test class
./gradlew :platform:test --tests "*MethodName*"          # Single test method
./gradlew bootBuildImage                                 # Build OCI image via Buildpacks
./gradlew nativeCompile                                  # GraalVM native image
```

### Frontend (run from `ui/ng-plate/`)

```bash
pnpm install                                             # Install dependencies
pnpm start                                               # Dev server at http://localhost:4200
pnpm build                                               # Production build (outputs to dist/)
pnpm test                                                # Run unit tests (Vitest via @angular/build:unit-test)
```

Dev server proxies `/api` → `http://localhost:8080/` (path rewrite strips `/api` prefix, configured in `proxy.conf.json`).

## Architecture

### Monorepo Layout

```
plate/
├── boot/                          # Backend (Gradle multi-module)
│   ├── build.gradle               # Root build: plugins, shared deps, Java 25 toolchain
│   ├── settings.gradle            # rootProject "plate", includes :platform
│   ├── gradle.properties          # version=4.1.0, graalvm/guava/springdoc versions
│   └── platform/                  # Single application module
│       └── src/main/java/com/plate/boot/
│           ├── config/            # Infrastructure: R2DBC, Redis, Session, Security, Web
│           ├── commons/           # Base classes, query framework, utils, exceptions
│           ├── relational/        # Business domain: dictionaries, logger, menus
│           └── security/          # Auth domain: users, groups, tenants, captcha, oauth2
├── ui/ng-plate/                   # Frontend (Angular 22 SSR)
│   ├── angular.json               # Build config: pnpm, SCSS, Tabler CSS/JS, SSR enabled
│   ├── proxy.conf.json            # Dev proxy: /api → http://localhost:8080/ (strips /api prefix)
│   └── src/app/
│       ├── core/                  # HTTP interceptor (XSRF + auth), guards, services, storage
│       ├── layout/                # BaseLayout, BlankLayout, sidebar, header
│       ├── pages/                 # Feature routes: passport, dashboard, platform, examples, error
│       └── plugins/               # Reusable UI: DataTable, Modals, Toasts, Transfer
└── .github/workflows/             # CI: gradle-tests.yml, gradle-build.yml, cleanup-caches.yml
```

### Backend Module Dependency Graph

```
BootApplication (entry point)
       │
       ▼
    Config Agent ──┐  (R2DBC, Redis, Session, Security, Web beans)
       │           │
       ▼           │
   Commons Agent ──┘  (base classes, query builder, utils, converters, exceptions)
       │
       ▼
  Security Agent    Relational Agent   (independent — no cross-dependencies)
```

**Dependency rules (enforced by convention)**:
- `commons/` must NOT depend on `security/` or `relational/`
- `config/` must NOT depend on business package beans
- `security/` and `relational/` are independent — no cross-dependencies

### Request Pipeline

Netty (HTTP/2) → `LoggerFilter` (audit log for non-safe methods) → Spring Security (session from Redis, CSRF cookie, `@PreAuthorize`) → `WebConfiguration` path-prefix routing (`/rel` → relational, `/sec` → security) → Controller → Service (extends `AbstractCache`, Redis caching) → Repository (R2DBC) → PostgreSQL

### Key Patterns

**Entity pattern**: All entities extend `AbstractEntity<T>` with fields: `id`, `code` (UUIDv7 PK, auto-generated via `ContextUtils.nextId()`), `tenantCode`, `extend` (JSONB), `version`, `createdBy/At`, `updatedBy/At`. Transient fields: `query` (Map), `search` (full-text), `securityCode` (current user UUID).

**Per-feature package structure**: Each domain feature contains:
- `Entity.java` — extends `AbstractEntity`
- `*Req.java` — request DTO
- `*Res.java` — response DTO (sensitive fields hidden via `@JsonIgnore`)
- `*Event.java` — domain event (extends `AbstractEvent`)
- `*Service.java` — extends `AbstractCache` for Redis caching
- `*Repository.java` — extends `R2dbcRepository`
- `*Controller.java` — `@RestController`, returns `Mono<T>`/`Flux<T>`

**Dynamic SQL**: Use `QueryFragment` (fluent builder extending `HashMap<String,Object>`) with `from()`, `where()`, `in()`, `like()`, `ts()` for PostgreSQL tsvector full-text search. Never concatenate SQL strings.

**Caching**: Services extend `AbstractCache` → `queryWithCache()`/`countWithCache()`. Redis prefix `plate:caches:`, TTL 10min. Objects >256KB skip cache.

**Frontend Architecture**: Angular 22 SSR with incremental hydration. Routes are lazy-loaded via `loadChildren` from `@app/pages`. XSRF protection configured with cookie `XSRF-TOKEN` and header `X-XSRF-TOKEN`. Service worker enabled in production. Key route groups: `passport` (login/lock), `dashboard` (users/welcome), `platform` (tenant management), `examples` (demos), `error` (404/500/512).

**Database**: All tables prefixed `se_`. UUIDv7 `code` as PK. Every table has `tenant_code`, `extend` (JSONB), `text_search` (tsvector with GIN index, zhparser for Chinese). Flyway migrations in `platform/src/main/resources/db/migration/` (V1.0.0–V1.0.6). Never modify existing migration files.

## Key Conventions

- Use `ContextUtils.OBJECT_MAPPER` for JSON — never `new ObjectMapper()`
- Use `ContextUtils.nextId()` for entity UUIDs (time-ordered UUIDv7)
- Use `ContextUtils.securityDetails()` for reactive current-user lookup — not `SecurityContextHolder`
- Use `ContextUtils.eventPublisher(event)` to fire domain events
- Use `@RequiredArgsConstructor` with `final` fields for DI (Lombok)
- Use `@Log4j2` for logging — Logback is excluded
- All reactive: controllers return `Mono<T>`/`Flux<T>`, never block IO
- Path prefixes: `/rel` for relational, `/sec` for security (auto-bound by `WebConfiguration`)
- API versioning: `x-api-version` header or `apiVersion` query param, default `v1`
- CSRF: POST/PUT/DELETE require `X-XSRF-TOKEN` header or `XSRF-TOKEN` cookie
- Response DTOs must omit passwords; `UserRes` masks phone/email
- Angular: standalone components (no NgModules), signals for state, `inject()` for DI, native control flow (`@if`/`@for`/`@switch`), zoneless change detection, SSR with incremental hydration, `@tabler/core` UI framework, SCSS for styles, `zh-Hans` locale, path aliases (`@app/` → `src/app/`)

## Testing

Backend tests use **Testcontainers** with real PostgreSQL (`alexbob/postgres` image with zhparser extension) and Redis — no mocking of database/cache. Docker daemon must be running.

- Integration tests: `ApplicationTests.java` (`@SpringBootTest(webEnvironment = RANDOM_PORT)`, `WebTestClient`)
- Unit tests: `SecurityManagerTest.java`, `SecurityControllerTest.java`
- Test config: `platform/src/test/resources/application.yml` (cache TTL 5min, debug logging)
- Test infrastructure: `InfrastructureConfiguration.java` (`@TestConfiguration` with Testcontainers beans)

Frontend tests use Vitest via `@angular/build:unit-test` builder (`pnpm test` from `ui/ng-plate/`).

## Environment

- Java 25+ (Liberica JDK recommended), Gradle 9.5+ (wrapper included)
- PostgreSQL 14+ with extensions: `uuid-ossp`, `pg_trgm`, `zhparser`
- Redis 6.0+
- Docker (required for Testcontainers)
- Node.js + pnpm 11.12.0 (frontend)
- Local backend: `127.0.0.1:5432/plate` (user `farmer`, password `123456`), Redis at `127.0.0.1`
- Default admin: `admin` / `123456`
- Swagger UI: `http://127.0.0.1:8080/swagger-ui.html`

## CI/CD

GitHub Actions workflows in `.github/workflows/`:
- `gradle-tests.yml` — runs `./gradlew test` on push to `main`/`dev`
- `gradle-build.yml` — builds multi-arch OCI images (amd64+arm64) via Buildpacks, pushes to GHCR and Docker Hub
- `cleanup-caches.yml` — cleans PR caches on close

Images published to: `ghcr.io/.../plate-platform` and `docker.io/alexbob/plate-platform`

## Additional Documentation

- `boot/CLAUDE.md` — Backend-specific guidance (tech stack details, package structure, security patterns, database schema)
- `AGENTS.md` — Detailed agent/module definitions, input/output specs, dependency graph, request/response formats
