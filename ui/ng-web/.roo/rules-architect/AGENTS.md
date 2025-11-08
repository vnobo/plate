# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Architecture Rules (Non-Obvious Only)

- HTTP interceptor architecture uses different timeouts: 5s first request, 10s subsequent requests
- Environment config architecture: Development uses `/api` proxy, production uses empty host with path rewriting
- API path architecture differs between environments: `/rela` (dev) vs `/rel` (prod) for rela API
- Internationalization architecture: Chinese locale (`zh_CN`) configured with dayjs for date handling
- Component input binding architecture enabled globally via `withComponentInputBinding()` in router config
- Change detection architecture: Zoneless enabled via `provideZonelessChangeDetection()` in app config
- Client hydration architecture uses `withIncrementalHydration()` and `withI18nSupport()` (not `withEventReplay()`)
- CSS integration architecture: Tabler bundles require specific bundleName properties: tabler-style, tabler-themes-style, tabler-socials-style
- Storage architecture: Session storage includes platform checks for SSR compatibility
- Route guard architecture uses functional `inject()` pattern instead of constructor injection
