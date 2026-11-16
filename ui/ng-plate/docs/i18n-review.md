# i18n review and remediation status

Scope: `ui/ng-plate` (Angular 22 SSR + Tabler). Reviewed: i18n configuration and catalogs,
`$localize`/`i18n` coverage, locale switching, SSR/localized build output, formatting,
tests and deployment. Source-language policy: English code, Chinese (`zh-Hans`) UI copy as
i18n source, English (`en-US`) delivered by the translation catalog.

Status legend: **Fixed** = changed in this pass, **Open** = tracked below, **Verify** = needs
a manual check that cannot run in CI.

## 1. Baseline before this pass

- `angular.json` had `sourceLocale: zh-Hans` and an `en-US` catalog, but only
  `build:i18n` localized; the default `build` produced one locale.
- 103 messages existed, covering `layout`, `login`, `themes-gallery` and `theme-meta` only.
- Every other page/component rendered hardcoded English copy.
- The language menu only existed in `BaseLayout`, and its label came from `localStorage`
  while the served build decided the real language.
- `i18nMissingTranslation: "warning"` silently fell back to Chinese in the English build.
- The generated `src/locale/messages.xlf` was tracked.
- `src/server.ts` mounted `browser/en-US` a second time on top of the framework's own
  localized entry points and cached every static file for a year, including `ngsw.json`.

## 2. P0 findings (correctness / deployment)

| # | Finding | Evidence | Status |
| --- | --- | --- | --- |
| P0-1 | Default `pnpm build` never emitted `en-US`; `serve:ssr:en-US` pointed at the same `server.mjs` as the default script, implying one server per locale | `package.json` scripts | Fixed: `build:localized` is the documented command; `serve:ssr:en-US` removed (one server serves both locales) |
| P0-2 | Missing translations only warned | `angular.json` | Fixed: `i18nMissingTranslation: "error"` |
| P0-3 | Locale switch was a partial reimplementation: label from `localStorage`, no cookie/`Accept-Language` negotiation, hardcoded `zh-Hans`/`en-US` pair, bypassed `BrowserStorage` | `layout-header.ts` (`resolveInitialLang`, `setLang`) | Fixed: `core/i18n/locale.ts` + `LocaleService` own the rules; header delegates |
| P0-4 | Switcher unreachable on login, error and theme-gallery pages | those templates | Fixed: shared `LocaleSwitcher` mounted on all four surfaces |
| P0-5 | Route titles were English constants, so the browser tab stayed English in the Chinese build | route files | Fixed: all routes use `$localize` (`route.*` units) |
| P0-6 | No locale-aware dates: `dayjs` pinned to `zh-cn`, `data-table-demo` hardcoded `'zh-CN'` | `plugins/dayjs.locale.ts`, `data-table-demo.ts` | Fixed: dayjs follows `<html lang>`; demo uses `LOCALE_ID` |
| P0-7 | Counts built by string concatenation (`4 套`, `N unread`, `Showing x of y`) | header, data-table, gallery | Partially fixed: ICU pattern documented and applied to the notification counter; remaining counts are P1 |
| P0-8 | Generated source catalog tracked; two message-id styles | `src/locale/messages.xlf` | Fixed: gitignored and removed; id convention in `docs/i18n.md`, existing ids frozen |
| P0-9 | Duplicate `/en-US` static mount and blanket 1-year caching (including `ngsw.json`, service worker) | `src/server.ts` | Fixed: single mount; hashed assets cached, HTML/manifests/workers `no-cache` |
| P0-10 | Unit test asserted Chinese literals, coupling tests to one locale | `login.spec.ts` | Fixed: expectations come from the catalog via `$localize` |
| P0-11 | SSR engine answered `400 Bad Request` for every HTML request: `security.allowedHosts` was empty, so `localhost` and `127.0.0.1` were rejected | observed with `node dist/ng-plate/server/server.mjs` | Fixed: `angular.json` sets `allowedHosts: ["localhost", "127.0.0.1"]`; production hosts via `NG_ALLOWED_HOSTS` (documented) |

## 3. P1: remaining copy (open, next pass)

Shared chrome:

- `layout/layout-header.ts`: primary nav (`Platform`/`Dashboard`/`Examples`), notifications
  panel (`Notifications`, `Archive all`, `Mark all as read`, `No notifications`, demo notice
  data), user menu (`Dashboard`, `Log out`), theme menu (`Switch theme`), `aria-label`s.
- `layout/layout-aside.ts`: `Main navigation`, `Close navigation`, menu failure retry text.
- `plugins/data-table.ts`, `transfer.ts`, `modals.ts`, `confirm-dialog.ts`, `toasts.ts`:
  default labels, empty states, pagination summary, `aria-label`s.
- `core/services/settings.service.ts`: palette descriptions.

Pages and forms:

- `pages/platform/users/*` (+ `user-form`, `user-authority-form`), `tenant/*`
  (+ `tenant-members-form`), `role/*` (+ `role-form`, `member-form`, `authority-form`),
  `menus/*` (+ `menu-form`).
