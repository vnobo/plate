# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Debug Rules (Non-Obvious Only)

- Zoneless change detection means traditional zone debugging won't work - use signal-based debugging
- Toast component manual cleanup failure causes memory leaks - check `toastsDropped` event handling
- Progress bar 500ms debounce makes fast operations appear delayed - not a performance issue
- Authentication token encoding issues cause silent failures - check base64 + URI encoding in TokenService
- API version header 'x-api-version: v1' is automatically added - missing header indicates interceptor failure
- Theme CSS loading failures are silent - check DOM for dynamically added link elements
- Modal `inputBinding()` pattern can fail silently - verify data passing in modal creation
- User pagination 1-based to 0-based conversion bugs cause off-by-one errors in API calls
- Session storage platform checks may hide browser-only API errors during SSR
- Tabler CSS bundleName mismatches cause styling failures without console errors
