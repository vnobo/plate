# AGENTS.md

> Plate Platform — reactive, multi-tenant administration system (Polyglot Monorepo).
> This document provides high-signal project context for AI Agents to onboard quickly and avoid pitfalls.
>
> - **Backend** (`boot/`): Spring Boot 4.1 / WebFlux / R2DBC / Redis — Java 25, Gradle 9.5.x
> - **Frontend** (`ui/ng-plate/`): Angular 22 SSR — TypeScript 6, pnpm 11.12.0, Tabler UI
>
> Module-ownership note: the backend/frontend context below is maintained per-module by the agent files
> `boot/AGENTS.md` (backend) and `ui/ng-plate/AGENTS.md` (frontend). This root file is the consolidated entry point.

---

## 0. Coding Standards (AI agents MUST follow — discipline-based, not build-enforced)

All code produced or modified by AI agents in this repository MUST follow the standards below. They are enforced by agent discipline only — there is intentionally no ESLint/PMD/Checkstyle/JaCoCo config or `lint`/`coverage` scripts in the repo; do not add them. Follow them rigorously when writing and reviewing code.

### Java — Alibaba Java Coding Guidelines
Authority: https://github.com/alibaba/Alibaba-Java-Coding-Guidelines

- **Naming**
  - Packages: all-lowercase, single word, no underscore (`com.plate.boot.security`).
  - Classes/interfaces/enums: `UpperCamelCase` (e.g., `SecurityManager`, `UserRes`).
  - Methods/variables: `lowerCamelCase`.
  - Constants: `UPPER_CASE_WITH_UNDERSCORES` (`MAX_RETRY_COUNT`); `long` literals use uppercase `L` (`600000L`); no magic numbers.
  - Array declaration: `String[] args` (not `String args[]`).
- **OOP**
  - Always annotate overrides with `@Override`.
  - Prefer `java.util.Objects.equals(a, b)` over `a.equals(b)`; put constants/known-non-null on the left of `equals`/`compareTo`.
  - Avoid raw types; POJOs/entities implement `equals`/`hashCode` together and provide `toString`.
- **Formatting (Java)**
  - Indent with **4 spaces** (no tabs).
  - Opening brace `{` on the same line as the declaration; closing brace `}` on its own line.
  - Always use braces for `if/for/while`, even single statements.
  - One blank line between methods; no blank line immediately after `{`.
  - Space after keywords (`if (` `for (` `while (`); no space inside method-call parentheses.
- **Concurrency**
  - Create thread pools via `ThreadPoolExecutor`, never `Executors.newFixedThreadPool`/`newCachedThreadPool` (OOM / unbounded-queue risk).
  - `java.text.SimpleDateFormat` is not thread-safe → use `java.time.format.DateTimeFormatter`.
  - Guard shared state with `volatile` + double-checked locking or `java.util.concurrent` utilities; prefer `Lock.tryLock()`.
  - Use `CountDownLatch`/`CyclicBarrier`/`Semaphore`/`BlockingQueue` for coordination.
- **Reactive discipline (WebFlux)**
  - Controllers/services return `Mono<T>`/`Flux<T>`; never block the event loop (no `.block()` in the request path; no synchronous IO).
  - Chain with `.map`/`.flatMap`/`.filter`; use `.subscribe()` only at well-defined boundaries.
  - Propagate errors via reactive error operators (`onErrorResume`, `onErrorReturn`), not swallowed `try/catch` around publishers.
- **Collections / Maps**
  - Iterate maps with `entrySet()`; use `ConcurrentHashMap` (not `Hashtable`).
  - Size `ArrayList`/`HashMap` with expected capacity when known.
  - `subList` reflects the backing list; use `toArray(new Type[0])` for typed arrays.
- **Exceptions & Logging**
  - Never `e.printStackTrace()`; log via the project logger (`@Log4j2`; `Logback` excluded).
  - Catch the most specific exception; never use exceptions for normal control flow.
  - Parameterized logging (`log.error("fail id={}", id, ex)`), not string concatenation.
- **Comments**
  - Public classes/methods/interfaces get Javadoc; no commented-out dead code left in the repo.

