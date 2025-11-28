# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Lint/Test Commands

- Run single test: `ng test` (uses Vitest as seen in devDependencies)
- SSR server start: `npm run serve:ssr:ng-plate` (after build, uses Node to serve server.mjs)

## Code Style Guidelines

- [https://angular.dev/style-guide](https://angular.dev/style-guide)
- Zoneless change detection is enabled: `provideZonelessChangeDetection()` in app.config.ts
- HTTP interceptors are chained: `indexInterceptor` combines multiple interceptors (defaultInterceptor, apiVersionInterceptor, handleErrorInterceptor)
- Custom environment files in src/envs/ (env.ts and env.dev.ts)

## Import Conventions

- Use `inject()` function instead of constructor injection for services
- Import from `@app/core`, `@app/plugins`, `@plate/types` for internal modules
- Import from `@envs/env` for environment configuration
- Use signal-based state management with `signal()` and `computed()`
- Prefer standalone components over NgModules

## Type & Naming Conventions

- Use `ChangeDetectionStrategy.OnPush` for all components
- Private service properties use underscore prefix: `_message`, `_modal`, `_http`
- Chinese locale is set to 'zh-Hans' via LOCALE_ID provider
- API timeout: 5s first request, 10s subsequent requests
- Custom Page/Pageable types for backend pagination integration

## Error Handling Patterns

- 401 errors trigger navigation to login URL via TokenService
- API requests use ProgressService for loading indicators
- XSRF protection configured with custom cookie/header names
- Service Worker enabled only in production builds

## Critical Patterns

- Multiple HTTP interceptors are used in sequence: defaultInterceptor adds base URL and loading indicator, apiVersionInterceptor adds x-api-version header, handleErrorInterceptor handles 401 redirects
- Environment files are aliased as @envs/env (not standard Angular pattern)
- Custom inputBinding('inputData', userSignal) pattern for modal data passing
- Tabler CSS framework is used for styling (not Material or Bootstrap)
- Day.js with Chinese locale for date/time handling
