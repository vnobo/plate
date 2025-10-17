# Project Coding Rules (Non-Obvious Only)

- Always use `QueryFragment` from `boot/platform/src/main/java/com/plate/boot/commons/query/QueryFragment.java` for database queries (raw SQL will fail)
- Database operations must use `queryWithCache()` from `AbstractCache` for proper caching
- All reactive operations must use Spring WebFlux - no blocking operations allowed
- UUID primary keys are mandatory - no auto-increment IDs
- API endpoints follow strict path prefixes: `/rel/` for relational data, `/sec/` for security
- Use `inputBinding()` for passing data to modal components in Angular
- Theme attributes must be set programmatically on parent element (see `App` component)
- HTTP interceptor automatically adds API version header - don't add manually
- Tabler icons are automatically copied to assets - don't reference them directly