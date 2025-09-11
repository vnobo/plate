# 服务 API 文档

## 概述

本文档详细描述了 Angular v20 Web 应用程序中所有核心服务的 API、使用方法和实现细节。

## 核心服务

### TokenService

**文件**: [`src/app/core/services/token.service.ts`](src/app/core/services/token.service.ts:1)

管理用户认证令牌的服务。

#### API 接口

```typescript
class TokenService {
  // 获取当前令牌
  getToken(): string | null
  
  // 设置令牌
  setToken(token: string): void
  
  // 移除令牌
  removeToken(): void
  
  // 检查令牌是否存在
  hasToken(): boolean
  
  // 验证令牌有效性
  isValidToken(token: string): boolean
}
```

#### 使用方法

```typescript
import { TokenService } from '@app/core';

export class LoginComponent {
  private readonly tokenService = inject(TokenService);
  
  onLoginSuccess(token: string) {
    this.tokenService.setToken(token);
  }
  
  onLogout() {
    this.tokenService.removeToken();
  }
}
```

#### 存储机制

- 使用 `BrowserStorage` 进行持久化存储
- 默认存储在 `localStorage` 中
- 支持会话存储选项

### PageTitleStrategy

**文件**: [`src/app/core/services/page-title-strategy.ts`](src/app/core/services/page-title-strategy.ts:1)

管理页面标题的策略服务。

#### API 接口

```typescript
class PageTitleStrategy {
  // 更新页面标题
  updateTitle(title: string): void
  
  // 获取当前标题
  getCurrentTitle(): string
  
  // 重置为默认标题
  resetTitle(): void
}
```

#### 配置方法

在路由配置中设置标题：

```typescript
const routes: Routes = [
  {
    path: 'dashboard',
    component: DashboardComponent,
    data: { title: '仪表板' }
  }
];
```

### ProgressBar

**文件**: [`src/app/core/services/progress-bar.ts`](src/app/core/services/progress-bar.ts:1)

全局进度条控制服务。

#### API 接口

```typescript
class ProgressBar {
  // 显示进度条
  show(): void
  
  // 隐藏进度条
  hide(): void
  
  // 切换进度条状态
  toggle(): void
  
  // 获取显示状态
  isVisible(): boolean
  
  // 监听显示状态变化
  isShow$: Observable<boolean>
}
```

#### 使用示例

```typescript
import { ProgressBar } from '@app/core';

export class DataService {
  private readonly progressBar = inject(ProgressBar);
  
  loadData() {
    this.progressBar.show();
    
    this.http.get('/api/data').pipe(
      finalize(() => this.progressBar.hide())
    ).subscribe(data => {
      // 处理数据
    });
  }
}
```

### ThemeService

**文件**: [`src/app/core/services/theme.service.ts`](src/app/core/services/theme.service.ts:1)

主题管理服务的接口定义。

#### API 接口

```typescript
class ThemeService {
  // 获取当前主题
  getCurrentTheme(): string
  
  // 设置主题
  setTheme(theme: string): void
  
  // 获取可用主题列表
  getAvailableThemes(): string[]
  
  // 监听主题变化
  themeChange$: Observable<string>
}
```

#### 主题配置

支持的主题选项：

- `light` - 浅色主题
- `dark` - 深色主题
- `slate` - 石板主题

### HTTP 拦截器

**文件**: [`src/app/core/net/http.Interceptor.ts`](src/app/core/net/http.Interceptor.ts:1)

HTTP 请求和响应的拦截处理。

#### 功能特性

1. **Token 注入**

   ```typescript
   // 自动在请求头中添加认证令牌
   headers: {
     'Authorization': `Bearer ${token}`
   }
   ```

2. **进度条控制**
   - 请求开始时显示进度条
   - 响应完成后隐藏进度条

3. **错误处理**
   - HTTP 错误统一处理
   - 用户友好的错误提示

4. **XSRF 防护**
   - 自动处理 XSRF-TOKEN
   - 请求头中添加 X-XSRF-TOKEN

#### 配置示例

```typescript
// 在 app.config.ts 中配置
provideHttpClient(
  withInterceptors(indexInterceptor),
  withXsrfConfiguration({
    cookieName: 'XSRF-TOKEN',
    headerName: 'X-XSRF-TOKEN',
  })
)
```

## 存储服务

### BrowserStorage

**文件**: [`src/app/core/storage/browser-storage.ts`](src/app/core/storage/browser-storage.ts:1)

浏览器存储的抽象基类。

#### API 接口

```typescript
abstract class BrowserStorage {
  // 获取值
  get<T>(key: string): T | null
  
  // 设置值
  set<T>(key: string, value: T): void
  
  // 删除值
  remove(key: string): void
  
  // 清空所有值
  clear(): void
  
  // 检查键是否存在
  has(key: string): boolean
  
  // 获取所有键
  keys(): string[]
}
```

#### 存储类型

支持两种存储类型：

1. **LocalStorage** - 持久化存储

   ```typescript
   export class LocalStorageService extends BrowserStorage {
     protected storage = localStorage;
   }
   ```

2. **SessionStorage** - 会话存储

   ```typescript
   export class SessionStorageService extends BrowserStorage {
     protected storage = sessionStorage;
   }
   ```

### SessionStorageService

**文件**: [`src/app/core/storage/session-storage.ts`](src/app/core/storage/session-storage.ts:1)

会话存储的具体实现。

#### 使用示例

```typescript
import { SessionStorageService } from '@app/core';

export class UserService {
  private readonly storage = inject(SessionStorageService);
  private readonly USER_KEY = 'current_user';
  
  saveUser(user: User) {
    this.storage.set(this.USER_KEY, user);
  }
  
  getUser(): User | null {
    return this.storage.get<User>(this.USER_KEY);
  }
  
  clearUser() {
    this.storage.remove(this.USER_KEY);
  }
}
```

