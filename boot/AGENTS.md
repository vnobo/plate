# AGENTS.md — Backend (boot/)

This file provides backend (Java) guidance for AI agents (Claude Code and others). See the root `AGENTS.md` / `CLAUDE.md` for monorepo overview, frontend, and CI/CD. The mandatory Java coding standards are in the Coding Standards section below.

## Project Overview

Single Gradle multi-module build: root project `plate` with one submodule `platform`. Group ID `com.alex.plate`, version `4.1.0`. Spring Boot 4.1 / WebFlux / R2DBC / Redis, Java 25 with virtual threads.

## Common Commands

All commands run from `boot/` directory:

```bash
./gradlew build                    # Full build with tests
./gradlew :platform:build          # Platform module only
./gradlew :platform:compileJava    # Compile only, skip tests
./gradlew :platform:bootRun --args='--spring.profiles.active=local'  # Run locally (port 8080)
./gradlew test                     # All tests (requires Docker for Testcontainers)
./gradlew :platform:test           # Platform tests only
./gradlew :platform:test --tests "com.plate.boot.security.SecurityManagerTest"  # Single class
./gradlew :platform:test --tests "*MethodName*"  # Single method
./gradlew bootBuildImage           # Build OCI image via Buildpacks
./gradlew nativeCompile            # GraalVM native image
```

## Architecture

### Tech Stack

- **Framework**: Spring Boot 4.1.0, Spring WebFlux (reactive, non-blocking on Netty)
- **Database**: PostgreSQL via R2DBC (reactive), Flyway migrations (V1.0.0–V1.0.6)
- **Cache/Session**: Redis (cache prefix `plate:caches:`, 10min TTL; session 8h timeout)
- **Security**: Spring Security (session-based, `@PreAuthorize`), GitHub OAuth2, CSRF cookie tokens
- **Java**: 25 with virtual threads (`spring.threads.virtual.enabled: true`)
- **Logging**: Log4j2 (Logback excluded), `@Log4j2` annotation
- **JSON**: Jackson 3.x (`tools.jackson.databind`), use `ContextUtils.OBJECT_MAPPER` — never `new ObjectMapper()`
- **UUIDs**: UUIDv7 via local `Uuid7` generator (RFC 9562, no third-party dependency), use `ContextUtils.nextId()`

### Package Structure

```
com.plate.boot/
├── config/            # SecurityConfiguration, WebConfiguration, R2dbcConfiguration, RedisConfiguration, SessionConfiguration
├── commons/
│   ├── base/          # AbstractEntity<T>, AbstractCache, AbstractEvent, BaseEntity<T>, BaseView
│   ├── query/         # QueryFragment (fluent SQL builder), QueryHelper, QueryJsonHelper
│   ├── utils/         # ContextUtils (global helpers), DatabaseUtils, BeanUtils
│   ├── converters/    # R2DBC type converters (JsonNode, UserAuditor, CustomTypes)
│   └── exception/     # RestServerException, QueryException, JsonException, JsonPointerException
├── relational/        # Non-security domain: dictionaries/, logger/, menus/
│   └── LoggerFilter   # WebFilter for audit logging on non-safe HTTP methods
└── security/          # Auth domain: SecurityManager, captcha/, oauth2/, core/ (user/, group/, tenant/)
```

### Request Pipeline

1. Netty (HTTP/2, port 8080) → `LoggerFilter` (audit logging for non-safe methods)
2. Spring Security: session from Redis-backed WebSession, CSRF cookie validation, `@PreAuthorize`
3. `WebConfiguration` routes by path prefix: `/rel/**` → `com.plate.boot.relational`, `/sec/**` → `com.plate.boot.security` (prefixes defined in `application.yml` `spring.webflux.properties.path-prefixes`, no `/v1` in the path; API version goes via `x-api-version` header)
4. Controller → Service (extends `AbstractCache`, Redis caching, event publishing) → Repository (R2DBC `ReactiveCrudRepository` or `DatabaseClient`)

### Entity Pattern

- All entities extend `AbstractEntity<T>` implementing `BaseEntity<T>`
- Fields: `id`, `code` (UUIDv7, auto-generated), `tenantCode`, `extend` (JsonNode/JSONB), `version`, `createdBy/At`, `updatedBy/At`
- Transient fields: `query` (Map for dynamic queries), `search` (full-text), `securityCode` (current user UUID)
- `BaseEntity.query()` builds `QueryFragment` from entity fields for dynamic queries

### Dynamic SQL (QueryFragment)

- Extends `HashMap<String, Object>` — fluent parameterized SQL builder
- Methods: `from()`, `column()`, `where()`, `in()`, `like()`, `between()`, `orderBy()`, `groupBy()`, `limit()`, `pageable()`, `ts()` (PostgreSQL tsvector)
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

All tables prefixed `se_`: `se_users`, `se_authorities`, `se_groups`, `se_group_authorities`, `se_group_members`, `se_tenants`, `se_tenant_members`, `se_menus`, `se_loggers`, `se_dictionaries`.

Common columns: UUIDv7 `code` (PK), `tenant_code`, `extend` JSONB, `text_search` tsvector (GIN-indexed, zhparser for Chinese), auto-updating `updated_at` triggers.

## Key Conventions

