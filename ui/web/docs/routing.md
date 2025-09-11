# 路由和导航文档

## 概述

本文档详细描述了 Angular v20 Web 应用程序的路由系统架构、配置和使用方法。路由系统采用模块化设计，支持懒加载、路由守卫和动态导航。

## 路由架构

### 路由结构

```
应用根路由
├── /passport (认证模块 - 懒加载)
│   ├── /login (登录页面)
│   └── /register (注册页面)
├── /home (主页模块 - 懒加载)
│   ├── /dashboard (仪表板)
│   └── /users (用户管理)
│       ├── /list (用户列表)
│       └── /edit/:id (用户编辑)
├── /exception (异常模块 - 懒加载)
│   ├── /404 (页面不存在)
│   ├── /500 (服务器错误)
│   └── /512 (权限不足)
└── / (根路径 - 重定向到 /passport)
```

### 路由配置

**主路由配置**: [`src/app/app.routes.ts`](src/app/app.routes.ts:1)

```typescript
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
    loadChildren: () => import('./pages/index').then(m => m.EXCEPTION_ROUTES) 
  },
  { 
    path: '', 
    pathMatch: 'full', 
    redirectTo: '/passport' 
  },
  { 
    path: '**', 
    redirectTo: 'exception/404' 
  },
];
```

## 模块路由配置

### 认证模块路由

**文件**: [`src/app/pages/passport/passport.ts`](src/app/pages/passport/passport.ts:1)

```typescript
export const PASSPORT_ROUTES: Routes = [
  {
    path: '',
    component: PassportComponent,
    children: [
      { path: 'login', component: LoginComponent },
      { path: 'register', component: RegisterComponent },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  }
];
```

### 主页模块路由

**文件**: [`src/app/pages/home/home.ts`](src/app/pages/home/home.ts:1)

```typescript
export const HOME_ROUTES: Routes = [
  {
    path: '',
    component: HomeComponent,
    canActivate: [PagesGuard], // 需要认证
    children: [
      { 
        path: 'dashboard', 
        component: DashboardComponent,
        data: { title: '仪表板' }
      },
      { 
        path: 'users', 
        loadChildren: () => import('./users/users').then(m => m.USER_ROUTES),
        data: { title: '用户管理' }
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];
```

### 用户管理子路由

**文件**: [`src/app/pages/home/users/users.ts`](src/app/pages/home/users/users.ts:1)

```typescript
export const USER_ROUTES: Routes = [
  {
    path: '',
    component: UsersComponent,
    children: [
      { path: 'list', component: UserListComponent },
      { path: 'edit/:id', component: UserEditComponent },
      { path: '', redirectTo: 'list', pathMatch: 'full' }
    ]
  }
];
```

### 异常模块路由

**文件**: [`src/app/pages/error/error.ts`](src/app/pages/error/error.ts:1)

```typescript
export const EXCEPTION_ROUTES: Routes = [
  {
    path: '',
    component: ErrorComponent,
    children: [
      { path: '404', component: Error404Component },
      { path: '500', component: Error500Component },
      { path: '512', component: Error512Component }
    ]
  }
];
```

## 路由守卫

### 页面守卫 (PagesGuard)

**文件**: [`src/app/core/pages.guard.ts`](src/app/core/pages.guard.ts:1)

主要的页面访问控制守卫。

#### 功能特性

1. **认证检查**

   ```typescript
   canActivate(
     route: ActivatedRouteSnapshot,
     state: RouterStateSnapshot
   ): Observable<boolean> | Promise<boolean> | boolean {
     // 检查用户是否已登录
     if (this.tokenService.hasToken()) {
       return true;
     }
     
     // 未登录则重定向到登录页
     this.router.navigate(['/passport/login'], {
       queryParams: { returnUrl: state.url }
     });
     return false;
   }
   ```

2. **权限验证**

   ```typescript
   // 在路由配置中指定所需角色
   {
     path: 'admin',
     component: AdminComponent,
     canActivate: [PagesGuard],
     data: { 
       requiresAuth: true,
       requiredRole: 'admin'
     }
   }
   ```

3. **数据预加载**

   ```typescript
   // 预加载用户数据
   private preloadUserData(): Observable<boolean> {
     return this.userService.getCurrentUser().pipe(
       map(user => {
         this.userService.setCurrentUser(user);
         return true;
       }),
       catchError(() => of(false))
     );
   }
   ```

### 路由守卫配置

在模块路由中使用守卫：

```typescript
const routes: Routes = [
  {
    path: 'protected',
    component: ProtectedComponent,
    canActivate: [PagesGuard],
    canDeactivate: [CanDeactivateGuard], // 离开守卫
    resolve: { data: DataResolver } // 数据预加载
  }
];
```

## 路由数据 (Route Data)

### 静态数据配置

