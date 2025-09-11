# 故障排除和 FAQ 文档

## 概述

本文档提供了 Angular v20 Web 应用程序常见问题的解决方案和故障排除指南。

## 常见问题 (FAQ)

### 1. 安装和构建问题

#### Q: 安装依赖时出现 `npm ERR! code ERESOLVE` 错误怎么办？

**A:** 这通常是由于依赖版本冲突引起的。尝试以下解决方案：

```bash
# 清理 npm 缓存
npm cache clean --force

# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安装依赖
npm install

# 如果仍然有问题，尝试使用 --legacy-peer-deps
npm install --legacy-peer-deps
```

#### Q: 构建时出现内存不足错误 `JavaScript heap out of memory` 怎么办？

**A:** 增加 Node.js 内存限制：

```bash
# 临时解决方案
export NODE_OPTIONS="--max-old-space-size=4096"
ng build --configuration=production

# 永久解决方案（Linux/Mac）
echo "export NODE_OPTIONS=\"--max-old-space-size=4096\"" >> ~/.bashrc
source ~/.bashrc

# Windows
setx NODE_OPTIONS "--max-old-space-size=4096"
```

#### Q: 构建速度很慢怎么办？

**A:** 尝试以下优化方法：

```bash
# 使用增量构建
ng build --watch

# 禁用源映射（开发时）
ng build --source-map=false

# 使用并行构建
ng build --build-optimizer=false

# 清理缓存
ng cache clean
```

### 2. 开发服务器问题

#### Q: `ng serve` 启动失败怎么办？

**A:** 检查以下常见问题：

```bash
# 检查端口是否被占用
netstat -ano | findstr :4200  # Windows
lsof -i :4200                 # Mac/Linux

# 使用不同端口
ng serve --port 4201

# 检查 Node.js 版本
node --version  # 需要 v18+

# 重新安装 Angular CLI
npm uninstall -g @angular/cli
npm install -g @angular/cli@latest
```

#### Q: 热重载不工作怎么办？

**A:** 尝试以下解决方案：

```bash
# 强制重新启动
ng serve --live-reload=true --poll=2000

# 检查文件监听限制（Linux）
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf
sudo sysctl -p

# 禁用防病毒软件（Windows）
# 某些防病毒软件会干扰文件监听
```

### 3. 路由和导航问题

#### Q: 路由刷新页面出现 404 错误怎么办？

**A:** 这是单页应用的常见问题，需要配置服务器重定向：

**Nginx 配置：**

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

**Apache 配置：**

```apache
RewriteEngine On
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule . /index.html [L]
```

**Node.js/Express 配置：**

```typescript
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});
```

#### Q: 路由守卫不工作怎么办？

**A:** 检查以下常见问题：

```typescript
// 确保正确实现 CanActivate 接口
export class AuthGuard implements CanActivate {
  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    // 返回布尔值或 Observable/Promise
    return this.authService.isAuthenticated();
  }
}

// 在路由配置中正确使用
const routes: Routes = [
  {
    path: 'protected',
    component: ProtectedComponent,
    canActivate: [AuthGuard]  // 确保守卫已提供
  }
];

// 在模块中提供守卫
@NgModule({
  providers: [AuthGuard]  // 确保在正确的模块中提供
})
```

### 4. Service Worker 和 PWA 问题

#### Q: Service Worker 不注册怎么办？

**A:** 检查以下条件：

```typescript
// 1. 确保在生产环境启用
provideServiceWorker('ngsw-worker.js', {
  enabled: !isDevMode(),  // 开发环境默认禁用
  registrationStrategy: 'registerWhenStable:30000'
})

// 2. 检查 HTTPS（生产环境需要）
// localhost 除外

// 3. 检查浏览器支持
if ('serviceWorker' in navigator) {
  // 浏览器支持 Service Worker
}

// 4. 手动注册测试
navigator.serviceWorker.register('/ngsw-worker.js')
  .then(registration => console.log('SW registered:', registration))
  .catch(error => console.log('SW registration failed:', error));
```

