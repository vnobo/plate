# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Coding Rules (Non-Obvious Only)

- Authentication tokens use base64 + URI encoding - see TokenService.login() method
- API interceptor automatically adds 'x-api-version: v1' header to all requests
- Progress bar service has 500ms debounce - operations may appear to complete before UI updates
- Toast component uses manual cleanup via `toastsDropped` event - memory leak risk if not handled
- Modal service uses non-standard `inputBinding()` pattern for data passing
- Theme service dynamically loads CSS files and manages DOM classes manually
- Session storage includes platform checks - must use for SSR compatibility
- User pagination converts 1-based UI to 0-based API calls (see Users.page() method)
- Route guards use functional `inject()` pattern, not constructor injection
- Tabler CSS bundles have specific bundleName properties that must be preserved
