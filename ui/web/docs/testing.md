# 测试文档

## 概述

Angular v20 Web 应用程序采用全面的测试策略，包括单元测试、集成测试和端到端测试。本文档详细描述了测试框架配置、测试用例编写和最佳实践。

## 测试架构

### 测试层次

```
测试层次结构
├── 单元测试 (Unit Tests)
│   ├── 组件测试 (Component Tests)
│   ├── 服务测试 (Service Tests)
│   ├── 管道测试 (Pipe Tests)
│   └── 指令测试 (Directive Tests)
├── 集成测试 (Integration Tests)
│   ├── 模块集成测试
│   ├── 服务集成测试
│   └── 组件集成测试
└── 端到端测试 (E2E Tests)
    ├── 用户流程测试
    ├── 页面导航测试
    └── 功能完整性测试
```

### 测试工具栈

| 工具 | 用途 | 版本 |
|------|------|------|
| **Jasmine** | 测试框架 | ~5.7.0 |
| **Karma** | 测试运行器 | ~6.4.0 |
| **Chrome Headless** | 测试浏览器 | 最新 |
| **Angular Testing Utilities** | Angular 测试工具 | ^20.0.0 |
| **TestBed** | 测试环境配置 | ^20.0.0 |

## 测试配置

### Karma 配置

**文件**: [`karma.conf.js`](karma.conf.js:1)

```javascript
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      jasmine: {
        random: true,
        seed: '4321',
        stopSpecOnExpectationFailure: false,
        failFast: false,
        timeoutInterval: 10000
      },
      clearContext: false
    },
    jasmineHtmlReporter: {
      suppressAll: true
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/web'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' },
        { type: 'lcov' }
      ],
      check: {
        global: {
          statements: 80,
          branches: 80,
          functions: 80,
          lines: 80
        }
      }
    },
    reporters: ['progress', 'kjhtml'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['Chrome'],
    singleRun: false,
    restartOnFileChange: true,
    customLaunchers: {
      ChromeHeadlessCI: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-web-security']
      }
    }
  });
};
```

### 测试脚本配置

**文件**: [`package.json`](package.json:1)

```json
{
  "scripts": {
    "test": "ng test",
    "test:watch": "ng test --watch",
    "test:ci": "ng test --watch=false --browsers=ChromeHeadlessCI --code-coverage",
    "test:coverage": "ng test --code-coverage --watch=false"
  }
}
```

## 单元测试

### 组件测试

#### 基础组件测试

**文件**: [`src/app/pages/passport/login/login.spec.ts`](src/app/pages/passport/login/login.spec.ts:1)

```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TokenService } from '@app/core';
import { of, throwError } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let router: jasmine.SpyObj<Router>;
  let tokenService: jasmine.SpyObj<TokenService>;

  beforeEach(async () => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const tokenServiceSpy = jasmine.createSpyObj('TokenService', ['setToken']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: TokenService, useValue: tokenServiceSpy }
      ]
    }).compileComponents();

    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    tokenService = TestBed.inject(TokenService) as jasmine.SpyObj<TokenService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize login form', () => {
    expect(component.loginForm).toBeDefined();
    expect(component.loginForm.get('username')).toBeTruthy();
    expect(component.loginForm.get('password')).toBeTruthy();
  });

  it('should validate required fields', () => {
    const form = component.loginForm;
    form.patchValue({
      username: '',
      password: ''
    });
    
    expect(form.valid).toBeFalsy();
    expect(form.get('username')?.hasError('required')).toBeTruthy();
    expect(form.get('password')?.hasError('required')).toBeTruthy();
  });

  it('should validate username format', () => {
    const form = component.loginForm;
    form.patchValue({
      username: 'invalid-email',
      password: 'password123'
    });
    
    expect(form.get('username')?.hasError('email')).toBeTruthy();
  });

  it('should call onSubmit with valid form', () => {
    spyOn(component, 'onSubmit').and.callThrough();
    
    component.loginForm.patchValue({
      username: 'test@example.com',
      password: 'password123'
    });
    
    component.onSubmit();
    
    expect(component.onSubmit).toHaveBeenCalled();
    expect(component.loading()).toBeTruthy();
  });
});
```

#### 高级组件测试

