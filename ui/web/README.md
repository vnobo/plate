# Angular v20 Enterprise Web Application

![Angular](https://img.shields.io/badge/Angular-v20.0.0-red?style=for-the-badge&logo=angular)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8.2-blue?style=for-the-badge&logo=typescript)
![RxJS](https://img.shields.io/badge/RxJS-7.8.0-pink?style=for-the-badge&logo=reactivex)
![SSR](https://img.shields.io/badge/SSR-Enabled-green?style=for-the-badge&logo=serverless)
![PWA](https://img.shields.io/badge/PWA-Supported-orange?style=for-the-badge&logo=pwa)

A production-ready Angular v20 web application featuring modern architecture patterns,
enterprise-grade performance optimizations, and comprehensive development tooling.

[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Node.js](https://img.shields.io/badge/Node.js-%E2%89%A518.0.0-brightgreen)](https://nodejs.org/)

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Technology Stack](#technology-stack)
- [Architecture](#-architecture)
- [Key Features](#-key-features)
- [Project Structure](#-project-structure)
- [Development Guide](#-development-guide)
- [Performance Optimization](#-performance-optimization)
- [Testing Strategy](#-testing-strategy)
- [Deployment](#-deployment)
- [Contributing Guidelines](#-contributing-guidelines)
- [Support](#-support)
- [License](#-license)

## 🚀 Project Overview

### Business Value Proposition

This Angular v20 application serves as a robust foundation for enterprise web applications,
providing:

- **Scalable Architecture**: Built with modern Angular patterns for maintainable and extensible code
- **Production-Ready Features**: SSR, PWA, authentication, and comprehensive error handling
- **Developer Experience**: Optimized development workflow with hot reload, testing, and debugging
  tools
- **Performance Excellence**: Zoneless change detection, code splitting, and advanced optimization
  techniques

### Core Capabilities

- **User Management System**: Complete CRUD operations with type-safe forms and validation
- **Authentication & Authorization**: JWT-based security with route guards and HTTP interceptors
- **Responsive UI**: Material Design components with Tabler theme integration
- **Plugin System**: Extensible alert, modal, and toast notification system
- **Multi-Environment Support**: Development, staging, and production configurations

## Technology Stack

### Core Framework & Runtime

| Component      | Version | Purpose                        |
| -------------- | ------- | ------------------------------ |
| **Angular**    | v20.0.0 | Main application framework     |
| **TypeScript** | 5.8.2   | Type-safe development language |
| **RxJS**       | 7.8.0   | Reactive programming library   |
| **Node.js**    | ≥18.0.0 | Runtime environment            |

### UI & Styling

| Component            | Version | Purpose                           |
| -------------------- | ------- | --------------------------------- |
| **Angular Material** | v20.0.0 | Material Design component library |
| **Tabler Core**      | Latest  | Admin interface theme system      |
| **Tabler Icons**     | Latest  | Comprehensive icon library        |
| **Day.js**           | Latest  | Lightweight date manipulation     |

### Build & Tooling

| Component         | Version | Purpose                       |
| ----------------- | ------- | ----------------------------- |
| **Angular CLI**   | v20.0.0 | Development and build tooling |
| **Angular Build** | v20.0.0 | Modern build system           |
| **Karma**         | ~6.4.0  | Unit test runner              |
| **Jasmine**       | ~5.1.0  | Testing framework             |

### Server & Deployment

| Component                   | Version | Purpose                       |
| --------------------------- | ------- | ----------------------------- |
| **Express**                 | 5.1.0   | SSR server framework          |
| **@angular/ssr**            | v20.0.0 | Server-side rendering support |
| **@angular/service-worker** | v20.0.0 | PWA capabilities              |

## 🏗️ Architecture

### Design Principles

This application follows modern Angular architectural patterns:

#### 1. Standalone Components Architecture

```typescript
@Component({
  selector: 'app-users',
  imports: [CommonModule],
  templateUrl: './users.html',
  styleUrl: './users.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Users {
  private readonly _message = inject(MessageService);
  private readonly _modal = inject(ModalsService);
  private readonly _http = inject(HttpClient);

  readonly userData = signal<Page<User>>({
    content: [],
    pageable: { page: 0, size: 0, sorts: [] },
    totalElements: 0,
    totalPages: 0,
    size: 0,
    number: 0,
    first: true,
    last: true,
    numberOfElements: 0,
    empty: true,
  });
}
```

#### 2. Signal-Based State Management

```typescript
@Component({
  // ... component config
})
export class UserListComponent {
  private readonly userService = inject(UserService);

  // Signal for reactive state management
  readonly users = signal<User[]>([]);
  readonly loading = signal(false);

  // Computed values for derived state
  readonly activeUsers = computed(() => this.users().filter(user => user.active));

  async loadUsers(): Promise<void> {
    this.loading.set(true);
    try {
      const users = await this.userService.getUsers();
      this.users.set(users);
    } finally {
      this.loading.set(false);
    }
  }
}
```

#### 3. Three-Layer HTTP Interceptor Chain

The application implements a sophisticated interceptor pattern:

```typescript
// Core interceptor configuration in app.config.ts
provideHttpClient(
  withFetch(),
  withInterceptorsFromDi(),
  withInterceptors([
    defaultInterceptor, // Request preprocessing & timeout
    handleErrorInterceptor, // Error handling & auth redirect
    authTokenInterceptor, // JWT token management
  ]),
  withXsrfConfiguration({
    cookieName: 'XSRF-TOKEN',
    headerName: 'X-XSRF-TOKEN',
  }),
);
```

### Architectural Patterns

- **Layered Architecture**: Clear separation between presentation, business logic, and data access
- **Dependency Injection**: Angular's built-in DI system for testable and maintainable code
- **Reactive Programming**: RxJS observables for complex async operations
- **Component Composition**: Small, focused components with clear responsibilities

## 🔧 Key Features

### Authentication & Security

#### JWT Token Management

```typescript
@Injectable({ providedIn: 'root' })
export class TokenService {
  public readonly loginUrl = '/passport/login';
  private readonly authenticationKey = 'authentication';
  private readonly _storage = inject(SessionStorageService);

  authToken(): string {
    const authentication = this.authenticationLoadStorage();
    if (authentication) {
      return authentication.token;
    }
    throw new HttpErrorResponse({
      error: 'Authentication is invalid, please log in again.',
      status: 401,
    });
  }

  isLogged(): boolean {
    return !!this.authenticationLoadStorage();
  }

  logout(): void {
    this._storage.removeItem(this.authenticationKey);
  }
}
```

#### Route Guards

```typescript
export const authGuard: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (tokenService.isLogged()) {
    return true;
  }

  return router.createUrlTree([tokenService.loginUrl]);
};
```

### User Management System

- **User List**: Paginated, searchable user interface with virtual scrolling
- **User Forms**: Type-safe reactive forms with comprehensive validation
- **Role-Based Access**: Extensible permission system
- **Profile Management**: User preferences and settings

### Plugin System Architecture

#### Alert System

```typescript
@Component({
  selector: 'tabler-alert',
  imports: [CommonModule],
  template: `
    <div
      class="alert alert-dismissible"
      role="alert"
      [ngClass]="{
        'alert-success': alert().type === 'success',
        'alert-danger': alert().type === 'danger',
        'alert-warning': alert().type === 'warning',
        'alert-info': alert().type === 'info'
      }">
      <div class="alert-icon">
        <!-- SVG icons for different alert types -->
      </div>
      {{ alert().message }}
      <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
    </div>
  `,
})
export class Alerts {
  readonly alert = signal<Alert>({ id: '', message: '', type: 'info' });

  show(alert: Alert): void {
    this.alert.set(alert);
  }
}
```

### Theme System

- **Multi-Theme Support**: Light/dark mode with system preference detection
- **CSS Custom Properties**: Dynamic theme variables for consistent styling
- **Component Theming**: Material Design theming integration

## 📁 Project Structure

```text
src/
├── app/                          # Application root
│   ├── core/                     # Core functionality
│   │   ├── net/                  # HTTP layer
│   │   │   └── http.Interceptor.ts # Interceptor chain
│   │   ├── services/             # Core services
│   │   │   ├── token.service.ts  # Authentication service
│   │   │   ├── progress-bar.ts   # Loading indicator
│   │   │   └── theme.service.ts  # Theme management
│   │   └── storage/              # Storage abstractions
│   │       ├── browser-storage.ts # Local storage wrapper
│   │       └── session-storage.ts # Session storage (SSR compatible)
│   ├── layout/                   # Layout components
│   │   └── base-layout.ts        # Main application layout
│   ├── pages/                    # Feature pages (lazy-loaded)
│   │   ├── dashboard/            # Dashboard module
│   │   ├── home/                 # Home & user management
│   │   ├── passport/             # Authentication pages
│   │   └── error/                # Error pages (404, 500, 512)
│   └── plugins/                  # Reusable UI components
│       ├── alerts/               # Alert system
│       ├── modals/               # Modal dialogs
│       └── toasts/               # Toast notifications
├── envs/                         # Environment configurations
│   ├── env.ts                    # Production configuration
│   └── env.dev.ts                # Development configuration
└── public/                       # Static assets
    ├── assets/                   # Images, fonts, etc.
    ├── icons/                    # PWA icons
    └── manifest.webmanifest      # PWA manifest
```

### Directory Responsibilities

- **`core/`**: Singleton services, interceptors, guards, and utilities
- **`pages/`**: Feature modules with route-based lazy loading
- **`plugins/`**: Reusable UI components and utility services
- **`envs/`**: Environment-specific configurations
- **`public/`**: Static assets and PWA configuration

## 🚦 Development Guide

### Prerequisites

- **Node.js** v18 or higher (LTS recommended)
- **npm** v9+ or **yarn** v1.22+
- **Angular CLI** v20.0.0 (`npm install -g @angular/cli@20`)

### Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd web

# Install dependencies
npm install

# Start development server
npm start

# Application will be available at http://localhost:4200
```

### Development Workflow

#### 1. Development Server with Hot Reload

```bash
# Start development server with proxy to backend API
npm start
# or
ng serve

# Development server proxies API requests to http://localhost:8080/
```

#### 2. Build Commands

```bash
# Production build (SSR + PWA)
npm run build

# Development build (with file replacement)
ng build --configuration=development

# Watch mode for development
npm run watch
```

#### 3. Code Generation

```bash
# Generate standalone component
ng generate component components/user-list --standalone --change-detection=OnPush

# Generate service
ng generate service services/user

# Generate guard
ng generate guard guards/auth

# Note: Angular CLI doesn't have a specific command for interfaces
# Create interfaces manually in the appropriate models directory
```

### Development Best Practices

#### Component Design

```typescript
@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgOptimizedImage],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'user-card',
    '[attr.data-theme]': 'theme()',
  },
  template: `
    <div class="user-card__content">
      <img [ngSrc]="user().avatar" [alt]="user().name" width="64" height="64" />
      <h3>{{ user().name }}</h3>
      <p>{{ user().email }}</p>
    </div>
  `,
})
export class UserCardComponent {
  readonly user = input.required<User>();
  readonly theme = computed(() => (this.user().active ? 'active' : 'inactive'));
}
```

#### Service Implementation

```typescript
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly API_PREFIX = '/sec';

  getUser(id: number): Observable<User> {
    return this.http.get<User>(`${this.API_PREFIX}/users/${id}`);
  }

  page(request: User, page: Pageable): Observable<Page<User>> {
    let params = new HttpParams({ fromObject: request as never });
    params = params.appendAll({ page: page.page - 1, size: page.size });
    for (const sort in page.sorts) {
      params = params.appendAll({ sort: page.sorts[sort] });
    }
    return this.http.get<Page<User>>(this.API_PREFIX + '/users/page', { params: params });
  }
}
```

## 📊 Performance Optimization

### Build-Time Optimizations

#### Code Splitting Strategy

```typescript
// Lazy-loaded route configuration in app.routes.ts
export const routes: Routes = [
  {
    path: 'home',
    loadChildren: () => import('./pages/index').then(m => m.HOME_ROUTES),
  },
  {
    path: 'passport',
    loadChildren: () => import('./pages/index').then(m => m.PASSPORT_ROUTES),
  },
  {
    path: 'exception',
    loadChildren: () => import('./pages/index').then(m => m.EXCEPTION_ROUTES),
  },
  { path: '', pathMatch: 'full', redirectTo: '/passport' },
  { path: '**', redirectTo: 'exception/404' },
];
```

#### Bundle Optimization

- **Tree Shaking**: Dead code elimination through Angular's build system
- **Asset Optimization**: Image compression and font subsetting
- **CSS Extraction**: Critical CSS inlining and unused style removal

### Runtime Optimizations

#### Zoneless Change Detection

```typescript
// app.config.ts - Enable zoneless mode
export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),
    // ... other providers
  ],
};
```

#### Signal-Based Reactivity

```typescript
@Component({
  // ... component config
})
export class EfficientComponent {
  readonly data = signal<Data[]>([]);
  readonly filter = signal('');

  // Computed values only recompute when dependencies change
  readonly filteredData = computed(() =>
    this.data().filter(item => item.name.toLowerCase().includes(this.filter().toLowerCase())),
  );

  // Efficient updates without zone.js overhead
  updateData(newData: Data[]): void {
    this.data.set(newData);
  }
}
```

#### Virtual Scrolling for Large Lists

```typescript
@Component({
  template: `
    <cdk-virtual-scroll-viewport itemSize="50">
      <div *cdkVirtualFor="let user of users()" class="user-item">
        {{ user.name }}
      </div>
    </cdk-virtual-scroll-viewport>
  `,
})
export class UserListComponent {
  readonly users = signal<User[]>([]);
}
```

### Network Optimizations

#### HTTP Interceptor Performance

```typescript
function defaultInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const _loading = inject(ProgressBar);
  _loading.show();

  // Bypass custom logic for asset requests
  if (req.url.indexOf('assets/') > -1) {
    return next(req);
  }

  const originalUrl = req.url.indexOf('http') > -1 ? req.url : environment.host + req.url;
  const xRequestedReq = req.clone({
    headers: req.headers.append('X-Requested-With', 'XMLHttpRequest'),
    url: originalUrl,
  });

  return next(xRequestedReq).pipe(
    timeout({ first: 5_000, each: 10_000 }),
    finalize(() => _loading.hide()),
  );
}
```

#### Caching Strategies

- **Service Worker**: Production PWA caching for static assets
- **HTTP Cache**: Browser caching with proper cache headers
- **Memory Cache**: In-memory caching for frequently accessed data

## 🧪 Testing Strategy

### Unit Testing

#### Component Testing

```typescript
// Example from existing login.spec.ts
describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent, HttpClientTestingModule],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.form.value).toEqual({ username: '', password: '' });
  });
});
```

#### Service Testing

```typescript
describe('UserService', () => {
  let service: UserService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService],
    });

    service = TestBed.inject(UserService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should retrieve user by id', () => {
    const mockUser: User = { id: 1, name: 'Test User', email: 'test@example.com' };

    service.getUser(1).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpTestingController.expectOne('/sec/users/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  afterEach(() => {
    httpTestingController.verify();
  });
});
```

### Integration Testing

#### HTTP Interceptor Testing

```typescript
describe('AuthInterceptor', () => {
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        provideHttpClient(withInterceptors([authTokenInterceptor])),
        {
          provide: TokenService,
          useValue: { isLogged: () => true, authToken: () => 'test-token' },
        },
      ],
    });

    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should add authorization header for authenticated requests', () => {
    const httpClient = TestBed.inject(HttpClient);

    httpClient.get('/api/test').subscribe();

    const req = httpTestingController.expectOne('/api/test');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush({});
  });
});
```

### E2E Testing

```typescript
// Example E2E test structure (would be in separate e2e test files)
describe('User Management', () => {
  beforeEach(() => {
    cy.visit('/home/users');
  });

  it('should display user list', () => {
    cy.get('.user-list').should('be.visible');
    cy.contains('User Management').should('be.visible');
  });

  it('should allow creating a new user', () => {
    cy.get('#add-user-btn').click();
    cy.get('#user-form').should('be.visible');
    cy.get('#username').type('testuser');
    cy.get('#email').type('test@example.com');
    cy.get('#submit-btn').click();
    cy.contains('User created successfully').should('be.visible');
  });
});
```

### Testing Best Practices

- **Test Isolation**: Each test should be independent
- **Mock Dependencies**: Use TestBed for dependency injection
- **Test Coverage**: Aim for 80%+ coverage on critical paths
- **Continuous Testing**: Integrate with CI/CD pipeline

## 🚀 Deployment

### Production Build

```bash
# Build for production with SSR and PWA
npm run build

