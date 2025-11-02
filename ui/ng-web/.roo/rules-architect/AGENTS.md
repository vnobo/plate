# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Architecture Rules (Non-Obvious Only)

- Authentication tokens use base64 + URI encoding pattern (non-standard security approach)
- Progress bar service implements 500ms debounce for perceived performance optimization
- Toast component architecture uses dynamic creation with manual cleanup lifecycle
- Modal service architecture uses `inputBinding()` pattern for component data passing
- Theme service architecture involves dynamic CSS loading with DOM class management
- User pagination architecture hides 1-based to 0-based conversion complexity
- Session storage architecture includes platform abstraction for SSR compatibility
- API interceptor architecture automatically injects version headers system-wide
- Tabler CSS integration architecture requires specific bundle naming conventions
- Route guard architecture uses functional injection pattern over class-based guards
