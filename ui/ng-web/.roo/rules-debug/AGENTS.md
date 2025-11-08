# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Debug Rules (Non-Obvious Only)

- Zoneless change detection means traditional zone debugging won't work - use signal-based debugging
- HTTP timeout debugging: 5s first request, 10s subsequent requests - check interceptor for timeout issues
- Environment-specific API path differences: `/rela` (dev) vs `/rel` (prod) can cause routing failures
- Chinese locale dayjs configuration may cause date formatting issues if not properly initialized
- Component input binding failures are silent - verify `withComponentInputBinding()` is working
- Client hydration debugging: uses `withIncrementalHydration()` not `withEventReplay()` - different behavior
- Tabler CSS bundleName mismatches cause styling failures without console errors
- Session storage platform checks may hide browser-only API errors during SSR
- Route guard functional injection pattern can fail silently if dependencies aren't available
- Progress bar 500ms debounce makes fast operations appear delayed - not a performance issue