#### Q: 离线功能不工作怎么办？

**A:** 检查 Service Worker 配置：

```json
// ngsw-config.json
{
  "assetGroups": [
    {
      "name": "app",
      "installMode": "prefetch",  // 确保资源被缓存
      "resources": {
        "files": [
          "/index.html",
          "/*.css",
          "/*.js"
        ]
      }
    }
  ]
}
```

### 5. HTTP 请求问题

#### Q: CORS 错误怎么办？

**A:** 这是后端配置问题，需要后端支持：

**后端解决方案（Node.js/Express）：**

```javascript
const cors = require('cors');
app.use(cors({
  origin: ['http://localhost:4200', 'https://yourdomain.com'],
  credentials: true
}));
```

**开发时代理配置（proxy.conf.json）：**

```json
{
  "/api": {
    "target": "http://localhost:3000",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  }
}
```

#### Q: HTTP 拦截器不工作怎么办？

**A:** 检查拦截器配置：

```typescript
// 1. 确保在 app.config.ts 中正确配置
export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors(indexInterceptor),  // 使用函数形式
      withInterceptorsFromDi()  // 或使用依赖注入
    )
  ]
};

// 2. 确保拦截器正确导出
export const indexInterceptor: HttpInterceptorFn = (req, next) => {
  // 拦截器逻辑
  return next(req);
};
```

## 性能问题

### 1. 应用加载缓慢

#### 诊断步骤

```typescript
// 性能监控服务
@Injectable({
  providedIn: 'root'
})
export class PerformanceMonitorService {
  measurePerformance(): void {
    // 测量首屏加载时间
    window.addEventListener('load', () => {
      const perfData = performance.getEntriesByType('navigation')[0];
      console.log('DNS 查询时间:', perfData.domainLookupEnd - perfData.domainLookupStart);
      console.log('TCP 连接时间:', perfData.connectEnd - perfData.connectStart);
      console.log('HTTP 请求时间:', perfData.responseEnd - perfData.requestStart);
      console.log('DOM 解析时间:', perfData.domComplete - perfData.domLoading);
      console.log('资源加载时间:', perfData.loadEventEnd - perfData.loadEventStart);
    });

    // 测量资源加载
    performance.getEntriesByType('resource').forEach(resource => {
      if (resource.duration > 1000) { // 超过 1 秒
        console.warn('慢资源:', resource.name, resource.duration);
      }
    });
  }
}
```

#### 优化建议

1. **启用生产模式**

```typescript
import { enableProdMode } from '@angular/core';

if (environment.production) {
  enableProdMode();
}
```

2. **使用懒加载**

```typescript
const routes: Routes = [
  {
    path: 'feature',
    loadChildren: () => import('./feature/feature.module').then(m => m.FeatureModule)
  }
];
```

3. **优化第三方库**

```json
// angular.json
{
  "buildOptimizer": true,
  "optimization": true,
  "vendorChunk": true,
  "commonChunk": true
}
```

### 2. 内存泄漏

#### 常见内存泄漏源

```typescript
// 1. 未取消的订阅
export class MyComponent implements OnDestroy {
  private destroy$ = new Subject<void>();
  
  ngOnInit() {
    // 错误：没有取消订阅
    this.service.getData().subscribe(data => {
      this.data = data;
    });
    
    // 正确：使用 takeUntil
    this.service.getData()
      .pipe(takeUntil(this.destroy$))
      .subscribe(data => {
        this.data = data;
      });
  }
  
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// 2. 未清理的事件监听器
export class MyComponent implements OnDestroy {
  private resizeListener: () => void;
  
  constructor() {
    // 错误：没有清理监听器
    window.addEventListener('resize', this.onResize.bind(this));
    
    // 正确：保存引用并清理
    this.resizeListener = this.onResize.bind(this);
    window.addEventListener('resize', this.resizeListener);
  }
  
  ngOnDestroy() {
    window.removeEventListener('resize', this.resizeListener);
  }
  
  onResize() {
    // 处理窗口大小变化
  }
}

// 3. 未清理的定时器
export class MyComponent implements OnDestroy {
  private intervalId: number;
  
  ngOnInit() {
    // 错误：没有清理定时器
    setInterval(() => {
      this.doSomething();
    }, 1000);
    
    // 正确：保存引用并清理
    this.intervalId = window.setInterval(() => {
      this.doSomething();
    }, 1000);
  }
  
  ngOnDestroy() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }
  
  doSomething() {
    // 定时任务
  }
}
```