## 插件服务

### Alerts 服务

**文件**: [`src/app/plugins/alerts/alerts.ts`](src/app/plugins/alerts/alerts.ts:1)

警告消息显示服务。

#### API 接口

```typescript
class Alerts {
  // 显示成功警告
  success(message: string, options?: AlertOptions): void
  
  // 显示错误警告
  error(message: string, options?: AlertOptions): void
  
  // 显示警告信息
  warning(message: string, options?: AlertOptions): void
  
  // 显示信息警告
  info(message: string, options?: AlertOptions): void
}
```

#### 配置选项

```typescript
interface AlertOptions {
  duration?: number;        // 显示时长（毫秒）
  position?: AlertPosition; // 显示位置
  closable?: boolean;       // 是否可关闭
  autoClose?: boolean;      // 是否自动关闭
}
```

### Toasts 服务

**文件**: [`src/app/plugins/toasts/toasts.ts`](src/app/plugins/toasts/toasts.ts:1)

消息提示服务。

#### API 接口

```typescript
class Toasts {
  // 显示成功消息
  success(message: string, title?: string, options?: ToastOptions): void
  
  // 显示错误消息
  error(message: string, title?: string, options?: ToastOptions): void
  
  // 显示警告消息
  warning(message: string, title?: string, options?: ToastOptions): void
  
  // 显示信息消息
  info(message: string, title?: string, options?: ToastOptions): void
}
```

### Modals 服务

**文件**: [`src/app/plugins/modals/modals.ts`](src/app/plugins/modals/modals.ts:1)

模态框管理服务。

#### API 接口

```typescript
class ModalsService {
  // 打开模态框
  open<T>(component: Type<T>, options?: ModalOptions): ModalRef<T>
  
  // 关闭所有模态框
  closeAll(): void
}
```

#### 使用示例

```typescript
import { ModalsService } from '@app/plugins';

export class UserComponent {
  private readonly modals = inject(ModalsService);
  
  editUser(user: User) {
    const modalRef = this.modals.open(UserEditModal, {
      data: { user },
      size: 'lg',
      backdrop: true
    });
    
    modalRef.onClose.subscribe(result => {
      if (result) {
        // 处理编辑结果
      }
    });
  }
}
```

## 路由服务

### 路由守卫

**文件**: [`src/app/core/pages.guard.ts`](src/app/core/pages.guard.ts:1)

页面访问控制守卫。

#### 功能特性

1. **认证检查**
   - 验证用户是否已登录
   - 未登录用户重定向到登录页

2. **权限验证**
   - 检查用户权限
   - 基于角色的访问控制

3. **数据预加载**
   - 预加载路由所需数据
   - 提高页面加载性能

#### 使用示例

```typescript
const routes: Routes = [
  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [PagesGuard],
    data: { 
      requiresAuth: true,
      requiredRole: 'admin'
    }
  }
];
```

## 应用配置服务

### 应用配置

**文件**: [`src/app/app.config.ts`](src/app/app.config.ts:1)

应用程序的核心配置。

#### 主要配置项

1. **变更检测**

   ```typescript
   provideZonelessChangeDetection() // 无区域变更检测
   ```

2. **路由配置**

   ```typescript
   provideRouter(routes) // 路由配置
   ```

3. **HTTP 客户端**

   ```typescript
   provideHttpClient(
     withFetch(),                    // Fetch API
     withInterceptors(indexInterceptor), // 拦截器
     withXsrfConfiguration({...})    // XSRF 配置
   )
   ```

4. **Service Worker**

   ```typescript
   provideServiceWorker('ngsw-worker.js', {
     enabled: !isDevMode(),
     registrationStrategy: 'registerWhenStable:30000'
   })
   ```

5. **动画支持**

   ```typescript
   provideAnimationsAsync() // 异步动画
   ```

6. **本地化**

   ```typescript
   { provide: LOCALE_ID, useValue: 'zh_CN' } // 中文本地化
   ```

## 错误处理

### 全局错误处理

应用程序配置了全局错误监听器：

```typescript
provideBrowserGlobalErrorListeners()
```

### HTTP 错误处理

HTTP 拦截器统一处理网络错误：

```typescript
// 错误类型
interface HttpError {
  status: number;
  message: string;
  details?: any;
}

// 错误处理逻辑
handleError(error: HttpErrorResponse): Observable<never> {
  const errorMessage = this.getErrorMessage(error);
  this.alerts.error(errorMessage);
  return throwError(() => error);
}
```

## 性能监控

### 性能指标

服务层提供性能监控功能：

```typescript
// 请求计时
const startTime = performance.now();
this.http.get('/api/data').pipe(
  finalize(() => {
    const duration = performance.now() - startTime;
    console.log(`Request took ${duration}ms`);
  })
);
```

## 最佳实践

### 服务设计原则

1. **单一职责** - 每个服务只负责一个功能
2. **依赖注入** - 使用 Angular DI 系统
3. **错误处理** - 统一的错误处理机制
4. **类型安全** - 使用 TypeScript 类型
5. **可测试性** - 易于单元测试

### 使用模式

```typescript
// 推荐：使用 inject() 函数
export class Component {
  private readonly service = inject(MyService);
}

// 推荐：使用信号进行状态管理
export class Service {
  private readonly state = signal<State>(initialState);
  readonly state$ = this.state.asReadonly();
}

// 推荐：使用 RxJS 操作符
export class Service {
  getData(): Observable<Data> {
    return this.http.get<Data>('/api/data').pipe(
      retry(3),
      catchError(this.handleError),
      shareReplay(1)
    );
  }
}
```

---

**文档版本**: 1.0.0  
**最后更新**: 2025年
