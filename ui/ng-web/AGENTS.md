# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Lint/Test Commands

- Run single test (no watch): `ng test --watch=false`
- Serve SSR app (after build): `npm run serve:ssr:ng-web`

## Critical Patterns

- Zoneless change detection: `provideZonelessChangeDetection()` in tests/config
- Client hydration: `withEventReplay()` in app config
- Tabler CSS: Preserve bundleName in angular.json
- Auth tokens: Base64 URI encoded in session storage
- API: Auto-add 'x-api-version: v1' header via interceptor
- Progress bar: 500ms debounce
- Toasts: Dynamic component cleanup
- Modals: `inputBinding()` for data
- Theme: Load CSS dynamically
- Session storage: Platform checks for SSR
- Route guards: `inject()` approach
- Pagination: 1-based UI, 0-based API
