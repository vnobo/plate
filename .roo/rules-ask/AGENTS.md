# Project Documentation Rules (Non-Obvious Only)

- Backend uses reactive Spring WebFlux architecture - no traditional servlet model
- Database access uses R2DBC (reactive) with PostgreSQL, not JPA/Hibernate
- Frontend uses Angular signals extensively instead of traditional observables
- API endpoints are organized by path prefixes: `/rel/` for business logic, `/sec/` for security
- Modal system uses `inputBinding()` for data passing between components
- Theme system sets attributes programmatically on parent element in `App` component
- Tabler icons are automatically copied to assets during build process
- Test data is initialized via Flyway migrations, not application code
- OAuth2 configuration requires external provider setup beyond basic config