# Output will be in dist/web/ directory
```

### Deployment Options

#### 1. Static Hosting (SSG/SSR)

```bash
# Build the application
npm run build

# Deploy dist/web/browser/ for static hosting
# Deploy dist/web/server/ for SSR hosting
```

**Supported Platforms**:

- **Vercel**: Zero-config deployment with SSR support
- **Netlify**: Static hosting with form handling
- **Firebase Hosting**: Google's hosting platform
- **AWS S3 + CloudFront**: Enterprise-grade static hosting

#### 2. Traditional Server Deployment

```bash
# Build and run SSR server
npm run build
npm run serve:ssr:web

# Server runs on port 4000 by default
```

#### 3. Docker Deployment

```dockerfile
# Dockerfile
FROM node:18-alpine

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

EXPOSE 4000
CMD ["npm", "run", "serve:ssr:web"]
```

### Environment Configuration

#### Production Environment (`src/envs/env.ts`)

```typescript
export const environment = {
  production: true,
  host: '', // Empty for relative paths
  relaApiPath: '/rela/v1',
  secApiPath: '/sec/v1',
};
```

#### Development Environment (`src/envs/env.dev.ts`)

```typescript
export const environment = {
  production: false,
  host: '/api', // Proxy to backend
  relaApiPath: '/rela/v1',
  secApiPath: '/sec/v1',
};
```

### Deployment Checklist

- [ ] Run production build: `npm run build`
- [ ] Test SSR server locally: `npm run serve:ssr:web`
- [ ] Verify PWA functionality in production
- [ ] Check environment configuration
- [ ] Validate API endpoints and CORS settings
- [ ] Test authentication flow
- [ ] Verify error pages (404, 500)
- [ ] Check performance metrics (Lighthouse)

## 🤝 Contributing Guidelines

### Development Workflow

#### 1. Fork and Clone

```bash
git clone https://github.com/your-username/web.git
cd web
git remote add upstream https://github.com/original-repo/web.git
```

#### 2. Create Feature Branch

```bash
git checkout -b feature/amazing-feature
```

#### 3. Development Process

```bash
# Install dependencies
npm install

