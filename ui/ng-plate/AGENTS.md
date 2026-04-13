# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Lint/Test Commands

- SSR server start: `npm run serve:ssr:ng-plate` (after build, uses Node to serve server.mjs)

## Code Style Guidelines

- HTTP interceptors are chained: `indexInterceptor` combines multiple interceptors (defaultInterceptor, apiVersionInterceptor, handleErrorInterceptor)
- Custom environment files in src/envs/ (env.ts and env.dev.ts)

## Critical Patterns

- Multiple HTTP interceptors are used in sequence: defaultInterceptor adds base URL and loading indicator, apiVersionInterceptor adds x-api-version header, handleErrorInterceptor handles 401 redirects
- Environment files are aliased as @envs/env (not standard Angular pattern)
- Custom inputBinding('inputData', userSignal) pattern for modal data passing
- Tabler CSS framework is used for styling (not Material or Bootstrap)
- Private service properties use underscore prefix: `_message`, `_modal`, `_http`
- 401 errors trigger navigation to login URL via TokenService
- API timeout: 5s first request, 10s subsequent requests
