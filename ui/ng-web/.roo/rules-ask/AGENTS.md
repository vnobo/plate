# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Documentation Rules (Non-Obvious Only)

- Zoneless change detection is a key architectural decision that affects all components
- Client hydration uses `withEventReplay()` option in app config
- Tabler CSS integration is done through specific bundle names in angular.json
- Component tests must include zoneless change detection provider
