# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Test Commands (Non-Obvious)

**Backend (Spring Boot):**
- Tests must be run from `boot/` directory, not project root: `cd boot && ./gradlew test`
- Integration tests require `InfrastructureConfiguration` for test containers setup
- Test data is initialized via Flyway migrations in `V1.0.4__InitTestData.sql`

**Frontend (Angular):**
- Development server uses proxy config (`proxy.conf.json`) for API calls to backend
- SSR builds output to `dist/web/server/server.mjs` - serve with `npm run serve:ssr:web`
- Tabler icons are automatically copied to assets during build via angular.json configuration

## Code Style & Architecture (Non-Obvious)

**Backend Patterns:**
- All reactive programming using Spring WebFlux - no blocking operations allowed
- Database queries use custom `QueryFragment` utility instead of raw SQL
- UUID primary keys are used throughout (not auto-increment IDs)
- API endpoints use path prefixes: `/rel/` for relational data, `/sec/` for security
- Custom caching layer in `AbstractCache` - must call `queryWithCache()` for database operations

**Frontend Patterns:**
- Uses Angular signals extensively instead of traditional observables
- HTTP interceptor automatically adds API version header and handles 401 redirects
- Modal system uses `inputBinding()` for data passing to modal components
- Theme attributes are set programmatically on parent element in `App` component

## Critical Gotchas

- Backend uses R2DBC (reactive) with PostgreSQL - no JPA/Hibernate
- Frontend API paths are relative (`/sec/oauth2/login`) not absolute URLs
- Test users: admin/123456 and user/123456 (see test data migration)
- OAuth2 configuration is in application.yml but requires external provider setup
- Database migrations run via Flyway but require JDBC connection (not R2DBC)