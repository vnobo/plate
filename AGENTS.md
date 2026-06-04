# AGENTS.md

## Monorepo Structure

- `boot/` — Spring Boot 4.0.0-M3 backend (Java 25, Gradle, PostgreSQL, Redis)
- `ui/ng-plate/` — production Angular app (Tabler UI, SSR, PWA, auth services)
- `ui/ng-web/` — fresh Angular 21 scaffold (minimal, placeholder)

## Build & Test Commands

Backend (from `boot/`):
- Build: `./gradlew build`
- Test: `./gradlew test`
- Boot run: `./gradlew bootRun`
- Native image: `./gradlew nativeCompile`

Frontend (from `ui/ng-plate/` or `ui/ng-web/`):
- Dev server: `pnpm start`
- Build: `pnpm build`
- Test: `pnpm test` (Vitest, NOT Jasmine/Karma)
- SSR serve: `pnpm serve:ssr:ng-{plate|web}`

## Shared Conventions

- Package manager: pnpm (pnpm@10.x). Do not use npm or yarn for either frontend.
- Testing: Vitest, not Jasmine/Karma. Test files use `*.spec.ts` suffix.
- Angular CLI builder: `@angular/build:unit-test` for tests.
- SCSS for component/stylesheets.
- Strict TypeScript with isolatedModules enabled.