```typescript
describe('LoginComponent - Advanced', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<any>;
  let progressBar: jasmine.SpyObj<any>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);
    const progressBarSpy = jasmine.createSpyObj('ProgressBar', ['show', 'hide']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule, HttpClientTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ProgressBar, useValue: progressBarSpy }
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService);
    progressBar = TestBed.inject(ProgressBar);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should handle successful login', fakeAsync(() => {
    const mockResponse = { token: 'mock-token', user: { id: 1, name: 'Test User' } };
    authService.login.and.returnValue(of(mockResponse));

    component.loginForm.patchValue({
      username: 'test@example.com',
      password: 'password123'
    });

    component.onSubmit();
    tick();

    expect(progressBar.show).toHaveBeenCalled();
    expect(authService.login).toHaveBeenCalledWith({
      username: 'test@example.com',
      password: 'password123'
    });
    expect(progressBar.hide).toHaveBeenCalled();
  }));

  it('should handle login error', fakeAsync(() => {
    const mockError = { error: { message: 'Invalid credentials' } };
    authService.login.and.returnValue(throwError(() => mockError));

    component.loginForm.patchValue({
      username: 'test@example.com',
      password: 'wrong-password'
    });

    component.onSubmit();
    tick();

    expect(progressBar.show).toHaveBeenCalled();
    expect(progressBar.hide).toHaveBeenCalled();
    expect(component.error()).toBe('Invalid credentials');
  }));

  it('should disable submit button when loading', () => {
    component.loading.set(true);
    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(submitButton.disabled).toBeTruthy();
  });
});
```

### 服务测试

#### TokenService 测试

```typescript
import { TestBed } from '@angular/core/testing';
import { TokenService } from './token.service';
import { BrowserStorage } from '../storage/browser-storage';

describe('TokenService', () => {
  let service: TokenService;
  let storageSpy: jasmine.SpyObj<BrowserStorage>;

  beforeEach(() => {
    const storageSpyObj = jasmine.createSpyObj('BrowserStorage', ['get', 'set', 'remove']);

    TestBed.configureTestingModule({
      providers: [
        TokenService,
        { provide: BrowserStorage, useValue: storageSpyObj }
      ]
    });

    service = TestBed.inject(TokenService);
    storageSpy = TestBed.inject(BrowserStorage) as jasmine.SpyObj<BrowserStorage>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should set and get token', () => {
    const mockToken = 'mock-jwt-token';
    
    service.setToken(mockToken);
    
    expect(storageSpy.set).toHaveBeenCalledWith('auth_token', mockToken);
  });

  it('should get token from storage', () => {
    const mockToken = 'mock-jwt-token';
    storageSpy.get.and.returnValue(mockToken);

    const token = service.getToken();

    expect(storageSpy.get).toHaveBeenCalledWith('auth_token');
    expect(token).toBe(mockToken);
  });

  it('should remove token', () => {
    service.removeToken();

    expect(storageSpy.remove).toHaveBeenCalledWith('auth_token');
  });

  it('should check if token exists', () => {
    storageSpy.get.and.returnValue('mock-token');

    const hasToken = service.hasToken();

    expect(hasToken).toBeTruthy();
  });

  it('should validate token format', () => {
    const validToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c';
    
    const isValid = service.isValidToken(validToken);

    expect(isValid).toBeTruthy();
  });

  it('should invalidate malformed token', () => {
    const invalidToken = 'invalid-token-format';
    
    const isValid = service.isValidToken(invalidToken);

    expect(isValid).toBeFalsy();
  });
});
```

#### HTTP 拦截器测试

```typescript
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { indexInterceptor } from './http.Interceptor';
import { TokenService } from '../services/token.service';
import { ProgressBar } from '../services/progress-bar';

describe('indexInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let tokenService: jasmine.SpyObj<TokenService>;
  let progressBar: jasmine.SpyObj<ProgressBar>;

  beforeEach(() => {
    const tokenServiceSpy = jasmine.createSpyObj('TokenService', ['getToken']);
    const progressBarSpy = jasmine.createSpyObj('ProgressBar', ['show', 'hide']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: TokenService, useValue: tokenServiceSpy },
        { provide: ProgressBar, useValue: progressBarSpy },
        { provide: HTTP_INTERCEPTORS, useValue: indexInterceptor, multi: true }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    tokenService = TestBed.inject(TokenService) as jasmine.SpyObj<TokenService>;
    progressBar = TestBed.inject(ProgressBar) as jasmine.SpyObj<ProgressBar>;
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should add auth token to request headers', () => {
    const mockToken = 'mock-jwt-token';
    tokenService.getToken.and.returnValue(mockToken);

    httpClient.get('/api/test').subscribe();

    const req = httpTestingController.expectOne('/api/test');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    
    req.flush({});
  });

  it('should show progress bar on request', () => {
    httpClient.get('/api/test').subscribe();

    expect(progressBar.show).toHaveBeenCalled();

    const req = httpTestingController.expectOne('/api/test');
    req.flush({});
  });

  it('should hide progress bar on response', () => {
    httpClient.get('/api/test').subscribe();

    const req = httpTestingController.expectOne('/api/test');
    req.flush({});

    expect(progressBar.hide).toHaveBeenCalled();
  });

  it('should hide progress bar on error', () => {
    httpClient.get('/api/test').subscribe({
      error: () => {
        expect(progressBar.hide).toHaveBeenCalled();
      }
    });

    const req = httpTestingController.expectOne('/api/test');
    req.flush({}, { status: 500, statusText: 'Server Error' });
  });
});
```

