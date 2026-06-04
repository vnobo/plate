# Plate Platform — Backend

Spring Boot 4.0.6 reactive backend with WebFlux, R2DBC, and PostgreSQL.

## Tech Stack

- **Spring Boot** 4.0.6 (WebFlux, R2DBC, Security, Session)
- **Java** 25 (virtual threads enabled)
- **PostgreSQL** 17+ with reactive driver
- **Redis** 7.0+ for caching and sessions
- **Flyway** for database migrations
- **GraalVM** native image support (build tools 0.11.5)
- **Lombok** for boilerplate reduction
- **springdoc-openapi** for API documentation

## Build

```bash
./gradlew build          # compile + test
./gradlew bootRun        # run locally
./gradlew bootBuildImage # build Docker image
```

## Project Structure

```
boot/platform/src/main/java/com/plate/boot/
├── security/              # Authentication & authorization
│   ├── core/user/         # User CRUD, events
│   ├── core/group/        # Group management, authorities, members
│   ├── core/tenant/       # Multi-tenant management
│   ├── captcha/           # Captcha generation & validation
│   └── oauth2/            # OAuth2 success handler & user service
├── relational/            # Business logic
│   ├── menus/             # Menu CRUD with events
│   ├── logger/            # Audit logging with scheduled cleanup
│   └── dictionaries/      # Dictionary management
├── commons/               # Shared utilities
│   ├── base/              # AbstractEntity, AbstractCache, AbstractEvent
│   ├── query/             # QueryFragment, QueryHelper, QueryJsonHelper
│   ├── converters/        # R2DBC type converters
│   ├── exception/         # Global exception handler & custom exceptions
│   └── utils/             # BeanUtils, ContextUtils, DatabaseUtils
└── config/                # Spring configuration
    ├── SecurityConfiguration
    ├── R2dbcConfiguration
    ├── RedisConfiguration
    ├── SessionConfiguration
    └── WebConfiguration
```

## Configuration

Main config: `src/main/resources/application.yml`

Key features:
- R2DBC connection pool (64 max connections)
- Redis-based session (8h timeout)
- Flyway baseline migration
- API versioning via header (`x-api-version`)
- Path prefixes: `/sec/*` (security), `/rel/*` (relational)

## Testing

```bash
./gradlew test           # JUnit 5 + Testcontainers (PostgreSQL + Redis)
```

## API Docs

Swagger UI available at `/swagger-ui.html` when running.
