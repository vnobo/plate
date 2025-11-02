# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Lint/Test Commands

- Run single test: `ng test --watch=false` (to run once instead of watch mode)
- Serve SSR app: `npm run serve:ssr:ng-web` (after building)

## Critical Patterns

- Zoneless change detection is mandatory - use `provideZonelessChangeDetection()` in all tests and app config
- Client hydration uses `withEventReplay()` option in app config - affects event handling debugging
- Tabler CSS framework bundles have specific names in angular.json that must be preserved (bundleName property)
- Authentication tokens are base64 encoded with URI encoding in session storage (see TokenService)
- API requests automatically add 'x-api-version: v1' header via interceptor
- Progress bar has 500ms debounce time - may appear delayed during fast operations
- Toast messages use dynamic component creation with manual cleanup - memory leak risk if not properly destroyed
- Modal service uses `inputBinding()` for passing data to content components - non-standard pattern
- Theme switching loads CSS files dynamically and manages DOM classes manually
- Session storage service includes platform browser checks - SSR safe
- Route guards use functional approach with `inject()` instead of constructor injection
- User pagination uses 1-based indexing in UI but 0-based for API calls (see Users component)