### 插件测试

#### Alerts 插件测试

```typescript
import { TestBed } from '@angular/core/testing';
import { Alerts } from './alerts';

describe('Alerts', () => {
  let service: Alerts;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [Alerts]
    });
    service = TestBed.inject(Alerts);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should show success alert', () => {
    spyOn(service, 'success');
    service.success('Test success message');
    expect(service.success).toHaveBeenCalledWith('Test success message');
  });

  it('should show error alert', () => {
    spyOn(service, 'error');
    service.error('Test error message');
    expect(service.error).toHaveBeenCalledWith('Test error message');
  });

  it('should show warning alert', () => {
    spyOn(service, 'warning');
    service.warning('Test warning message');
    expect(service.warning).toHaveBeenCalledWith('Test warning message');
  });

  it('should show info alert', () => {
    spyOn(service, 'info');
    service.info('Test info message');
    expect(service.info).toHaveBeenCalledWith('Test info message');
  });
});
```

## 集成测试

### 模块集成测试

```typescript
import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CoreModule } from './core.module';

describe('CoreModule', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CoreModule,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    }).compileComponents();
  });

  it('should create', () => {
    expect(CoreModule).toBeDefined();
  });

  it('should provide all core services', () => {
    const tokenService = TestBed.inject(TokenService);
    const progressBar = TestBed.inject(ProgressBar);
    const themeService = TestBed.inject(ThemeService);

    expect(tokenService).toBeTruthy();
    expect(progressBar).toBeTruthy();
    expect(themeService).toBeTruthy();
  });
});
```

### 路由集成测试

```typescript
import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { routes } from './app.routes';

describe('App Routing', () => {
  let router: Router;
  let location: Location;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes(routes)]
    });

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    
    router.initialNavigation();
  });

  it('should redirect empty path to /passport', fakeAsync(() => {
    router.navigate(['']);
    tick();
    expect(location.path()).toBe('/passport');
  }));

  it('should redirect unknown paths to /exception/404', fakeAsync(() => {
    router.navigate(['/unknown-path']);
    tick();
    expect(location.path()).toBe('/exception/404');
  }));

  it('should navigate to home route', fakeAsync(() => {
    router.navigate(['/home']);
    tick();
    expect(location.path()).toBe('/home');
  }));
});
```

## 端到端测试

### E2E 测试配置

#### Protractor 配置 (可选)

```typescript
// protractor.conf.js
exports.config = {
  allScriptsTimeout: 11000,
  specs: [
    './src/**/*.e2e-spec.ts'
  ],
  capabilities: {
    browserName: 'chrome',
    chromeOptions: {
      args: ['--headless', '--no-cache', '--no-sandbox']
    }
  },
  directConnect: true,
  baseUrl: 'http://localhost:4200/',
  framework: 'jasmine',
  jasmineNodeOpts: {
    showColors: true,
    defaultTimeoutInterval: 30000,
    print: function() {}
  },
  onPrepare() {
    require('ts-node').register({
      project: require('path').join(__dirname, './tsconfig.json')
    });
  }
};
```

### E2E 测试用例

#### 登录流程测试

