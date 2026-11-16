# NgPlate

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 22.2.4.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Internationalization (i18n)

The UI is built once per locale with `@angular/localize`:

| Locale | URL | Catalog |
| --- | --- | --- |
| `zh-Hans` (source, default) | `/` | strings in templates and `$localize` |
| `en-US` | `/en-US/` | `src/locale/messages.en-US.xlf` |

```bash
pnpm i18n:extract     # source catalog -> src/locale/.generated/messages.xlf (gitignored)
pnpm build:localized  # production build of both locales (alias: pnpm build:i18n)
```

`pnpm build` only produces the source locale. A missing `en-US` target fails the localized
build (`i18nMissingTranslation: "error"`). Conventions, message-id rules, ICU guidance and
the switching mechanism are documented in [docs/i18n.md](docs/i18n.md); the current gaps and
the migration backlog are in [docs/i18n-review.md](docs/i18n-review.md).

## Production SSR

```bash
pnpm build:localized
pnpm serve:ssr:ng-plate   # http://localhost:4000, serves both locales
```

Runtime environment variables:

- `PORT` — listen port (default `4000`).
- `NG_ALLOWED_HOSTS` — comma-separated hostnames accepted by the SSR engine, overriding the
  build value (`localhost,127.0.0.1`). Required in production, otherwise requests are
  rejected with `400 Bad Request`.
- `NG_TRUST_PROXY_HEADERS` — proxy headers to trust when running behind a reverse proxy,
  e.g. `x-forwarded-host`.

## Running unit tests

To execute unit tests with [Vitest](https://vitest.dev), run:

```bash
pnpm test
```

To run a single test file, pass a path glob:

```bash
pnpm test user-list.component.spec.ts
```

## Running end-to-end tests

Angular CLI does not come with an end-to-end testing framework by default. No e2e suite is
configured in this project.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
