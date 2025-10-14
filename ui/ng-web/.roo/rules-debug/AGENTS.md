# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Debug Rules (Non-Obvious Only)

- Zoneless change detection means traditional zone-based debugging won't work (use zoneless debugging approaches)
- Client hydration uses `withEventReplay()` option which may affect debugging of event handling
- SSR server runs on a separate process, requiring different debugging approach (check server.ts)
- Angular Universal debugging requires checking both client and server logs (dual debugging context)
- Browser-only APIs may cause silent failures during SSR - check for proper platform checks