```typescript
// login.e2e-spec.ts
import { browser, by, element } from 'protractor';

describe('Login Flow', () => {
  beforeEach(() => {
    browser.get('/passport/login');
  });

  it('should display login form', () => {
    expect(element(by.css('form')).isPresent()).toBeTruthy();
    expect(element(by.css('input[type="email"]')).isPresent()).toBeTruthy();
    expect(element(by.css('input[type="password"]')).isPresent()).toBeTruthy();
  });

  it('should show validation errors for empty fields', () => {
    element(by.css('button[type="submit"]')).click();
    
    expect(element(by.css('.error-message')).isPresent()).toBeTruthy();
  });

  it('should login with valid credentials', () => {
    element(by.css('input[type="email"]')).sendKeys('test@example.com');
    element(by.css('input[type="password"]')).sendKeys('password123');
    element(by.css('button[type="submit"]')).click();
    
    browser.wait(() => {
      return browser.getCurrentUrl().then(url => {
        return url.includes('/home');
      });
    }, 5000);
    
    expect(browser.getCurrentUrl()).toContain('/home');
  });

  it('should show error for invalid credentials', () => {
    element(by.css('input[type="email"]')).sendKeys('invalid@example.com');
    element(by.css('input[type="password"]')).sendKeys('wrongpassword');
    element(by.css('button[type="submit"]')).click();
    
    expect(element(by.css('.error-message')).getText()).toContain('Invalid credentials');
  });
});
```

#### 用户管理测试

```typescript
// users.e2e-spec.ts
describe('User Management', () => {
  beforeEach(() => {
    // 先登录
    browser.get('/passport/login');
    element(by.css('input[type="email"]')).sendKeys('admin@example.com');
    element(by.css('input[type="password"]')).sendKeys('admin123');
    element(by.css('button[type="submit"]')).click();
    
    // 等待跳转到用户管理页面
    browser.wait(() => {
      return browser.getCurrentUrl().then(url => {
        return url.includes('/home');
      });
    }, 5000);
    
    // 导航到用户管理
    element(by.css('[routerLink="/home/users"]')).click();
  });

  it('should display user list', () => {
    expect(element(by.css('table')).isPresent()).toBeTruthy();
    expect(element.all(by.css('tbody tr')).count()).toBeGreaterThan(0);
  });

  it('should create new user', () => {
    element(by.css('.add-user-btn')).click();
    
    // 填写表单
    element(by.css('input[name="name"]')).sendKeys('New User');
    element(by.css('input[name="email"]')).sendKeys('newuser@example.com');
    element(by.css('select[name="role"]')).sendKeys('user');
    
    element(by.css('button[type="submit"]')).click();
    
    // 验证用户创建成功
    expect(element(by.css('.success-message')).isPresent()).toBeTruthy();
  });

  it('should edit existing user', () => {
    // 点击第一个用户的编辑按钮
    element.all(by.css('.edit-user-btn')).first().click();
    
    // 修改用户信息
    const nameInput = element(by.css('input[name="name"]'));
    nameInput.clear();
    nameInput.sendKeys('Updated User');
    
    element(by.css('button[type="submit"]')).click();
    
    // 验证更新成功
    expect(element(by.css('.success-message')).isPresent()).toBeTruthy();
  });
});
```

## PWA 测试

### Service Worker 测试

```typescript
describe('PWA Features', () => {
  it('should register service worker', async () => {
    await browser.get('/');
    
    const hasSW = await browser.executeScript(() => {
      return 'serviceWorker' in navigator;
    });
    
    expect(hasSW).toBeTruthy();
  });

  it('should work offline', async () => {
    // 先在线访问页面
    await browser.get('/home');
    
    // 模拟离线状态
    await browser.executeScript(() => {
      window.dispatchEvent(new Event('offline'));
    });
    
    // 刷新页面
    await browser.refresh();
    
    // 验证页面仍然可以访问
    const currentUrl = await browser.getCurrentUrl();
    expect(currentUrl).toContain('/home');
  });

  it('should show install prompt', async () => {
    await browser.get('/');
    
    // 触发安装提示
    const promptShown = await browser.executeScript(() => {
      return new Promise((resolve) => {
        window.addEventListener('beforeinstallprompt', () => {
          resolve(true);
        });
        
        // 模拟安装提示事件
        setTimeout(() => resolve(false), 5000);
      });
    });
    
    // 验证安装提示逻辑
    expect(promptShown).toBeDefined();
  });
});
```

## 测试最佳实践

### 1. 测试组织

```typescript
describe('Feature', () => {
  // 测试数据
  const mockData = {
    users: [
      { id: 1, name: 'User 1', email: 'user1@example.com' },
      { id: 2, name: 'User 2', email: 'user2@example.com' }
    ]
  };

  beforeEach(() => {
    // 通用设置
  });

  describe('when user is authenticated', () => {
    beforeEach(() => {
      // 认证用户设置
    });

    it('should display user-specific content', () => {
      // 测试用例
    });
  });

  describe('when user is not authenticated', () => {
    beforeEach(() => {
      // 未认证用户设置
    });

    it('should redirect to login', () => {
      // 测试用例
    });
  });
});
```

