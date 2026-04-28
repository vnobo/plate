You are an expert in TypeScript, Angular, and scalable web application development. You write functional, maintainable, performant, and accessible code following Angular and TypeScript best practices.

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
- Set `changeDetection: ChangeDetectionStrategy.OnPush` in `@Component` decorator
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
- Use the `providedIn: 'root'` option for singleton services
- Use the `inject()` function instead of constructor injection

# ng-web — Agent Guide

Angular 21 SSR app. Minimal scaffold, not the production app (that's `ui/ng-plate/`).

## Commands (from this directory)

- Dev server: `pnpm start` (port 4200)
- Build: `pnpm build`
- Test: `pnpm test` (Vitest via `@angular/build:unit-test`, NOT Jasmine/Karma)
- SSR serve: `pnpm serve:ssr:ng-web` (port 4000)
- Lint/format: Prettier only — no ESLint configured. Run `npx prettier --check .`

## Angular 21 specifics

- Standalone components are the default. Do NOT add `standalone: true`.
- Use `input()` / `output()` signal functions, not `@Input` / `@Output` decorators.
- Use `computed()` for derived state.
- Use native control flow (`@if`, `@for`, `@switch`) — no `*ngIf` / `*ngFor`.
- Host bindings go in the `host` object inside `@Component` — no `@HostBinding` / `@HostListener`.
- Use `inject()` instead of constructor injection.
- Use `class` / `style` bindings directly — no `ngClass` / `ngStyle`.
- `NgOptimizedImage` for static images (not inline base64).
- Always set `changeDetection: ChangeDetectionStrategy.OnPush`.

## Prettier / EditorConfig

- 100 char print width, single quotes, 2-space indent.
- HTML uses `angular` parser (set in `.prettierrc` overrides).

## SSR

- Entry: `src/server.ts` — Express 5 serving Angular via `@angular/ssr/node`.
- Default SSR port: 4000 (overridable via `PORT` env var).
- Browser build output: `dist/ng-web/browser/`.
- Server build output: `dist/ng-web/server/`.

## Testing

- Only test file currently: `src/app/app.spec.ts`.
- Tests use `TestBed.configureTestingModule({ imports: [App] })` — import standalone components directly.
- Expect Vitest globals (`describe` / `it` / `expect`) — no Jasmine needed.

## Project structure

```
src/
├── main.ts              # Browser bootstrap
├── main.server.ts       # Server bootstrap
├── server.ts            # Express SSR server
├── index.html
├── styles.scss          # Global styles
└── app/
    ├── app.ts           # Root component (selector: app-root)
    ├── app.config.ts    # Browser providers (router, hydration)
    ├── app.config.server.ts
    ├── app.routes.ts    # Route definitions (currently empty)
    ├── app.routes.server.ts
    └── app.spec.ts      # Root component test
```

## Monorepo context

- This repo (`E:\workspace\plate`) is a monorepo: `boot/` (Spring Boot 4 + Java 25), `ui/ng-plate/` (production Angular), `ui/ng-web/` (this scaffold).
- Package manager: pnpm 10.x. Never use npm or yarn.
- Root `AGENTS.md` has backend commands and monorepo structure.