- `pages/platform/welcome/*`, `pages/dashboard/welcome/*`.
- `pages/passport/auth-lock/*` (still Tabler sample copy, also a functional placeholder).

Suggested batches:

1. Shared chrome + plugin defaults (one build/regression cycle, highest visibility).
2. Platform pages and forms (largest volume; term list: 用户/租户/角色/菜单/权限/启用/停用).
3. Welcome pages and auth-lock; convert remaining counts to ICU.
4. Polish: every `aria-label`/`title`/`placeholder`/`alt`, `index.html` and PWA metadata.

## 4. P2: architecture and UX improvements

- `index.html`: static `<title>`/meta, English boot splash (`Preparing application`) and
  `noscript` text are not localizable by the CLI. Use the brand as the static title, or a
  tiny inline script keyed off `<html lang>`; `manifest.webmanifest` needs `lang` and proper
  `name`/`short_name` (currently `ng-plate`).
- No `hreflang`/canonical; only relevant if the marketing/theme pages are indexed.
- `layout-header.ts` and `layout-aside.ts` still set `ChangeDetectionStrategy.OnPush`
  explicitly, against the repository convention.
- Many menu toggles use `href="javascript:void(0)"`; prefer `<button>` for CSP and keyboard
  semantics.
- `pages/platform/welcome` and `pages/dashboard/welcome` are near-duplicates.
- `@plate/types` is an ambient module in `typings.d.ts` rather than a shared types path.
- `AGENTS.md` says pnpm 11.22.0 while `package.json` pins 12.x.
- Localized build prints an `initial` bundle budget warning (1.28 MB vs 1.25 MB); confirm the
  baseline and either raise the budget or split the chunk.
- Preview caveat: `pnpm start` serves the source locale only; check English through
  `pnpm build:localized` + `pnpm serve:ssr:ng-plate`.

## 5. Dynamic content (design only, not implemented)

Menu names, dictionary labels and backend validation/exception text do not pass through
`@angular/localize`; they arrive from the API at runtime.

1. **Transport**: send `Accept-Language` from `LOCALE_ID` in `http.Interceptor`; backend
   resolves it (WebFlux `LocaleContextHolder`/`MessageSource`) and adds
   `Vary: Accept-Language` to cacheable responses.
2. **Messages**: replace free-text exception messages with stable codes plus parameters;
   `RestServerException` carries the code, the frontend maps code → `$localize` string with a
   generic fallback. This keeps translation in the frontend catalog and avoids backend
   redeploys for copy changes.
3. **Data**: extend `se_menus`/`se_dictionaries` with a per-locale name/label, either as an
   `extend` JSONB map (`{"i18n":{"en-US":"Users"}}`) or a translation table. JSONB is the
   smaller change; a table is better if editors need workflows. Seed the `en-US` values with
   the dictionary `LANGUAGE` type that already exists.
4. **Caching**: include the locale in cache keys (`MENUS-{userCode}-{locale}`) and never
   serve a cached payload across locales.
5. **Scope check**: tenant/user data (names, emails) is not translated; only UI labels and
   system menus are.

## 6. Verification evidence

- `pnpm test`: 7 files / 32 tests pass, including new `core/i18n/locale.spec.ts` (path
  parsing, prefix stripping, URL switching, cookie and `Accept-Language` negotiation).
- `pnpm i18n:extract`: 126 messages, all marked correctly; no template/type errors.
- `pnpm build:localized` (`i18nMissingTranslation: "error"`): succeeds, produces
  `browser/{index.csr.html, en-US/index.csr.html}`, `server/{server.mjs, en-US/}` and
  `browser/en-US/ngsw.json` with `index: /en-US/index.csr.html`. Both shells carry the right
  `<html lang>` and `<base href>`.
- SSR (`PORT=4010 node dist/ng-plate/server/server.mjs`):
  - `GET /passport/login` → 200, `lang="zh-Hans"`, `base="/"`.
  - `GET /en-US/passport/login` → 200, `lang="en-US"`, `base="/en-US/"`.
  - `GET /` with `Accept-Language: en-US` → 302 `/en-US/`.
  - `GET /` with `Accept-Language: en-US` + cookie `plate-lang=zh-Hans` → 302 `/passport`
    (cookie wins).
  - `GET /en-US/ngsw.json` and `/en-US/ngsw-worker.js` → `Cache-Control: no-cache`.
- Catalog and bundles: English targets (“Skip to main content”, “Switch language”, ICU
  “No unread”) appear in `dist/ng-plate/browser/en-US/*.js`; Chinese source and “条未读” in
  the root bundle.

## 7. Residual risks

- Language switching reloads the page; unsaved form state is lost. This is inherent to
  build-time localization and should be stated in the UI if it becomes a complaint.
- Explicit message ids hide source drift; reviewers must diff the extracted catalog when
  copy changes.
- `LOCALE_ID` in a non-localized dev build is `en-US`; locale-dependent pipes can differ from
  the Chinese production build until they read `LOCALE_ID` explicitly.
- PWA clients hold the locale builds in separate scopes; a switcher reload is required for a
  new service-worker manifest to apply.
