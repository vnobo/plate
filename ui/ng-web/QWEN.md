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
├── AGENTS.md
├── angular.json
├── ngsw-config.json
├── package-lock.json
├── package.json
├── proxy.conf.json
├── QWEN.md
├── README.md
├── tsconfig.app.json
├── tsconfig.json
├── tsconfig.spec.json
├── .angular\...
├── .github\...
├── .roo\...
├── .vscode\...
├── node_modules\...
├── public\
│   ├── favicon.ico
│   ├── manifest.webmanifest
│   └── icons\...
└── src\
    ├── index.html
    ├── main.server.ts
    ├── main.ts
    ├── server.ts
    ├── app\
    │   ├── app.ts
    │   ├── app.config.ts
    │   ├── app.html
    │   ├── app.scss
    │   ├── app.routes.ts
    │   └── core\...
    ├── environments\
    ├── styles.scss
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
The application will be available at `http://localhost:4000/` and will automatically reload when source files are modified.

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
- Start MCP services: `npm run mcp`

### Development Server with Proxy
The project uses a proxy configuration to redirect API requests to a backend server:
```json
{
  "/api": {
    "target": "http://localhost:8080/",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug",
    "pathRewrite": {
      "^/api": ""
    }
  }
}
```

## Development Conventions

- Uses SCSS for styling (as configured in angular.json)
- Follows Angular best practices and conventions
- Uses zoneless change detection
- TypeScript strict mode enabled
- Prettier configured with print width of 100, single quotes, and Angular parser for HTML files
- Client-side routing with Angular Router
- Server-side rendering enabled with hydration support
- Includes internationalization support with Chinese locale as default ('zh_CN')
- Uses Tabler CSS framework for UI components and styling
- Includes service worker support for PWA capabilities

## Key Dependencies

- `@angular/*`: Core Angular libraries
- `@tabler/core`: UI framework (CSS/JS components)
- `@angular/ssr`: Server-side rendering support
- `@angular/service-worker`: PWA capabilities
- `express`: Web server for SSR
- `rxjs`: Reactive programming library
- `dayjs`: Date/time utility library

## Configuration Files

- `angular.json`: Angular CLI project configuration
- `package.json`: NPM dependencies and scripts
- `tsconfig.json`: TypeScript compiler configuration
- `ngsw-config.json`: Service worker configuration
- `proxy.conf.json`: Development server proxy configuration

## Special Features

- Server-Side Rendering (SSR) with hydration for better performance and SEO
- Progressive Web App (PWA) support with service worker
- Theme switching between light and dark modes
- Responsive design using Tabler CSS framework
- Preconfigured with Tabler's UI components and styles
- Internationalization support with dayjs for date formatting
- Zoneless change detection for improved performance
- HTTP client with interceptors, CSRF protection, and fetch API support
- MCP (Model Context Protocol) services for enhanced development

## Application Architecture

The application uses a modern Angular architecture with:
- Component-based structure
- Service-oriented architecture
- Reactive state management
- Client-side routing with lazy loading capability (though routes are currently empty)
- Shared services for common functionality (MessageService, etc.)
- Custom interceptors for HTTP requests
- Environment-specific configurations

## MCP Services

This project includes support for Model Context Protocol (MCP) services to enhance development capabilities:

- Context7: Provides up-to-date documentation and code examples for libraries
- Angular CLI: Offers Angular best practices and documentation search
- Playwright: Enables browser automation and testing capabilities
- Chrome DevTools: Provides browser debugging and performance analysis tools