# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Documentation Rules (Non-Obvious Only)

- Authentication system uses base64 + URI encoding for token storage (non-standard approach)
- Progress bar debounce timing (500ms) affects perceived performance, not actual performance
- Toast component requires manual cleanup to prevent memory leaks (non-obvious lifecycle management)
- Modal service uses `inputBinding()` pattern instead of standard input properties
- Theme switching involves dynamic CSS loading and manual DOM class management
- User pagination has hidden 1-based to 0-based conversion logic in API calls
- Session storage service includes SSR platform checks that may hide browser API usage
- API interceptor automatically adds version header - not documented in API specs
- Tabler CSS integration requires specific bundleName properties in angular.json
- Route guards use functional injection pattern instead of class-based approach
