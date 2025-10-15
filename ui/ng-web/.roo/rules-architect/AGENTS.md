# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Architecture Rules (Non-Obvious Only)

- Zoneless change detection is used throughout the application instead of traditional zones (requires `provideZonelessChangeDetection()`)
- Client hydration uses `withEventReplay()` option in app config (important for event handling)
- Tabler CSS framework is integrated with specific bundle names in angular.json that must be preserved
- Build process generates both client and server bundles for SSR (affects deployment strategy)
- Providers must be stateless - hidden caching layer assumes this architectural pattern
- The application uses Angular v20+ with standalone components as the default pattern (no NgModules)
- Signals are used for state management instead of traditional properties (modern Angular approach)
- Built-in control flow (`@if`, `@for`, `@switch`) is used instead of structural directives (`*ngIf`, `*ngFor`, etc.)
- All static images should use the `NgOptimizedImage` directive for performance optimization
- Components use `input()` and `output()` functions instead of decorators for inputs/outputs