# Start development server
npm start

# Run tests
npm test

# Build for production
npm run build
```

#### 4. Commit Changes

```bash
# Follow conventional commit format
git commit -m "feat: add user management functionality"
git commit -m "fix: resolve authentication token expiration"
git commit -m "docs: update API documentation"
```

#### 5. Push and Create PR

```bash
git push origin feature/amazing-feature
# Create Pull Request on GitHub
```

### Code Standards

#### TypeScript Standards

```typescript
// Use strict typing - avoid 'any'
interface User {
  id: number;
  name: string;
  email: string;
  active: boolean;
}

// Prefer readonly properties for immutability
class UserService {
  private readonly http = inject(HttpClient);
}

// Use proper error handling
try {
  await this.loadData();
} catch (error) {
  this.handleError(error);
}
```

#### Angular Best Practices

```typescript
// Use standalone components
@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})

// Use signals for state management
readonly users = signal<User[]>([]);

// Use dependency injection properly
constructor(private readonly userService: UserService) {}
```

#### CSS/SCSS Standards

```scss
// Use BEM methodology with Tabler integration
.user-card {
  border: 1px solid var(--tblr-border-color);
  border-radius: var(--tblr-border-radius);

  &__header {
    padding: var(--tblr-card-spacer-y) var(--tblr-card-spacer-x);
    background-color: var(--tblr-card-cap-bg);
    border-bottom: 1px solid var(--tblr-border-color);
  }

  &__content {
    padding: var(--tblr-card-spacer-y) var(--tblr-card-spacer-x);
  }

  &--active {
    border-color: var(--tblr-primary);
    box-shadow: 0 0 0 1px var(--tblr-primary);
  }
}