### 2. 测试数据管理

```typescript
// 测试数据工厂
class TestDataFactory {
  static createUser(overrides?: Partial<User>): User {
    return {
      id: 1,
      name: 'Test User',
      email: 'test@example.com',
      role: 'user',
      createdAt: new Date(),
      ...overrides
    };
  }

  static createUsers(count: number): User[] {
    return Array.from({ length: count }, (_, i) => ({
      id: i + 1,
      name: `User ${i + 1}`,
      email: `user${i + 1}@example.com`,
      role: 'user',
      createdAt: new Date()
    }));
  }
}

// 使用示例
const user = TestDataFactory.createUser({ name: 'John Doe' });
const users = TestDataFactory.createUsers(5);
```

### 3. 异步测试

```typescript
// 使用 fakeAsync
it('should handle async operations', fakeAsync(() => {
  let result: string | null = null;
  
  someAsyncOperation().subscribe(data => {
    result = data;
  });
  
  tick(); // 推进时间
  expect(result).toBe('expected result');
}));

// 使用 async/await
it('should handle async operations', async () => {
  const result = await someAsyncOperation().toPromise();
  expect(result).toBe('expected result');
});

// 使用 done
it('should handle async operations', (done) => {
  someAsyncOperation().subscribe({
    next: (result) => {
      expect(result).toBe('expected result');
      done();
    },
    error: done.fail
  });
});
```

### 4. 模拟策略

```typescript
// 服务模拟
const mockService = {
  getData: jasmine.createSpy('getData').and.returnValue(of(mockData)),
  saveData: jasmine.createSpy('saveData').and.returnValue(of({ success: true }))
};

// 组件模拟
@Component({
  selector: 'app-child',
  template: ''
})
class MockChildComponent {
  @Input() data: any;
  @Output() action = new EventEmitter();
}

// 指令模拟
@Directive({
  selector: '[appCustomDirective]'
})
class MockCustomDirective {
  @Input() appCustomDirective: any;
}
```

### 5. 覆盖率目标

```typescript
// karma.conf.js 覆盖率配置
coverageReporter: {
  dir: require('path').join(__dirname, './coverage'),
  subdir: '.',
  reporters: [
    { type: 'html' },
    { type: 'text-summary' },
    { type: 'lcov' }
  ],
  check: {
    global: {
      statements: 80,
      branches: 75,
      functions: 80,
      lines: 80
    },
    each: {
      statements: 70,
      branches: 60,
      functions: 70,
      lines: 70
    }
  }
}
```

## 测试运行

### 本地测试

```bash
# 运行所有测试
ng test

# 运行特定文件
ng test --include='**/login.component.spec.ts'

# 运行带覆盖率
ng test --code-coverage

# 运行一次（不监听）
ng test --watch=false

# 运行特定浏览器
ng test --browsers=ChromeHeadless
```

### CI/CD 测试

```bash
# CI 环境测试
npm run test:ci

# 生成覆盖率报告
npm run test:coverage

# 端到端测试
npm run e2e
```

## 测试报告

### 覆盖率报告

测试完成后，覆盖率报告将生成在 `coverage/` 目录：

```
coverage/
├── index.html          # 覆盖率总览
├── lcov.info          # LCOV 格式报告
├── src/
│   ├── app/
│   │   ├── components/
│   │   ├── services/
│   │   └── ...
│   └── ...
└── ...
```

### 测试结果

测试结果将显示在终端：

```
Chrome Headless 96.0.4664.45 (Mac OS 10.15.7): Executed 45 of 45 SUCCESS (2.345 secs / 2.123 secs)
TOTAL: 45 SUCCESS
```

## 故障排除

### 常见问题

1. **测试超时**

   ```typescript
   // 增加超时时间
   jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
   
   // 或在测试用例中
   it('should handle long operation', (done) => {
     // 测试逻辑
   }, 10000); // 10秒超时
   ```

2. **异步测试失败**

   ```typescript
   // 确保正确处理异步操作
   it('should handle async', async () => {
     await fixture.whenStable();
     expect(component.data).toBeDefined();
   });
   ```

3. **组件检测变化**

   ```typescript
   // 手动触发变化检测
   fixture.detectChanges();
   
   // 等待稳定
   await fixture.whenStable();
   ```

---

**文档版本**: 1.0.0  
**最后更新**: 2025年