### Angular / TypeScript — Angular Style Guide + Google TypeScript Style
Authorities: Angular Style Guide (https://angular.dev/style-guide) + Google TypeScript Style (https://google.github.io/styleguide/tsguide.html).

- **Naming**
  - Files: `kebab-case.feature.type.ts` (e.g., `user-list.component.ts`).
  - Classes/types/enums/interfaces: `UpperCamelCase` (`UserListComponent`, `UserRes`).
  - Variables/functions/methods: `camelCase`; constants: `UPPER_SNAKE_CASE`.
  - Component selectors: prefixed (`app-*` or feature prefix), `kebab-case`.
- **Formatting (TypeScript)** (AI discipline; project formatter uses Prettier; if a linter is ever introduced it should be `@angular-eslint` + Google TS Style)
  - **2-space** indentation (no tabs).
  - **Single quotes** for strings (double quotes only to avoid escaping).
  - **Semicolons** required at end of statements.
  - **Max line width 100** characters.
  - `no-var`: use `let`/`const`; prefer `const`. Use `===`/`!==` (never `==`/`!=`).
- **Type discipline**
  - Avoid `any`; prefer `unknown` + narrowing, or precise types.
  - Prefer `interface` for object shapes; `type` for unions/mapped types.
  - Use `readonly` / `as const` where appropriate; prefer immutable data.
- **Angular idioms (follow repo patterns)**
  - **Standalone components** (no `NgModule` where avoidable).
  - State via **signals**; DI via **`inject()`**; native control flow (`@if`/`@for`/`@switch`).
  - `OnPush` change detection; services use `providedIn: 'root'`.
  - Lazy-load routes via `loadChildren`; use `NgOptimizedImage` for `<img>`.
  - One responsibility per file; keep functions < ~75 lines; keep files focused.
  - Prefer `for...of` over indexed `for`; use arrow functions / `bind` to preserve `this`.
- **Accessibility**: meet WCAG 2.1 AA; provide alt text, labels, keyboard support (AXE-clean).

> **Boundary (do not violate)**: These rules are for AI code generation and review only. Do **not** add ESLint/PMD/Checkstyle/JaCoCo config files, `lint`/`coverage` scripts, or CI quality gates to this repo. Keep the project structure clean.

---

## 1. Development Commands Cheat-Sheet

### Backend (run from `boot/`)

```bash
./gradlew :platform:bootRun --args='--spring.profiles.active=local'  # Local start (port 8080)
./gradlew :platform:test                                              # Platform tests (requires Docker)
./gradlew :platform:test --tests "com.plate.boot.security.SecurityManagerTest"  # Single test class
./gradlew :platform:test --tests "*MethodName*"                       # Single test method
./gradlew :platform:compileJava                                       # Compile only, skip tests
./gradlew build                                                        # Full build + tests
./gradlew bootBuildImage                                              # Buildpacks OCI image build
./gradlew nativeCompile                                                 # GraalVM native image (see boot/AGENTS.md)
```

### Frontend (run from `ui/ng-plate/`)

```bash
pnpm install          # Install dependencies
pnpm start            # Dev server → http://localhost:4200 (proxy /api → localhost:8080)
pnpm build            # Production build (SSR + CSR, outputs dist/)
pnpm test             # Vitest unit tests
pnpm serve:ssr:ng-plate  # Serve the SSR build (Express, http://localhost:4000)
```

### Default dev credentials

- Backend Swagger UI: `http://127.0.0.1:8080/swagger-ui.html`
- Admin: `admin` / `123456`, regular user: `user` / `123456`
- PostgreSQL: `127.0.0.1:5432/plate`, user `farmer` / `123456`
- Redis: `127.0.0.1`

---

## 2. Monorepo Structure

```
plate/
├── AGENTS.md                    # This file
├── CLAUDE.md                    # Project overview guide (English)
├── boot/                        # Backend (Gradle single-module build)
│   ├── AGENTS.md                # Backend-specific guide (English)
│   ├── build.gradle             # Root build script (plugins/deps/Java 25 toolchain)
│   ├── settings.gradle          # rootProject "plate", include :platform
│   ├── gradle.properties        # version=4.1.0, graalvm, guava, springdoc versions
│   └── platform/                # The only submodule — all backend code
│       └── src/
│           ├── main/java/com/plate/boot/
│           │   ├── BootApplication.java       # Spring Boot entry (@SpringBootApplication)
│           │   ├── commons/                   # Common base layer
│           │   ├── config/                    # Infrastructure config layer
│           │   ├── relational/                # Business domains (dictionary/log/menu)
│           │   └── security/                  # Security domain (user/group/tenant/auth)
│           ├── main/resources/
│           │   ├── application.yml            # Production/default config
│           │   ├── application-local.yml      # Local dev override (git-ignored)
│           │   └── db/migration/              # Flyway migration scripts (V1.0.0–V1.0.6)
│           └── test/                          # Tests (Testcontainers integration + unit)
└── ui/ng-plate/                 # Frontend (Angular 22 SSR, standalone project)
    ├── AGENTS.md                # Frontend coding standards (Angular/TypeScript best practices)
    ├── angular.json             # Build config (pnpm, SCSS, Tabler CSS/JS, SSR)
    ├── proxy.conf.json          # Dev proxy: /api → http://localhost:8080/ (strips /api prefix)
    └── src/app/
        ├── core/                # HTTP interceptor, route guards, Token service, storage wrapper
        ├── layout/              # BaseLayout (sidebar+header), BlankLayout (login page)
        ├── pages/               # Lazy-loaded page modules
        └── plugins/             # Reusable UI components (DataTable, Modals, Toasts, Transfer)
```

**Key fact**: Backend and frontend are completely independent build systems (Gradle vs pnpm/Angular CLI) with no shared workspace config and no pre-commit hooks. There is **intentionally no quality-gate tooling in the repo** (no ESLint/Checkstyle/PMD/JaCoCo config, no `lint`/`coverage` scripts). Coding standards (see §0) are **advisory for AI agents only**, not enforced by the build.

---

## 3. Tech Stack

| Category | Technology | Version / Notes |
|----------|-------------|-----------------|
| **Backend language** | Java | 25 (virtual threads, GraalVM support) |
| **Frontend language** | TypeScript | 6.x |
| **Backend build** | Gradle | 9.5.x (Wrapper) |
| **Frontend build** | pnpm | 11.12.0 |
| **Web framework** | Spring Boot WebFlux | 4.1.0 (reactive non-blocking, Netty HTTP/2) |
| **Frontend framework** | Angular | 22.x (SSR + Signals + Zoneless) |
| **Database** | PostgreSQL | 17+ (uuid-ossp, pg_trgm, zhparser extensions) |
| **Data access** | Spring Data R2DBC | Reactive relational database |
| **Cache / Session** | Redis | 7.0+ (cache + WebSession) |
| **Security** | Spring Security | Session-based + OAuth2 + CSRF Cookie |
| **Migrations** | Flyway | baseline-on-migrate, V1.0.0–V1.0.6 |
| **JSON** | Jackson 3.x (tools.jackson) | Use `ContextUtils.OBJECT_MAPPER` |
| **UI** | Tabler UI | @tabler/core CSS/JS |
| **Logging** | Log4j2 | `@Log4j2` annotation (Logback excluded) |
| **Testing** | JUnit 5 + Testcontainers | Real PostgreSQL + Redis containers |
| **Frontend testing** | Vitest | @angular/build:unit-test |
| **SSR** | Angular SSR + Express 5.1.0 | Incremental Hydration |

---

## 4. Backend Architecture

### 4.1 Module responsibilities

| Module | Path | Responsibility |
|--------|------|----------------|
| **commons/** | `com.plate.boot.commons` | Common base classes, dynamic SQL builder, utilities, type converters, exception definitions |
| **config/** | `com.plate.boot.config` | R2DBC/Redis/Session/Security/Web infrastructure config |
| **security/** | `com.plate.boot.security` | User/group/tenant CRUD, auth, permissions, captcha, OAuth2 |
| **relational/** | `com.plate.boot.relational` | Dictionary management, menu management, audit logging |

### 4.2 commons/ submodules

| Sub-package | Core classes | Responsibility |
|-------------|--------------|----------------|
| `base/` | `AbstractEntity`, `AbstractCache`, `AbstractEvent`, `BaseEntity`, `BaseView` | Entity base classes (`code` UUIDv7 PK, `tenantCode`, `extend` JSONB, `version` optimistic lock, audit fields) |
| `query/` | `QueryFragment`, `QueryHelper`, `QueryJsonHelper` | Dynamic SQL builder (`from()`, `where()`, `in()`, `like()`, `between()`, `orderBy()`, `groupBy()`, `limit()`, `pageable()`, `ts()` full-text search) |
| `utils/` | `ContextUtils`, `DatabaseUtils`, `BeanUtils` | Global utilities: `securityDetails()` current user, `nextId()` UUIDv7, `eventPublisher()` event publish, `OBJECT_MAPPER` |
| `converters/` | `JsonNodeConverters`, `UserAuditorConverters` | R2DBC type converters (JSONB ↔ JsonNode) |
| `exception/` | `RestServerException`, `QueryException`, `JsonException` | Exception hierarchy, unified by `GlobalExceptionHandler` |

### 4.3 config/ submodules

| Class | Responsibility |
|-------|----------------|
| `SecurityConfiguration` | SecurityWebFilterChain: CSRF → Authentication → Concurrent Session Control → Logout |
| `WebConfiguration` | Path-prefix routing: `/rel` → relational package, `/sec` → security package |
| `R2dbcConfiguration` | R2DBC connection config, registers type converters |
| `RedisConfiguration` | Redis connection & cache config |
| `SessionConfiguration` | Redis WebSession (8h TTL, single-user single-session `SessionLimit.of(1)`) |
| `WebfluxProperties` | Path prefixes, API version, pagination params |
| `HttpCodecsProperties` | maxInMemorySize (default 256KB) |

### 4.4 security/ submodules

| Sub-package | Core classes | Responsibility |
|-------------|--------------|----------------|
| root | `SecurityManager`, `SecurityController`, `SecurityDetails`, `CsrfWebFilter` | Auth core: `findByUsername()` (case-insensitive), caches user permissions/group/tenant info |
| `core/user/` | `User`, `UsersService`, `UsersController` | User CRUD + `authority/` sub-package for user permissions |
| `core/group/` | `Group`, `GroupsService`, `GroupsController` | User group CRUD (`code` hierarchical) + `authority/` + `member/` |
| `core/tenant/` | `Tenant`, `TenantsService`, `TenantsController` | Tenant CRUD (`code` hierarchical) + `member/` |
| `captcha/` | `CaptchaController`, `CaptchaRepository` | Captcha (Redis-backed, permitAll) |
| `oauth2/` | `Oauth2UserService`, `Oauth2SuccessHandler` | GitHub OAuth2 auto-register/bind, XHR-aware responses |

**Standard structure per feature package**: Entity → `*Req` (request DTO) → `*Res` (response DTO) → `*Event` (domain event) → `*Service` (extends `AbstractCache`) → `*Repository` (R2DBC) → `*Controller` (returns `Mono`/`Flux`).

### 4.5 relational/ submodules

| Sub-package | Core classes | Responsibility |
|-------------|--------------|----------------|
| root | `LoggerFilter`, `MethodType` | Audit-log interceptor: matches non-safe HTTP methods, buffers req/resp DataBuffer, async-publishes `LoggerEvent` |
| `dictionaries/` | `Dictionary`, `DictionariesService/Controller` | Dictionary CRUD (`code` hierarchy, `dict_type` category) |
| `logger/` | `Logger`, `LoggersService/Controller` | Audit-log query (written by `LoggerFilter` event) |
| `menus/` | `Menu`, `MenusService/Controller` | Menu management (`type`: FOLDER/MENU/LINK/API) |

### 4.6 Request processing pipeline

```
HTTP Request (Netty, port 8080, HTTP/2)
       │
       ▼
 LoggerFilter ── matches non-safe methods → buffers req/resp → async-publishes LoggerEvent
       │
       ▼
 Spring Security Chain:
    CsrfWebFilter → AuthenticationWebFilter → ConcurrentSessionControl(1) → Logout
       │
       ▼
 WebConfiguration path-prefix routing:
    /rel/**   → com.plate.boot.relational
    /sec/**   → com.plate.boot.security
    /oauth2/** → SecurityController
       │
       ▼
 Controller → Service (AbstractCache, Redis cache, event publish) → Repository (R2DBC) → PostgreSQL
```

### 4.7 Authentication flow

1. `GET /oauth2/csrf` → CsrfWebFilter writes to Reactor Context → Cookie `XSRF-TOKEN`
2. `GET /oauth2/login` → Basic Auth or existing Session → `SecurityManager.findByUsername()`
3. SecurityManager: loads user (case-insensitive) → merges direct user authorities + inherited group authorities → loads group/tenant → assembles `SecurityDetails`
4. Response → `Set-Cookie: SESSION=...` (Redis-backed WebSession)
5. Subsequent requests carry the SESSION cookie to auto-restore `SecurityContext`
6. `POST /oauth2/logout` → `Clear-Site-Data` response header

**SecurityManager cache keys** (TTL 10min): `OAUTH2_{bindType}_{openid}`, `USER_GROUPS-{userCode}`, `USER_TENANTS-{userCode}`, `USER_AUTHORITIES-{userCode}`, `GROUP_AUTHORITIES-{userCode}`

---

## 5. Frontend Architecture

### 5.1 Key configuration

- **Zoneless** change detection (Signals-driven, no Zone.js)
- **SSR** + Incremental Hydration (`withIncrementalHydration`)
- **XSRF** protection: cookie `XSRF-TOKEN` → header `X-XSRF-TOKEN`
- **PWA**: Service Worker enabled in production only
- **Path aliases**: `@app/` → `src/app/`, `@envs/` → `src/envs/`, `@styles/` → `src/styles/`
- **Env files**: `env.ts` (prod: `host: ''`, calls `/rel`/`/sec` directly) vs `env.dev.ts` (dev: `host: '/api'`, goes through proxy)
- **Styling**: SCSS, `@tabler/core` global CSS/JS
- **i18n**: `zh-Hans` locale, dayjs Chinese locale

### 5.2 Route structure

| Route | Page | Layout |
|-------|------|--------|
| `/passport` | Login, lock screen | BlankLayout |
| `/dashboard` | Welcome, user management (list+form) | BaseLayout |
| `/platform` | Tenant management | BaseLayout |
| `/examples` | DataTable example, Transfer example | BaseLayout |
| `/error` | 404, 500, 512 | — |
| `/` → `/passport` | redirect | — |
| `/**` → `/error` | fallback | — |

### 5.3 Frontend coding conventions

- Standalone components — **do NOT** set `standalone: true` (Angular 22 default)
- Use `inject()` for service injection, not constructor injection
- Manage state with Signals: `signal()`, `computed()`, `input()`, `output()`
- **Do NOT** `mutate` signals; use `update` or `set`
- Native control flow `@if`/`@for`/`@switch`, not `*ngIf`/`*ngFor`
- **Do NOT** use `@HostBinding`/`@HostListener`; use the `host` object
- **Do NOT** use `ngClass`/`ngStyle`; use `class`/`style` bindings
- External template/style paths are relative to the component TS file
- Reactive forms, not Template-driven forms
- **Accessibility (mandatory)**: must pass all AXE checks; must meet WCAG 2.1 AA minimums (focus management, color contrast, ARIA attributes)
- Use `NgOptimizedImage` for all static images (note: does NOT work for inline base64 images)

---

## 6. Module Dependency Rules

```
Config Agent ──┐
               ▼
Commons Agent ──┘  (base/query/utils/converters/exception)
       │
  ┌────┴────┐
  ▼         ▼
Security   Relational   (mutually independent, no cross-dependency)
  │         │
  └────┬────┘
       ▼
   Frontend Agent (Angular UI)
```

| Rule | Status |
|------|--------|
| `commons/` → `security/` or `relational/` | ❌ **Forbidden** |
| `config/` → business Controller/Service method calls | ❌ **Forbidden** |
| `security/` ↔ `relational/` | ❌ **Forbidden cross-dependency** |
| `security/` → `commons/` | ✅ Allowed |
| `relational/` → `commons/` | ✅ Allowed |
| Any module → `ContextUtils` | ✅ Allowed |

---

## 7. Data Model

All tables prefixed `se_`. Common columns: `code` (UUIDv7 PK), `version` (optimistic lock), `tenant_code` (multi-tenant isolation), `extend` (JSONB extension), `created_by/updated_by` (UUID), `created_at/updated_at` (TIMESTAMPTZ), `text_search` (tsvector GIN index, zhparser Chinese tokenizer).

| Table | Key columns | Notes |
|-------|--------------|-------|
| `se_users` | `username`, `password`, `phone`, `email`, `name`, `disabled`, `account_expired/locked`, `credentials_expired`, `login_time` | User table |
| `se_authorities` | `user_code` (FK), `authority` | Direct user authorities |
| `se_groups` | `code` (parent group), `name`, `description` | User groups (hierarchy) |
| `se_group_authorities` | `group_code` (FK), `authority` | Group authorities |
| `se_group_members` | `group_code` (FK), `user_code` (FK) | Group members |
| `se_tenants` | `code` (parent tenant), `name`, `description` | Tenants (hierarchy; id is `serial` not `BIGSERIAL`) |
| `se_tenant_members` | `tenant_code` (FK), `user_code` (FK), `enabled` | Tenant members |
| `se_menus` | `code`, `type` (FOLDER/MENU/LINK/API), `authority`, `name`, `path` | Menus (hierarchy + type enum) |
| `se_loggers` | `prefix`, `operator`, `status`, `method`, `url`, `context` (JSONB) | Audit logs (written by `LoggerFilter` event) |
| `se_dictionaries` | `code`, `dict_type`, `dict_key`, `dict_value`, `dict_label`, `description`, `sort_no`, `enabled` | Data dictionary (`tenant_code + dict_type + dict_key` unique constraint) |
| `oauth2_authorized_client` | `client_registration_id`, `principal_name`, `access_token_*` | OAuth2 token storage |

**Note**: `se_tenants.id` uses `serial` (not `BIGSERIAL`), unlike other tables.

---

## 8. API Contract

### Auth API (`/oauth2/**`)

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `GET` | `/oauth2/login` | Basic Auth / Session | Login, returns `AuthenticationToken` |
| `GET` | `/oauth2/csrf` | Session | Get CSRF Token |
| `GET` | `/oauth2/bind` | Session | OAuth2 bind query |
| `POST` | `/oauth2/change/password` | Session | Change password `{password, newPassword}` |
| `POST` | `/oauth2/logout` | Session | Logout + Clear-Site-Data |

### Security business API (`/sec/**`)

| Method | Path | Notes |
|--------|------|-------|
| `GET` | `/sec/users/search` | Search users |
| `GET` | `/sec/users/page` | Paginated query |
| `POST` | `/sec/users` | Create user |
| `PUT` | `/sec/users` | Update user |
| `DELETE` | `/sec/users/{code}` | Delete user |
| `*` | `/sec/users/authorities/**` | User authorities |
| `*` | `/sec/groups/**` | User group CRUD |
| `*` | `/sec/groups/authorities/**` | Group authorities |
| `*` | `/sec/groups/members/**` | Group members |
| `*` | `/sec/tenants/**` | Tenant CRUD |
| `*` | `/sec/tenants/members/**` | Tenant members |
| `GET` | `/sec/captcha/code` | Captcha (unauthenticated) |

### Relational business API (`/rel/**`)

| Method | Path | Notes |
|--------|------|-------|
| `*` | `/rel/dictionaries/**` | Dictionary management |
| `GET` | `/rel/loggers/**` | Audit-log query |
| `*` | `/rel/menus/**` | Menu management |

**Request requirements**: POST/PUT/DELETE must carry Cookie `SESSION` + Header `X-XSRF-TOKEN`. API version is controlled via `x-api-version` Header or `apiVersion` Query param (default `v1`).

> ✅ **Verified** (from `application.yml` + `WebConfiguration.configurePathMatching`): path prefixes are `rel → com.plate.boot.relational` and `sec → com.plate.boot.security` — i.e. `/rel/**` and `/sec/**`, **no `/v1` in the path**. Prefixes are data-driven via `WebfluxProperties.pathPrefixes` (`application.yml` → `spring.webflux.properties.path-prefixes`), not hard-coded. Any `/rel/v1`//`/sec/v1` reference in `boot/AGENTS.md` is outdated.

---

## 9. Code Conventions

### Backend

| Rule | Notes |
|------|-------|
| **JSON** | Always use `ContextUtils.OBJECT_MAPPER`; **forbidden** `new ObjectMapper()` |
| **Primary key** | `ContextUtils.nextId()` (UUIDv7) |
| **Current user** | `ContextUtils.securityDetails()` → `Mono<SecurityDetails>`; **forbidden** `SecurityContextHolder` |
| **Event publish** | `ContextUtils.eventPublisher(AbstractEvent)` |
| **Dynamic SQL** | Use `QueryFragment`/`QueryHelper`/`QueryJsonHelper`; **forbidden** string concatenation |
| **Service base** | Extend `AbstractCache` for `queryWithCache()`/`countWithCache()` (Redis prefix `plate:caches:`, TTL 10min) |
| **Reactive** | Controllers must return `Mono<T>`/`Flux<T>`; **forbidden** blocking IO |
| **DTO** | `*Req` request DTO, `*Res` response DTO (**forbidden** to expose password; `UserRes` masks phone/email) |
| **Hierarchy** | Group/Tenant/Dictionary/Menu all use `code` (parent node code) |
| **Path prefix** | `/rel/` → relational package, `/sec/` → security package (auto-bound by `WebConfiguration`) |
| **Authorization** | `@PreAuthorize("hasRole('...')")`; admin role constant `ContextUtils.RULE_ADMINISTRATORS` (see note below) |
| **DI** | Lombok `@RequiredArgsConstructor` + `final` fields |
| **Logging** | `@Log4j2` annotation; **forbidden** `System.out` or Logback |
| **Password** | `DelegatingPasswordEncoder` (default bcrypt) |
| **Cache threshold** | Objects over `HttpCodecsProperties.maxInMemorySize` (256KB) are not cached |
| **Concurrent session** | Single user single session (`SessionLimit.of(1)`); later login evicts earlier |

> ✅ **Verified** (from `ContextUtils.java:50`): the constant is `public final static String RULE_ADMINISTRATORS = "ROLE_SYSTEM_ADMINISTRATORS";` — the **field name** is `RULE_ADMINISTRATORS` (historical typo, kept for compatibility; do not rename), and its **value** is `ROLE_SYSTEM_ADMINISTRATORS`. Reference it as `ContextUtils.RULE_ADMINISTRATORS` in code.

### Frontend

| Rule | Notes |
|------|-------|
| **Components** | Standalone components; do NOT set `standalone: true` |
| **DI** | `inject()` function, not constructor injection |
| **State** | Signals (`signal()`, `computed()`, `input()`, `output()`) |
| **Templates** | Native control flow `@if`/`@for`/`@switch`, not structural directives |
| **HTTP** | Angular HttpClient; interceptor auto-handles XSRF + auth |
| **Routing** | Lazy loading (`loadChildren`), component input bindings |
| **Styling** | SCSS + @tabler/core |
| **Testing** | Vitest (`@angular/build:unit-test`), files `*.spec.ts` |
| **Host bindings** | Use `host` object, not `@HostBinding`/`@HostListener` |
| **Signal ops** | Use `update`/`set`, not `mutate` |
| **Accessibility** | Must pass AXE; meet WCAG 2.1 AA (focus, contrast, ARIA) |
| **Images** | Use `NgOptimizedImage` (not for inline base64) |

---

## 10. Flyway Migrations

Script location: `boot/platform/src/main/resources/db/migration/`

| Script | Content |
|--------|---------|
| `V1.0.0__Baseline.sql` | Baseline |
| `V1.0.1__Extension.sql` | PostgreSQL extensions (uuid-ossp, pg_trgm, zhparser) |
| `V1.0.2__Schema.sql` | Create all `se_*` tables + `updated_at` trigger function |
| `V1.0.3__Data.sql` | Initial data |
| `V1.0.4__InitTestData.sql` | Test data + `.conf` config files |
| `V1.0.5__Dictionary.sql` | Dictionary table structure |
| `V1.0.6__DictionaryData.sql` | Dictionary initial data |

**Rules**: `baseline-on-migrate: true`, `baseline-version: 1.0.0`. New tables require a new `V1.x.y__*.sql`. **Strictly forbidden** to modify existing migration files (Flyway checksum validation).

---

## 11. Testing

### Backend

- **Integration tests**: `ApplicationTests.java` (21 tests, `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `WebTestClient`)
- **Unit tests**: `SecurityManagerTest.java`, `SecurityControllerTest.java` (Mockito + StepVerifier)
- **Infrastructure**: `InfrastructureConfiguration.java` (Testcontainers: `alexbob/postgres` + zhparser, Redis)
- **Test config**: `src/test/resources/application.yml` (cache TTL 5min, maxInMemorySize 10MB, debug logging)
- **Prerequisite**: Docker daemon must be running

```bash
cd boot
./gradlew :platform:test                            # All tests
./gradlew :platform:test --tests "*ClassName*"       # Single class
./gradlew :platform:test --tests "*MethodName*"      # Single method
```

### Frontend

```bash
cd ui/ng-plate
pnpm test            # Vitest unit tests
```

---

## 12. Configuration Properties Cheat-Sheet

| Property | Value | Notes |
|----------|-------|-------|
| `server.port` | `8080` | HTTP port |
| `server.http2.enabled` | `true` | HTTP/2 |
| `spring.threads.virtual.enabled` | `true` | Java virtual threads |
| `spring.session.timeout` | `8H` | Session validity |
| `spring.cache.redis.key-prefix` | `plate:caches:` | Cache prefix |
| `spring.cache.redis.time-to-live` | `10m` | Cache TTL (5min in tests) |
| `spring.http.codecs.max-in-memory-size` | `256KB` | In-memory buffer cap (10MB in tests) |
| `spring.r2dbc.pool.max-size` | `64` | Connection pool cap |
| `spring.jackson.time-zone` | `GMT+8` | Timezone |
| `spring.jackson.locale` | `zh_CN` | Locale |

---

## 13. CI/CD

GitHub Actions workflows (`.github/workflows/`):

| File | Trigger | Responsibility |
|------|---------|----------------|
| `gradle-tests.yml` | push to `main`/`dev` | Runs `./gradlew test` (JDK 25 Liberica) |
| `gradle-build.yml` | push to `main`/`dev` + tags `v*` + releases created | Multi-arch OCI image (amd64+arm64) via Buildpacks → GHCR + Docker Hub |
| `cleanup-caches.yml` | PR close | Clean PR caches |

Image publish: `ghcr.io/<actor>/plate-platform` and `docker.io/alexbob/plate-platform`

> Note: there is **no frontend quality workflow** in this repo. CI only builds/tests the backend (`.github/workflows/gradle-*.yml`). Do not add ESLint/coverage/lint CI gates unless explicitly requested.

---

## 14. Environment Requirements

| Component | Version | Notes |
|----------|---------|-------|
| Java | 25+ | Liberica JDK recommended |
| Gradle | 9.5+ | Wrapper included |
| PostgreSQL | 17+ | uuid-ossp, pg_trgm, zhparser extensions |
| Redis | 7.0+ | Cache + WebSession |
| Docker | Latest | Testcontainers tests |
| Node.js | LTS | Frontend dev |
| pnpm | 11.12.0 | Frontend package manager |

---

## 15. Related Documents

| Document | Path | Notes |
|----------|------|-------|
| Project overview | `CLAUDE.md` | Claude Code project overview guide |
| Backend-specific | `boot/AGENTS.md` | Backend tech stack, package structure, security model, DB schema |
| Frontend coding standards | `ui/ng-plate/AGENTS.md` | Angular/TypeScript best practices (components, templates, state, accessibility) |
| Project README | `README.md` | Features, quick start, Docker deployment |
| Backend README | `boot/README.md` / `boot/README_CH.md` | Backend deployment docs (EN/ZH) |