### 3. 变更检测性能

#### 优化策略

```typescript
// 1. 使用 OnPush 策略
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MyComponent {
  // 组件逻辑
}

// 2. 手动触发变更检测
export class MyComponent {
  constructor(private cdr: ChangeDetectorRef) {}
  
  updateData() {
    this.data = newData;
    this.cdr.detectChanges(); // 手动触发
  }
}

// 3. 使用信号 (Angular v20+)
export class MyComponent {
  data = signal(initialData);
  
  updateData() {
    this.data.set(newData); // 自动优化
  }
}
```

## 错误处理

### 1. 全局错误处理

```typescript
// 全局错误处理器
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  handleError(error: Error): void {
    console.error('Global error:', error);
    
    // 发送错误到服务器
    this.errorReportingService.reportError(error);
    
    // 显示用户友好的错误信息
    this.showErrorNotification(error);
  }
  
  private showErrorNotification(error: Error): void {
    // 使用插件系统显示错误
    this.alerts.error('发生错误，请稍后重试');
  }
}

// 在 app.config.ts 中配置
export const appConfig: ApplicationConfig = {
  providers: [
    { provide: ErrorHandler, useClass: GlobalErrorHandler }
  ]
};
```

### 2. HTTP 错误处理

```typescript
// HTTP 错误拦截器
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = '发生未知错误';
      
      switch (error.status) {
        case 400:
          errorMessage = '请求参数错误';
          break;
        case 401:
          errorMessage = '未授权，请重新登录';
          // 重定向到登录页
          break;
        case 403:
          errorMessage = '权限不足';
          break;
        case 404:
          errorMessage = '请求的资源不存在';
          break;
        case 500:
          errorMessage = '服务器内部错误';
          break;
        case 503:
          errorMessage = '服务不可用';
          break;
        default:
          errorMessage = `错误代码: ${error.status}`;
      }
      
      // 显示错误信息
      inject(Alerts).error(errorMessage);
      
      return throwError(() => error);
    })
  );
};
```

## 调试技巧

### 1. Angular DevTools

```typescript
// 安装 Angular DevTools 浏览器扩展
// 启用调试模式
if (typeof window !== 'undefined') {
  (window as any).__ngDevMode = true;
}

// 使用 ng.probe (旧版本)
const element = document.querySelector('app-root');
const component = (window as any).ng.probe(element);
console.log('Component instance:', component);

// 使用调试工具
@Component({
  selector: 'app-debug'
})
export class DebugComponent {
  ngAfterViewInit() {
    // 添加调试信息
    console.log('Component initialized:', this);
    
    // 检查变更检测
    console.log('Change detection count:', (window as any).ng.getDebugNode(this.elementRef.nativeElement));
  }
}
```

### 2. 性能调试

```typescript
// 性能分析工具
export class PerformanceDebugger {
  measureComponentPerformance(): void {
    // 测量组件初始化时间
    const startTime = performance.now();
    
    // 组件逻辑
    
    const endTime = performance.now();
    console.log(`Component initialization: ${endTime - startTime}ms`);
  }
  
  measureChangeDetection(): void {
    const originalDetectChanges = this.cdr.detectChanges;
    let detectionCount = 0;
    
    this.cdr.detectChanges = function() {
      detectionCount++;
      const start = performance.now();
      const result = originalDetectChanges.apply(this, arguments);
      const end = performance.now();
      console.log(`Change detection #${detectionCount}: ${end - start}ms`);
      return result;
    };
  }
}
```

### 3. 内存调试

```typescript
// 内存泄漏检测
export class MemoryDebugger {
  private initialMemory: number;
  
