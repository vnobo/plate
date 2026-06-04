# ng-plate

Production Angular 21 application with Tabler UI, SSR, and PWA support.

## Tech Stack

- **Angular** 21.2.x
- **TypeScript** 5.9
- **RxJS** 7.8
- **Tabler Core** 1.4.0
- **Express** 5.1.0 (SSR server)
- **Vitest** 4.x (testing)
- **dayjs** (date handling)

## Development

```bash
pnpm install
pnpm start              # dev server at http://localhost:4200
```

## Build

```bash
pnpm build              # production build
pnpm build:ssr          # SSR build
```

## Testing

```bash
pnpm test               # Vitest
```

## Project Structure

```
src/app/
├── core/                  # Core services
│   ├── net/               # HTTP interceptor
│   ├── storage/           # Browser & session storage
│   ├── services/          # Token, settings services
│   └── pages.guard.ts     # Auth guard
├── layout/                # Layout components
│   ├── base-layout.ts     # Main layout with sidebar + header
│   ├── blank-layout.ts    # Minimal layout (login, errors)
│   ├── layout-aside.ts    # Sidebar navigation
│   └── layout-header.ts   # Top header bar
├── pages/
│   ├── dashboard/         # Main dashboard, users, welcome
│   ├── passport/          # Login, auth lock
│   ├── platform/          # Tenant management
│   ├── examples/          # Component demos (data-table, transfer)
│   └── error/             # 404, 500, 512 pages
├── plugins/               # Reusable UI plugins
│   ├── modals.ts          # Modal service
│   ├── toasts.ts          # Toast notifications
│   ├── data-table.ts      # Data table component
│   ├── transfer.ts        # Transfer list component
│   └── progress.ts        # Progress bar
└── envs/                  # Environment configs
```

## Environment

- `src/envs/env.dev.ts` — Development (`http://localhost:8080`)
- `src/envs/env.ts` — Production

## Proxy

Development proxy configured in `proxy.conf.json`:
- `/sec/*` → `http://localhost:8080`
- `/rel/*` → `http://localhost:8080`
