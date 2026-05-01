# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Lint/Test Commands

- SSR server start: `npm run serve:ssr:ng-plate` (after build, uses Node to serve server.mjs)
- Testing uses Vitest (not Jasmine/Karma): `npm test` - test files named `*.spec.ts`

## Critical Patterns

- HTTP interceptors are chained in array: `export const indexInterceptor = [defaultInterceptor, apiVersionInterceptor, handleErrorInterceptor]` (src/app/core/net/http.Interceptor.ts)
- defaultInterceptor: adds base URL, shows loading indicator, timeout: 5s first request, 10s subsequent requests
- handleErrorInterceptor: 401 errors trigger navigation to `/passport/login` via TokenService
- Environment files: `src/envs/env.ts` (prod) / `src/envs/env.dev.ts` (dev), aliased as `@envs/env`, replaced via angular.json fileReplacements
- Path aliases: `@app/*` → src/app/, `@envs/*` → src/envs/, `@styles/*` → src/styles/ (defined in tsconfig.app.json ONLY, not root tsconfig)
- ModalsService uses `inputBinding('inputData', userSignal)` pattern for modal data passing (src/app/plugins/modals.ts)
- Tabler CSS framework for styling (bundled via angular.json styles/scripts - three CSS bundles, two JS bundles)
- Private service properties use underscore prefix: `_loading`, `_storage`, `_el`, `_auth`, `_route`
- TokenService stores auth in sessionStorage with `btoa(encodeURIComponent(jsonStr))` encoding
- Proxy config: `proxy.conf.json` used for development API requests (active only in development mode)
