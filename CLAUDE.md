# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Plate Platform — a reactive, multi-tenant enterprise management system. Polyglot monorepo:

- **Backend** (`boot/`): Spring Boot 4.1 / WebFlux / R2DBC / Redis — Java 25, Gradle 9.5.x
- **Frontend** (`ui/ng-plate/`): Angular 22 SSR — TypeScript 6, pnpm 11.12.0, Tabler UI

For detailed architecture, data model, API contract, and coding standards, see [`AGENTS.md`](./AGENTS.md). Module-specific guides: [`boot/AGENTS.md`](./boot/AGENTS.md), [`ui/ng-plate/AGENTS.md`](./ui/ng-plate/AGENTS.md).

## Default dev credentials
- Backend Swagger UI: `http://127.0.0.1:8080/swagger-ui.html`
- Admin: `admin` / `123456`, user: `user` / `123456`
- PostgreSQL: `127.0.0.1:5432/plate`, user `farmer` / `123456`
- Redis: `127.0.0.1`

## Architecture

### Module dependency rules

- `commons/` → `security/` or `relational/`: ❌ forbidden (commons is the base layer)
- `security/` ↔ `relational/`: ❌ forbidden (mutually independent)
- Both may depend on `commons/` and `config/`
- Any module may use `ContextUtils`

### API path prefixes

- `/sec/**` — security endpoints (auth, users, groups, tenants, captcha)
- `/rel/**` — relational endpoints (menus, dictionaries, loggers)
- `/oauth2/logout` — Spring Security internal (no `/sec` prefix)
- No `/v1` in paths; API version via `x-api-version` header

### Data model

All tables prefixed `se_` with UUIDv7 PK (`code`), `tenant_code` (multi-tenant), `extend` JSONB, `version` (optimistic lock), audit columns. Tenant isolation is enforced at the application layer via `SecurityDetails.getTenantCode()` injected into query builders — not via DB row-level security.

## Key Conventions (mandatory for AI-generated code)

### Backend (Java)
- **JSON**: always `ContextUtils.OBJECT_MAPPER` — never `new ObjectMapper()`
- **IDs**: `ContextUtils.nextId()` (UUIDv7)
- **Current user**: `ContextUtils.securityDetails()` — never `SecurityContextHolder`
- **Events**: `ContextUtils.eventPublisher(event)`
- **Dynamic SQL**: `QueryFragment`/`QueryHelper`/`QueryJsonHelper` — never string concatenation
- **DI**: Lombok `@RequiredArgsConstructor` + `final` fields
- **Reactive**: controllers return `Mono<T>`/`Flux<T>`, never `.block()` in request path
- **Logging**: `@Log4j2` (Logback excluded) — never `e.printStackTrace()` or `System.out`
- **DTOs**: `*Req` for requests, `*Res` for responses (mask sensitive fields)
- **Authorization**: `@PreAuthorize("hasRole('...')")`; admin constant: `ContextUtils.RULE_ADMINISTRATORS` (value: `ROLE_SYSTEM_ADMINISTRATORS`)
- **Password encoding**: `DelegatingPasswordEncoder` (default bcrypt)
- **Formatting**: 4-space indent, braces required for all `if/for/while`

### Frontend (Angular/TypeScript)
- **Standalone components** — do NOT set `standalone: true` (default in Angular 22)
- **DI**: `inject()` function, not constructor injection
- **State**: signals (`signal()`, `computed()`, `input()`, `output()`); never `mutate`, use `update`/`set`
- **Templates**: native control flow `@if`/`@for`/`@switch`, not `*ngIf`/`*ngFor`
- **Host bindings**: `host` object in decorator, not `@HostBinding`/`@HostListener`
- **Styling**: `class`/`style` bindings, not `ngClass`/`ngStyle`
- **Forms**: prefer reactive forms
- **Images**: `NgOptimizedImage` (not for base64 inline)
- **Accessibility**: must pass AXE checks, WCAG 2.1 AA minimum
- **Formatting**: 2-space indent, single quotes, semicolons required, max 100 chars

### General
- **No quality-gate tooling**: there is intentionally no ESLint/Checkstyle/PMD/JaCoCo config in the repo — do not add lint/coverage scripts or CI quality gates
- **No modifying existing Flyway migrations**: new tables require new `V1.x.y__*.sql` scripts

## Environment requirements

| Component | Version |
|-----------|---------|
| Java | 25+ (Liberica JDK recommended) |
| Gradle | 9.5+ (wrapper included) |
| PostgreSQL | 17+ (uuid-ossp, pg_trgm, zhparser) |
| Redis | 7.0+ |
| Docker | latest (for Testcontainers) |
| Node.js | LTS (22+) |
| pnpm | 11.12.0 |
