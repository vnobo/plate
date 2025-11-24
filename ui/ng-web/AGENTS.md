# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Lint/Test Commands

- Run single test (no watch): `ng test --watch=false`
- Serve SSR app: `npm run serve:ssr:ng-web`

## Critical Patterns

- Zoneless change detection: `provideZonelessChangeDetection()` in app config
- Client hydration: `withIncrementalHydration()` + `withI18nSupport()` (avoid `withEventReplay()`)
- Tabler CSS: Bundle names must be preserved in angular.json (tabler-style, tabler-themes-style, tabler-socials-style)
- Auth tokens: Base64 URI encoded in session storage using `btoa(encodeURIComponent(json))` and `decodeURIComponent(atob(str))`
- API: Auto-add 'x-api-version: v1' header via interceptor in `src/app/core/net/http.Interceptor.ts`
- Progress bar: 500ms debounce on visibility changes in `ProgressService`
- HTTP timeouts: 5s first request, 10s subsequent requests in interceptor
- Environment config: Development uses `/api` proxy, production uses empty host with path rewriting
- API paths: Development uses `/rela` and `/sec`, production uses `/rel` and `/sec`
- Locale: Chinese locale (`zh_CN`) configured with dayjs for date handling
- SSR session storage: Platform checks required for SSR compatibility
- Route guards: Use functional guards with `inject()` approach
- Component input binding: Enabled via `withComponentInputBinding()` in router config
