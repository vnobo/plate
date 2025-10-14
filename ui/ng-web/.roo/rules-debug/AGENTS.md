# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Debug Rules (Non-Obvious Only)

- Zoneless change detection means traditional zone-based debugging won't work
- Client hydration uses `withEventReplay()` option which may affect debugging
- SSR server runs on a separate process, requiring different debugging approach
- Angular Universal debugging requires checking both client and server logs
