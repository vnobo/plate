# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Coding Rules (Non-Obvious Only)

- Always use `provideZonelessChangeDetection()` from '@angular/core' in component tests (not traditional zone testing)
- Client hydration uses `withEventReplay()` option in app config (affects event handling)
- Tabler CSS bundles have specific names that must be preserved in angular.json (bundleName property)
- Server-side rendering requires special consideration for browser-only APIs (use `isPlatformBrowser` checks)
- Use signals for state management instead of traditional properties (modern Angular pattern)
- When creating new components, always use standalone components with `ChangeDetectionStrategy.OnPush`
- Import pipes when used in templates and use built-in pipes where available (e.g., `@if`, `@for`, `@switch`)
- For image optimization, use `NgOptimizedImage` directive for all static images
- Use `input()` and `output()` functions instead of decorators for component inputs/outputs
- Use `computed()` for derived state instead of computed properties
