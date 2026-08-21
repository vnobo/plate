You are an expert in TypeScript, Angular, and scalable web application development. You write functional, maintainable, performant, and accessible code following Angular and TypeScript best practices.

## Project Workflow

- Use `pnpm@11.21.0`; keep `package.json` and `pnpm-lock.yaml` synchronized when dependencies change.
- The project uses Angular 22, standalone components, signals, `computed()`, `linkedSignal()`, `inject()`, and native `@if`/`@for` control flow.
- Organize pages by business domain under `src/app/pages`. Keep top-level routes in `src/app/app.routes.ts` and domain routes near their feature pages; prefer lazy loading.
- Put cross-cutting services and storage adapters in `src/app/core`, layouts in `src/app/layout`, reusable UI plugins in `src/app/plugins`, and shared primitives in `src/app/shared`.
- Reuse the existing HTTP interceptor and environment configuration for API requests. Do not duplicate the API host, timeout, or version headers in individual services.
- Reuse `ModalsService` for dynamic forms and preserve the existing output-based refresh flow when changing modal inputs or results.
- Signal Forms are already used by feature forms; follow the local pattern instead of introducing template-driven forms.

## Code Update And Synchronization

- After ordinary source changes, run `pnpm exec ng build`.
- After changing tests or component behavior, run `pnpm test -- --watch=false` in addition to the build.
- After changing routes, shared services, interceptors, layouts, modals, environment files, SSR, or dependencies, run both checks and manually verify the affected page with `pnpm start`.
- When changing dependencies, build configuration, Angular versions, or test tooling, update the relevant documentation in `README.md` and keep the lockfile in sync.
- When adding a page, update its feature route and the corresponding route index/export when the local feature pattern requires it.
- Before considering a change synchronized, inspect the diff, confirm no generated `dist/` output or local environment files were added, and report any unrelated pre-existing changes instead of reverting them.
- Direct use of `document`, `window`, browser storage, Tabler globals, or `confirm` must remain safe for SSR and tests; interactive elements must also retain keyboard and focus accessibility.
- A guard is not active merely because it exists: verify that it is attached to the relevant route before relying on it for protection.

## TypeScript Best Practices

- Use strict type checking
- Prefer type inference when the type is obvious
- Avoid the `any` type; use `unknown` when type is uncertain

## Angular Best Practices

- Always use standalone components over NgModules
- Must NOT set `standalone: true` inside Angular decorators. It's the default in Angular v20+.
- Use signals for state management
- Implement lazy loading for feature routes
- Do NOT use the `@HostBinding` and `@HostListener` decorators. Put host bindings inside the `host` object of the `@Component` or `@Directive` decorator instead
- Use `NgOptimizedImage` for all static images.
  - `NgOptimizedImage` does not work for inline base64 images.

## Accessibility Requirements

- It MUST pass all AXE checks.
- It MUST follow all WCAG AA minimums, including focus management, color contrast, and ARIA attributes.

### Components

- Keep components small and focused on a single responsibility
- Use `input()` and `output()` functions instead of decorators
- Use `computed()` for derived state
- Prefer inline templates for small components
- Prefer Reactive forms instead of Template-driven ones
- Do NOT use `ngClass`, use `class` bindings instead
- Do NOT use `ngStyle`, use `style` bindings instead
- When using external templates/styles, use paths relative to the component TS file.

## State Management

- Use signals for local component state
- Use `computed()` for derived state
- Keep state transformations pure and predictable
- Do NOT use `mutate` on signals, use `update` or `set` instead

## Templates

- Keep templates simple and avoid complex logic
- Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- Use the async pipe to handle observables
- Do not assume globals like (`new Date()`) are available.

## Services

- Design services around a single responsibility
- Use Angular's `@Injectable({ providedIn: 'root' })` for singleton services
- Use the `inject()` function instead of constructor injection