```typescript
{
  path: 'dashboard',
  component: DashboardComponent,
  data: {
    title: '仪表板',           // 页面标题
    requiresAuth: true,       // 需要认证
    requiredRole: 'user',     // 所需角色
    breadcrumb: '首页'        // 面包屑导航
  }
}
```

### 动态数据获取

```typescript
export class DataResolver implements Resolve<Data> {
  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<Data> {
    return this.dataService.getData(route.params['id']);
  }
}
```

## 导航服务

### Router 服务使用

```typescript
import { Router } from '@angular/router';

export class NavigationService {
  private readonly router = inject(Router);
  
  // 基本导航
  navigateToDashboard() {
    this.router.navigate(['/home/dashboard']);
  }
  
  // 带参数导航
  navigateToUserEdit(userId: string) {
    this.router.navigate(['/home/users/edit', userId]);
  }
  
  // 相对导航
  navigateToSibling() {
    this.router.navigate(['../sibling'], { relativeTo: this.route });
  }
  
  // 查询参数导航
  navigateWithParams() {
    this.router.navigate(['/search'], {
      queryParams: { q: 'angular', page: 1 }
    });
  }
}
```

### ActivatedRoute 服务

```typescript
import { ActivatedRoute } from '@angular/router';

export class UserComponent {
  private readonly route = inject(ActivatedRoute);
  
  ngOnInit() {
    // 获取路由参数
    this.route.params.subscribe(params => {
      this.userId = params['id'];
    });
    
    // 获取查询参数
    this.route.queryParams.subscribe(queryParams => {
      this.page = queryParams['page'];
    });
    
    // 获取路由数据
    this.route.data.subscribe(data => {
      this.title = data['title'];
    });
  }
}
```

## 懒加载配置

### 模块懒加载

```typescript
{
  path: 'admin',
  loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule)
}
```

### 组件懒加载

```typescript
{
  path: 'feature',
  loadComponent: () => import('./feature/feature.component').then(c => c.FeatureComponent)
}
```

## 预加载策略

### 自定义预加载策略

```typescript
@Injectable({
  providedIn: 'root'
})
export class SelectivePreloadingStrategy implements PreloadingStrategy {
  preload(route: Route, load: () => Observable<any>): Observable<any> {
    // 只预加载标记为 preload 的路由
    return route.data && route.data['preload'] ? load() : of(null);
  }
}
```

### 配置预加载

```typescript
// 在 app.config.ts 中配置
provideRouter(routes, 
  withPreloading(SelectivePreloadingStrategy)
)
```

## 路由动画

### 路由动画配置

```typescript
// route-animations.ts
export const routeSlideAnimation = trigger('routeSlideAnimation', [
  transition('* <=> *', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%'
      })
    ], { optional: true }),
    query(':enter', [
      style({ transform: 'translateX(100%)' })
    ], { optional: true }),
    query(':leave', [
      style({ transform: 'translateX(0%)' })
    ], { optional: true }),
    group([
      query(':leave', [
        animate('300ms ease-out', style({ transform: 'translateX(-100%)' }))
      ], { optional: true }),
      query(':enter', [
        animate('300ms ease-out', style({ transform: 'translateX(0%)' }))
      ], { optional: true })
    ])
  ])
]);
```

### 在组件中使用

```typescript
@Component({
  selector: 'app-root',
  template: `
    <div [@routeSlideAnimation]="prepareRoute(outlet)">
      <router-outlet #outlet="outlet"></router-outlet>
    </div>
  `,
  animations: [routeSlideAnimation]
})
export class AppComponent {
  prepareRoute(outlet: RouterOutlet) {
    return outlet && outlet.activatedRouteData && outlet.activatedRouteData['animation'];
  }
}
```

## 面包屑导航

### 面包屑服务

```typescript
@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {
  private breadcrumbs = signal<Breadcrumb[]>([]);
  
  generateBreadcrumbs(route: ActivatedRouteSnapshot): Breadcrumb[] {
    const breadcrumbs: Breadcrumb[] = [];
    let currentRoute = route;
    
    while (currentRoute) {
      if (currentRoute.data['breadcrumb']) {
        breadcrumbs.push({
          label: currentRoute.data['breadcrumb'],
          url: currentRoute.pathFromRoot.map(r => r.url).join('/')
        });
      }
      currentRoute = currentRoute.firstChild;
    }
    
    return breadcrumbs;
  }
}
```

### 面包屑组件

```typescript
@Component({
  selector: 'app-breadcrumb',
  template: `
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li class="breadcrumb-item">
          <a routerLink="/">首页</a>
        </li>
        <li class="breadcrumb-item" *ngFor="let item of breadcrumbs()">
          <a [routerLink]="item.url">{{ item.label }}</a>
        </li>
      </ol>
    </nav>
  `
})
export class BreadcrumbComponent {
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly route = inject(ActivatedRoute);
  
  breadcrumbs = signal<Breadcrumb[]>([]);
  
  ngOnInit() {
    this.breadcrumbs.set(
      this.breadcrumbService.generateBreadcrumbs(this.route.snapshot)
    );
  }
}
```

