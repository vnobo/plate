# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Architecture Rules (Non-Obvious Only)

- Zoneless change detection is used throughout the application instead of traditional zones
- Client hydration uses `withEventReplay()` option in app config
- Tabler CSS framework is integrated with specific bundle names in angular.json
- Build process generates both client and server bundles for SSR
