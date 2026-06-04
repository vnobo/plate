# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Plate Platform is a reactive, multi-tenant backend management system built on Spring Boot 4.0 / WebFlux / R2DBC / Redis,
targeting Java 25 with virtual threads enabled. It provides user management, RBAC authorization, hierarchical menus,
operation logging, and OAuth2 login — all through a non-blocking reactive pipeline.

Single Gradle multi-module build: root project `plate` with one submodule `platform`. Group ID `com.alex.plate`, version
`4.0.6`.

## Common Commands

### Build

```bash
./gradlew build                    # Full build
./gradlew :platform:build          # Platform module only (faster)
```

### Run Locally (port 8080, requires local PostgreSQL + Redis)

```bash
./gradlew :platform:bootRun --args='--spring.profiles.active=local'
```

### Tests (requires Docker daemon for Testcontainers)

```bash
./gradlew test                                          # All tests
./gradlew :platform:test                                # Platform only
./gradlew :platform:test --tests "com.plate.boot.security.SecurityManagerTest"  # Single class
./gradlew :platform:test --tests "*MethodName*"         # Single method
```

### Docker

```bash
./gradlew bootBuildImage           # Build OCI image via Buildpacks → alexbob/plate-platform:latest
```

## Architecture

### Tech Stack

- **Framework**: Spring Boot 4.0.x, Spring WebFlux (reactive, non-blocking)
- **Database**: PostgreSQL via R2DBC (reactive), Flyway migrations (7 files, baseline V1.0.0)
- **Cache/Session**: Redis (cache prefix `plate:caches:`, 10min TTL; session 8h timeout)
- **Security**: Spring Security (session-based, `@PreAuthorize`), GitHub OAuth2, CSRF cookie tokens
- **Java**: 25 with virtual threads (`spring.threads.virtual.enabled: true`)
- **Logging**: Log4j2 (Logback excluded), `@Log4j2` annotation
- **JSON**: Jackson 3.x (`tools.jackson.databind`), use `ContextUtils.OBJECT_MAPPER` — never `new ObjectMapper()`
- **UUIDs**: UUIDv7 via `UuidCreator.getTimeOrderedEpoch()`, use `ContextUtils.nextId()`

### Request Pipeline

1. Netty (HTTP/2, port 8080) → `LoggerFilter` (audit logging for non-safe methods)
2. Spring Security: session from Redis-backed WebSession, CSRF cookie validation, `@PreAuthorize`
3. `WebConfiguration` routes by path prefix: `/rel/v1/**` → `com.plate.boot.relational`, `/sec/v1/**` →
   `com.plate.boot.security`
4. Controller → Service (extends `AbstractCache`, Redis caching, event publishing) → Repository (R2DBC
   `ReactiveCrudRepository` or `DatabaseClient`)

### Package Structure

```
com.plate.boot/
├── config/            # SecurityConfiguration, WebConfiguration, R2dbcConfiguration, RedisConfiguration, SessionConfiguration
├── commons/
│   ├── base/          # AbstractEntity<T>, AbstractCache, BaseEntity<T>
│   ├── query/         # QueryFragment (fluent SQL builder), QueryHelper, QueryJsonHelper
│   ├── utils/         # ContextUtils (global helpers), DatabaseUtils, BeanUtils
│   ├── converters/    # R2DBC type converters (JsonNode, UserAuditor)
│   └── exception/     # RestServerException, QueryException, JsonException
├── relational/        # Non-security domain: dictionaries/, logger/, menus/
└── security/          # Auth domain: SecurityManager, captcha/, oauth2/, core/ (user/, group/, tenant/)
```

### Entity Pattern

- All entities extend `AbstractEntity<T>` implementing `BaseEntity<T>`
- Fields: `id`, `code` (UUIDv7, auto-generated), `tenantCode`, `extend` (JsonNode/JSONB), `version`, `createdBy/At`,
  `updatedBy/At`
- Transient fields: `query` (Map for dynamic queries), `search` (full-text), `securityCode` (current user UUID)
- `BaseEntity.query()` builds `QueryFragment` from entity fields for dynamic queries

### Dynamic SQL (QueryFragment)

- Extends `HashMap<String, Object>` — fluent parameterized SQL builder
- Methods: `from()`, `column()`, `where()`, `in()`, `like()`, `between()`, `orderBy()`, `groupBy()`, `limit()`,
  `pageable()`, `ts()` (PostgreSQL tsvector)
- `querySql()` → SELECT, `countSql()` → COUNT
- `QueryHelper` converts entities to Spring Data `Criteria`
- `QueryJsonHelper` parses HTTP params like `extend.fieldNameEq=value` into JSONB conditions

### Caching

- Services extend `AbstractCache` → `queryWithCache()`, `countWithCache()`
- Redis prefix `plate:caches:`, TTL 10min (5min in tests)
- Large objects (>256KB prod, >10MB test) skip caching

### Security

- Auth: Form login, HTTP Basic, GitHub OAuth2
- Session: Redis-backed WebSession, one concurrent session per principal
- Roles: `ROLE_SYSTEM_ADMINISTRATORS`, `ROLE_USER` (fixed, via `@PreAuthorize`)
- Multi-tenancy: every table has `tenant_code`, filtered via `SecurityDetails.getTenantCode()`
- CSRF: cookie-based, excluded for safe methods and `/oauth2/none` POST

### Database

All tables prefixed `se_`: `se_users`, `se_authorities`, `se_groups`, `se_group_authorities`, `se_group_members`,
`se_tenants`, `se_tenant_members`, `se_menus`, `se_loggers`, `se_dictionaries`.

Common columns: UUIDv7 `code` (PK), `tenant_code`, `extend` JSONB, `text_search` tsvector (GIN-indexed, zhparser for
Chinese), auto-updating `updated_at` triggers.

## Key Conventions

- Use `ContextUtils.OBJECT_MAPPER` for JSON — never instantiate `new ObjectMapper()`
- Use `ContextUtils.nextId()` for entity UUIDs (time-ordered UUIDv7)
- Use `ContextUtils.securityDetails()` for reactive current-user lookup (not `SecurityContextHolder`)
- Use `ContextUtils.eventPublisher(event)` to fire `ApplicationEvent` for decoupled audit logging
- Use `@RequiredArgsConstructor` with `final` fields for dependency injection (Lombok)
- Use `@Log4j2` for logging — Logback is excluded
- Response DTOs (e.g., `UserRes`) must omit sensitive fields like passwords
- Path prefixes: `/rel/v1` for relational, `/sec/v1` for security endpoints
- Use `QueryFragment`/`QueryHelper`/`QueryJsonHelper` for all dynamic SQL — never string concatenation

## Testing

Tests use **Testcontainers** with real PostgreSQL (`alexbob/postgres` image with zhparser) and Redis containers — no
mocking of database/cache. Test config: `platform/src/test/resources/application.yml`. Docker daemon must be running.

Test frameworks: JUnit 5, `StepVerifier` (reactive), `WebTestClient` (WebFlux), Mockito.

## Environment

- Java 25+, Gradle 9.5+ (wrapper included)
- PostgreSQL 14+ (with `uuid-ossp`, `pg_trgm`, `zhparser` extensions)
- Redis 6.0+
- Local profile connects to `127.0.0.1:5432/plate` (PostgreSQL) and `127.0.0.1` (Redis)
