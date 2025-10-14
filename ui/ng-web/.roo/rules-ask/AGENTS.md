# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Documentation Rules (Non-Obvious Only)

- Zoneless change detection is a key architectural decision that affects all components (not traditional Angular zones)
- Client hydration uses `withEventReplay()` option in app config (important for understanding event handling)
- Tabler CSS integration is done through specific bundle names in angular.json that must be preserved
- Component tests must include zoneless change detection provider (use `provideZonelessChangeDetection()`)
- SSR means there are two separate code paths - client and server - to consider when reviewing code
