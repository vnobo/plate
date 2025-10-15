# Project Architecture Rules (Non-Obvious Only)

- Reactive architecture is mandatory - all backend operations use Spring WebFlux
- Database layer uses R2DBC with custom `QueryFragment` utility instead of ORM
- Caching layer is built into `AbstractCache` - must use `queryWithCache()` for all DB operations
- API routing follows strict path-based organization: `/rel/` vs `/sec/` endpoints
- Frontend uses Angular signals as primary state management pattern
- Modal system requires `inputBinding()` for component communication
- Theme system sets attributes programmatically on DOM parent elements
- Tabler icons are automatically bundled during build via angular.json configuration
- Database migrations use Flyway but require JDBC (not R2DBC) for execution
- OAuth2 integration requires external provider configuration beyond basic setup