// Leverage Tabler CSS custom properties
:root {
  --tblr-primary: #206bc4;
  --tblr-border-radius: 4px;
  --tblr-card-spacer-y: 1rem;
  --tblr-card-spacer-x: 1.5rem;
}
```

### Pull Request Process

1. **Code Review Requirements**:

   - All tests must pass
   - Code follows established patterns
   - Documentation updated if needed
   - No console.log statements in production code

2. **Review Checklist**:

   - [ ] Code follows Angular style guide
   - [ ] Tests added for new functionality
   - [ ] Documentation updated
   - [ ] Performance impact considered
   - [ ] Security considerations addressed

3. **Merge Criteria**:
   - At least one approved review
   - All CI checks passing
   - No merge conflicts

### Issue Reporting

When reporting issues, please include:

- **Environment Details**: OS, Node version, Angular version
- **Steps to Reproduce**: Clear, reproducible steps
- **Expected Behavior**: What you expected to happen
- **Actual Behavior**: What actually happened
- **Screenshots/Logs**: Visual evidence or error logs

## 🆘 Support

### Getting Help

- **Documentation**: Check the detailed documentation in the project
- **Issues**: Search existing issues in the repository
- **Code Inspection**: Review the actual implementation in source files

### Troubleshooting Common Issues

#### Build Issues

```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Clear Angular cache
ng cache clean

