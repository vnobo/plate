# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Lint/Test Commands

- Run single test: `ng test --watch=false` (to run once instead of watch mode)
- Serve SSR app: `npm run serve:ssr:ng-web` (after building)

## Code Style Guidelines

- Prettier: printWidth: 10, singleQuote: true, Angular parser for HTML files
- TypeScript: strict mode, experimental decorators enabled, target ES2022
- SCSS styling (not CSS/LESS)

## Critical Patterns

- Zoneless change detection is used throughout the application (use `provideZonelessChangeDetection()`)
- SSR is enabled with server entry at src/server.ts
- Tabler CSS framework integration with specific bundle names in angular.json
- Client hydration uses `withEventReplay()` option in app config
- Tests must use zoneless change detection provider
