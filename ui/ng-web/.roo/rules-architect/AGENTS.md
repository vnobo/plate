# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Architecture Rules (Non-Obvious Only)

- Zoneless change detection is used throughout the application instead of traditional zones (requires `provideZonelessChangeDetection()`)
- Client hydration uses `withEventReplay()` option in app config (important for event handling)
- Tabler CSS framework is integrated with specific bundle names in angular.json that must be preserved
- Build process generates both client and server bundles for SSR (affects deployment strategy)
- Providers must be stateless - hidden caching layer assumes this architectural pattern
