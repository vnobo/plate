# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Coding Rules (Non-Obvious Only)

- Always use `provideZonelessChangeDetection()` from '@angular/core' in component tests
- Client hydration uses `withEventReplay()` option in app config
- Tabler CSS bundles have specific names that must be preserved in angular.json
- Server-side rendering requires special consideration for browser-only APIs