  constructor() {
    if (performance.memory) {
      this.initialMemory = performance.memory.usedJSHeapSize;
    }
  }
  
  checkMemoryUsage(): void {
    if (performance.memory) {
      const currentMemory = performance.memory.usedJSHeapSize;
      const memoryIncrease = currentMemory - this.initialMemory;
      
      console.log(`Memory usage: ${(currentMemory / 1024 / 1024).toFixed(2)} MB`);
      console.log(`Memory increase: ${(memoryIncrease / 1024 / 1024).toFixed(2)} MB`);
      
      if (memoryIncrease > 50 * 1024 * 1024) { // 50MB 增长
        console.warn('Potential memory leak detected!');
      }
    }
  }
  
  forceGarbageCollection(): void {
    // 强制垃圾回收（开发工具中）
    if (window.gc) {
      window.gc();
      console.log('Garbage collection forced');
    }
  }
}
```

## 浏览器兼容性问题

### 1. Polyfill 配置

```typescript
// polyfills.ts
import 'core-js/es6/symbol';
import 'core-js/es6/object';
import 'core-js/es6/function';
import 'core-js/es6/parse-int';
import 'core-js/es6/parse-float';
import 'core-js/es6/number';
import 'core-js/es6/math';
import 'core-js/es6/string';
import 'core-js/es6/date';
import 'core-js/es6/array';
import 'core-js/es6/regexp';
import 'core-js/es6/map';
import 'core-js/es6/weak-map';
import 'core-js/es6/set';

// 如果需要 IE11 支持
import 'core-js/es6/reflect';
import 'core-js/es7/reflect';
import 'zone.js/dist/zone';  // 包含 IE 需要的补丁
```

### 2. CSS 兼容性

```scss
// 使用 autoprefixer 自动添加前缀
// 在 angular.json 中配置
{
  "projects": {
    "your-app": {
      "architect": {
        "build": {
          "options": {
            "styles": [
              "src/styles.scss"
            ],
            "stylePreprocessorOptions": {
              "includePaths": [
                "src/styles"
              ]
            }
          }
        }
      }
    }
  }
}

// 使用 CSS Grid 回退
.container {
  display: flex; // 旧浏览器回退
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}
```

## 网络问题

### 1. 请求超时处理

```typescript
// HTTP 超时配置
export const timeoutInterceptor: HttpInterceptorFn = (req, next) => {
  const timeout = 30000; // 30秒
  
  return next(req).pipe(
    timeout(timeout),
    catchError(error => {
      if (error.name === 'TimeoutError') {
        inject(Alerts).error('请求超时，请稍后重试');
      }
      return throwError(() => error);
    })
  );
};
```

### 2. 网络状态监控

```typescript
// 网络状态服务
@Injectable({
  providedIn: 'root'
})
export class NetworkStatusService {
  private onlineStatus = signal(navigator.onLine);
  
  constructor() {
    window.addEventListener('online', () => {
      this.onlineStatus.set(true);
      this.handleOnline();
    });
    
    window.addEventListener('offline', () => {
      this.onlineStatus.set(false);
      this.handleOffline();
    });
  }
  
  isOnline() {
    return this.onlineStatus.asReadonly();
  }
  
  private handleOnline(): void {
    // 恢复网络操作
    this.syncOfflineData();
  }
  
  private handleOffline(): void {
    // 切换到离线模式
    this.enableOfflineMode();
  }
  
  private syncOfflineData(): void {
    // 同步离线期间的数据
  }
  
  private enableOfflineMode(): void {
    // 启用离线模式
  }
}
```

## 联系支持

如果以上解决方案无法解决您的问题，请通过以下方式获取帮助：

1. **查看完整文档**: 参考项目文档目录
2. **提交 Issue**: 在 GitHub 仓库提交问题
3. **社区支持**: 在相关技术社区寻求帮助
4. **邮件支持**: 发送邮件至技术支持团队

---

**文档版本**: 1.0.0  
**最后更新**: 2025年