# Check Angular version compatibility
ng version
```

#### SSR Issues

```bash
# Test SSR locally
npm run build
npm run serve:ssr:web

# Check server logs for errors
```

#### PWA Issues

```bash
# Unregister service worker in browser dev tools
# Clear browser cache and hard reload
```

### Performance Optimization Tips

1. **Bundle Analysis**: Use `ng build --stats-json` and webpack-bundle-analyzer
2. **Lighthouse Audit**: Regular performance audits
3. **Memory Profiling**: Monitor memory usage in production
4. **Network Optimization**: Implement lazy loading and caching

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### Third-Party Licenses

- **Angular**: MIT License
- **Angular Material**: MIT License
- **Tabler**: MIT License
- **RxJS**: Apache-2.0 License

## 🔗 Useful Links

### Documentation

- [Angular Documentation](https://angular.dev/)
- [Angular CLI Reference](https://angular.dev/tools/cli)
- [Angular Material Components](https://material.angular.io/components)
- [RxJS Operators](https://rxjs.dev/guide/operators)

### Tools & Resources

- [Angular DevTools](https://angular.dev/tools/devtools)
- [Angular ESLint](https://github.com/angular-eslint/angular-eslint)
- [Angular Performance Guide](https://web.dev/angular/)

### Community

- [Angular Blog](https://blog.angular.io/)
- [Angular GitHub](https://github.com/angular/angular)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/angular)

---

**Built with ❤️ using Angular v20**

_Last Updated: September 2025_

[![Angular](https://img.shields.io/badge/Angular-v20.0.0-red?logo=angular)](https://angular.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8.2-blue?logo=typescript)](https://www.typescriptlang.org/)
[![RxJS](https://img.shields.io/badge/RxJS-7.8.0-pink?logo=reactivex)](https://rxjs.dev/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