- Use `ContextUtils.OBJECT_MAPPER` for JSON — never `new ObjectMapper()`
- Use `ContextUtils.nextId()` for entity UUIDs (time-ordered UUIDv7)
- Use `ContextUtils.securityDetails()` for reactive current-user lookup (not `SecurityContextHolder`)
- Use `ContextUtils.eventPublisher(event)` to fire `ApplicationEvent` for decoupled audit logging
- Use `@RequiredArgsConstructor` with `final` fields for dependency injection (Lombok)
- Use `@Log4j2` for logging — Logback is excluded
- Response DTOs (e.g., `UserRes`) must omit sensitive fields like passwords
- Path prefixes: `/rel` for relational, `/sec` for security endpoints (config-driven via `WebfluxProperties`; no `/v1` path segment)
- Use `QueryFragment`/`QueryHelper`/`QueryJsonHelper` for all dynamic SQL — never string concatenation

## Coding Standards (MANDATORY — Java: Alibaba Java Coding Guidelines)

All Java produced or modified in this backend MUST follow **Alibaba Java Coding Guidelines** (https://github.com/alibaba/Alibaba-Java-Coding-Guidelines). These are **advisory only** — there is intentionally **no Checkstyle/PMD/SpotBugs/ErrorProne config and no `-Pquality` gate** in this repo; do not add them. Follow them by discipline when writing and reviewing.

- **Naming**
  - Packages: all-lowercase, single word, no underscore (`com.plate.boot.security`).
  - Classes/interfaces/enums: `UpperCamelCase` (e.g., `SecurityManager`, `UserRes`, `SeUser`).
  - Methods/variables: `lowerCamelCase`.
  - Constants: `UPPER_CASE_WITH_UNDERSCORES` (`MAX_RETRY_COUNT`); `long` literals use uppercase `L` (`600000L`); no magic numbers — extract to named constants.
  - Array declaration: `String[] args` (not `String args[]`).
- **OOP**
  - Always annotate overrides with `@Override`.
  - Prefer `java.util.Objects.equals(a, b)` over `a.equals(b)`; put constants / known-non-null on the left of `equals`/`compareTo`.
  - Avoid raw types; POJOs/entities implement `equals`/`hashCode` together and provide `toString` (or Lombok `@Data`/`@ToString`).
  - Lombok: use `@RequiredArgsConstructor` + `final` fields for DI (see Key Conventions).
- **Formatting (Java)**: **4-space** indent (no tabs); opening brace `{` on same line as the declaration, closing `}` on its own line; braces required even for single-statement `if/for/while`; one blank line between methods; space after keywords (`if (`), no space inside call parentheses (`method(arg)`).
- **Concurrency (virtual threads — be careful)**
  - Even with Java 25 virtual threads, do not share mutable state across requests via `synchronized`; prefer `java.util.concurrent` utilities.
  - Never `Executors.newFixedThreadPool`/`newCachedThreadPool` — use `ThreadPoolExecutor` with a bounded queue.
  - `java.text.SimpleDateFormat` is not thread-safe → use `java.time.format.DateTimeFormatter`.
  - Guard shared state with `volatile` + double-checked locking or locks; prefer `Lock.tryLock()`.
- **Collections / Maps**
  - Iterate maps with `entrySet()`; use `ConcurrentHashMap` (not `Hashtable`/`Collections.synchronizedMap`).
  - Size `ArrayList`/`HashMap` with expected capacity when known.
  - `subList` reflects the backing list (careful with structural modifications); `toArray(new Type[0])` for typed arrays.
- **Reactive discipline (WebFlux)**
  - Controllers/services return `Mono<T>`/`Flux<T>`; never block the event loop (no `.block()` in the request path; no synchronous IO).
  - Chain with `.map`/`.flatMap`/`.filter`; use `.subscribe()` only at well-defined boundaries.
  - Propagate errors via reactive error operators (`onErrorResume`, `onErrorReturn`), not swallowed `try/catch` around publishers.
- **Exceptions & Logging**
  - Never `e.printStackTrace()`; log via `@Log4j2` (Logback excluded).
  - Catch the most specific exception; never use exceptions for normal control flow.
  - Parameterized logging (`log.error("fail id={}", id, ex)`), never string concatenation.
- **Comments**: Javadoc on public types/members; no commented-out dead code left in the repo.

> **Boundary**: standards are for AI code generation/review only. Do **not** add Checkstyle/PMD/SpotBugs/ErrorProne/JaCoCo config or a `-Pquality` build gate to this repo.

## Coding Standards (see root `AGENTS.md` §0)

Backend Java code MUST follow the **Alibaba Java Coding Guidelines** plus the WebFlux reactive-discipline rules (no `.block()` in the request path; propagate errors via `onErrorResume`/`onErrorReturn`). The full, authoritative standard, including the Java and Angular/TypeScript rules and the "do-not-add-lint" boundary, lives in the root [`AGENTS.md` §0](./AGENTS.md). These rules are enforced by agent discipline only; there is intentionally no Checkstyle/PMD/SpotBugs/ErrorProne config or `-Pquality` gate in this repo, so do not add them.

## Testing

Tests use **Testcontainers** with real PostgreSQL (`alexbob/postgres` image with zhparser) and Redis containers — no mocking of database/cache. Test config: `platform/src/test/resources/application.yml`. Docker daemon must be running.

Test frameworks: JUnit 5, `StepVerifier` (reactive), `WebTestClient` (WebFlux), Mockito.

## Environment

- Java 25+, Gradle 9.5+ (wrapper included)
- PostgreSQL 17+ (with `uuid-ossp`, `pg_trgm`, `zhparser` extensions)
- Redis 7.0+
- Local profile connects to `127.0.0.1:5432/plate` (PostgreSQL, user `farmer`, password `123456`) and `127.0.0.1` (Redis)
