# Project Debug Rules (Non-Obvious Only)

- Backend tests require running from `boot/` directory, not project root
- Integration tests need `InfrastructureConfiguration` for test containers setup
- Database migrations use Flyway but require JDBC connection (not R2DBC)
- Test users are pre-configured: admin/123456 and user/123456
- Reactive streams debugging requires understanding of Mono/Flux chains
- Frontend development server uses proxy config for API calls to backend
- SSR debugging requires checking `dist/web/server/server.mjs` output
- HTTP interceptor automatically handles 401 redirects to login page
- Theme debugging requires checking parent element attributes set by `App` component