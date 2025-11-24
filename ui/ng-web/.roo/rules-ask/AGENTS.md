# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Documentation Rules (Non-Obvious Only)

- HTTP interceptor uses different timeouts: 5s first request, 10s subsequent requests
- Environment config: Development uses `/api` proxy, production uses empty host with path rewriting
- API paths differ between environments: `/rela` (dev) vs `/rel` (prod) for rela API
- Chinese locale (`zh_CN`) configured with dayjs for date handling throughout the app
- Component input binding enabled globally via `withComponentInputBinding()` in router config
- Zoneless change detection enabled via `provideZonelessChangeDetection()` in app config
- Client hydration uses `withIncrementalHydration()` and `withI18nSupport()` (not `withEventReplay()`)
- Tabler CSS bundles require specific bundleName properties: tabler-style, tabler-themes-style, tabler-socials-style
- Session storage service includes platform checks for SSR compatibility
- Route guards use functional `inject()` pattern instead of constructor injection