## 路由事件监听

### 路由事件处理

```typescript
import { NavigationStart, NavigationEnd, NavigationError } from '@angular/router';

export class AppComponent {
  private readonly router = inject(Router);
  
  ngOnInit() {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        // 导航开始
        this.loadingService.show();
      }
      
      if (event instanceof NavigationEnd) {
        // 导航完成
        this.loadingService.hide();
        this.analytics.trackPageView(event.url);
      }
      
      if (event instanceof NavigationError) {
        // 导航错误
        this.loadingService.hide();
        this.errorHandler.handleError(event.error);
      }
    });
  }
}
```

## 路由参数处理

### 必需参数

```typescript
// 路由配置
{ path: 'user/:id', component: UserComponent }

// 参数获取
this.route.params.subscribe(params => {
  this.userId = params['id']; // 必需参数
});
```

### 可选参数

```typescript
// 导航时添加可选参数
this.router.navigate(['/user', userId], {
  queryParams: { tab: 'profile', edit: true }
});

// 获取可选参数
this.route.queryParams.subscribe(params => {
  this.activeTab = params['tab'] || 'default';
  this.isEditMode = params['edit'] === 'true';
});
```

### 矩阵参数

```typescript
// 路由配置
{ path: 'user/:id', component: UserComponent }

// 导航时使用矩阵参数
this.router.navigate(['/user', { id: userId, tab: 'profile' }]);

// 获取矩阵参数
this.route.paramMap.subscribe(params => {
  const id = params.get('id');
  const tab = params.get('tab');
});
```

## 路由状态管理

### 路由状态保存

```typescript
@Injectable({
  providedIn: 'root'
})
export class RouteStateService {
  private routeStates = new Map<string, any>();
  
  saveRouteState(url: string, state: any) {
    this.routeStates.set(url, state);
  }
  
  getRouteState(url: string): any {
    return this.routeStates.get(url);
  }
  
  clearRouteState(url: string) {
    this.routeStates.delete(url);
  }
}
```

## 路由测试

### 路由守卫测试

```typescript
describe('PagesGuard', () => {
  let guard: PagesGuard;
  let router: Router;
  let tokenService: TokenService;
  
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PagesGuard, TokenService]
    });
    
    guard = TestBed.inject(PagesGuard);
    router = TestBed.inject(Router);
    tokenService = TestBed.inject(TokenService);
  });
  
  it('should allow access when user is authenticated', () => {
    spyOn(tokenService, 'hasToken').and.returnValue(true);
    
    const result = guard.canActivate(
      {} as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot
    );
    
    expect(result).toBe(true);
  });
  
  it('should redirect to login when user is not authenticated', () => {
    spyOn(tokenService, 'hasToken').and.returnValue(false);
    spyOn(router, 'navigate');
    
    const result = guard.canActivate(
      {} as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot
    );
    
    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/passport/login']);
  });
});
```

## 性能优化

### 路由级代码分割

```typescript
// 使用动态导入实现代码分割
{
  path: 'heavy-feature',
  loadChildren: () => import('./heavy-feature/heavy-feature.module').then(m => m.HeavyFeatureModule)
}
```

### 预加载策略优化

```typescript
@Injectable({
  providedIn: 'root'
})
export class OptimizedPreloadingStrategy implements PreloadingStrategy {
  preload(route: Route, load: () => Observable<any>): Observable<any> {
    // 基于网络条件和用户行为智能预加载
    return this.connectionService.isOnline() && 
           this.userService.isActiveUser() ? 
           load() : of(null);
  }
}
```

## 最佳实践

### 1. 路由命名规范

```typescript
// 好的命名
{ path: 'user-profile', component: UserProfileComponent }
{ path: 'product/:id/reviews', component: ProductReviewsComponent }

// 避免
{ path: 'up', component: UserProfileComponent }
{ path: 'p/:id/r', component: ProductReviewsComponent }
```

### 2. 路由组织

```typescript
// 按功能模块组织路由
const USER_ROUTES: Routes = [
  { path: 'list', component: UserListComponent },
  { path: 'create', component: UserCreateComponent },
  { path: 'edit/:id', component: UserEditComponent },
  { path: 'detail/:id', component: UserDetailComponent }
];
```

### 3. 路由守卫组合

```typescript
{
  path: 'admin',
  component: AdminComponent,
  canActivate: [AuthGuard, RoleGuard, FeatureFlagGuard],
  data: {
    requiredRole: 'admin',
    requiredFeature: 'admin-panel'
  }
}
```

### 4. 错误处理

```typescript
{
  path: '**',
  component: NotFoundComponent,
  data: {
    title: '页面未找到',
    breadcrumb: '404错误'
  }
}
```

---

**文档版本**: 1.0.0  
**最后更新**: 2025年
