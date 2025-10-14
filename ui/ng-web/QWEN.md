# Qwen Code Context for NgWeb

## Project Overview

NgWeb is an Angular application generated using Angular CLI version 20.3.5. The project includes a modern dashboard interface using the Tabler CSS framework, with features like:

- Client-side rendering with hydration
- Server-side rendering (SSR) support
- Service worker integration for PWA capabilities
- Responsive design with Tabler UI components
- Theme switching (light/dark mode)
- Navigation with dropdown menus

## Technologies Used

- Angular 20.3.x
- TypeScript 5.9.x
- Tabler CSS framework (v1.4.0)
- RxJS
- Express.js (for SSR)
- Angular Service Worker
- Angular SSR (Server-Side Rendering)

## Project Structure

```
D:\workspace\plate\ui\ng-web\
├── .editorconfig
├── .gitignore
├── angular.json
├── ngsw-config.json
├── package-lock.json
├── package.json
├── README.md
├── tsconfig.app.json
├── tsconfig.json
├── tsconfig.spec.json
├── .roo\...
├── node_modules\...
├── public\
│   ├── favicon.ico
│   ├── manifest.webmanifest
│   └── icons\
└── src\
    ├── index.html
    ├── main.server.ts
    ├── main.ts
    ├── ...
    └── ...
```

## Building and Running

### Development Server
To start a local development server:
```bash
ng serve
```
or
```bash
npm start
```
The application will be available at `http://localhost:4200/` and will automatically reload when source files are modified.

### Production Build
To build the project for production:
```bash
ng build
```
or
```bash
npm run build
```
Build artifacts are stored in the `dist/` directory with optimization for performance.

### Other Commands
- Watch mode for development: `npm run watch`
- Run unit tests: `ng test` or `npm test`
- Run end-to-end tests: `ng e2e`
- Serve SSR build: `npm run serve:ssr:ng-web`

## Development Conventions

- Uses SCSS for styling (as configured in angular.json)
- Follows Angular best practices and conventions
- Uses zoneless change detection
- TypeScript strict mode enabled
- Prettier configured with print width of 100, single quotes, and Angular parser for HTML files
- Client-side routing with Angular Router
- Server-side rendering enabled with hydration support

## Key Dependencies

- `@angular/*`: Core Angular libraries
- `@tabler/core`: UI framework (CSS/JS components)
- `@angular/ssr`: Server-side rendering support
- `@angular/service-worker`: PWA capabilities
- `express`: Web server for SSR
- `rxjs`: Reactive programming library

## Configuration Files

- `angular.json`: Angular CLI project configuration
- `package.json`: NPM dependencies and scripts
- `tsconfig.json`: TypeScript compiler configuration
- `ngsw-config.json`: Service worker configuration

## Special Features

- Server-Side Rendering (SSR) with hydration for better performance and SEO
- Progressive Web App (PWA) support with service worker
- Theme switching between light and dark modes
- Responsive design using Tabler CSS framework
- Preconfigured with Tabler's UI components and styles