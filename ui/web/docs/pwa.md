# Progressive Web App (PWA) 文档

## 概述

Angular v20 Web 应用程序集成了完整的 Progressive Web App 功能，提供类似原生应用的用户体验，包括离线支持、推送通知和应用安装等功能。

## PWA 架构

### 核心组件

1. **Service Worker** - 后台脚本，处理离线缓存和网络请求
2. **Web App Manifest** - 应用元数据，定义应用外观和行为
3. **App Shell** - 应用外壳架构，提供快速加载
4. **缓存策略** - 智能缓存管理，优化资源加载

### 文件结构

```
src/
├── public/
│   ├── manifest.webmanifest    # Web 应用清单
│   ├── icons/                  # 应用图标
│   └── ngsw-config.json       # Service Worker 配置
├── app/
│   └── app.config.ts          # PWA 配置
└── ngsw-config.json           # Service Worker 配置文件
```

## Service Worker 配置

### 配置文件

**文件**: [`ngsw-config.json`](ngsw-config.json:1)

```json
{
  "$schema": "./node_modules/@angular/service-worker/config/schema.json",
  "index": "/index.html",
  "assetGroups": [
    {
      "name": "app",
      "installMode": "prefetch",
      "resources": {
        "files": [
          "/favicon.ico",
          "/index.csr.html",
          "/index.html",
          "/manifest.webmanifest",
          "/*.css",
          "/*.js"
        ]
      }
    },
    {
      "name": "assets",
      "installMode": "lazy",
      "updateMode": "prefetch",
      "resources": {
        "files": [
          "/**/*.(svg|cur|jpg|jpeg|png|apng|webp|avif|gif|otf|ttf|woff|woff2)"
        ]
      }
    }
  ]
}
```

### 配置说明

#### Asset Groups

1. **App Group** - 核心应用资源
   - `installMode: "prefetch"` - 安装时预加载所有资源
   - 包含 HTML、CSS、JS 等核心文件
   - 确保应用可以离线运行

2. **Assets Group** - 静态资源
   - `installMode: "lazy"` - 按需加载资源
   - `updateMode: "prefetch"` - 更新时预加载
   - 包含图片、字体等媒体文件

#### 缓存策略

| 策略 | 描述 | 适用场景 |
|------|------|----------|
| prefetch | 预加载所有资源 | 核心应用文件 |
| lazy | 按需加载资源 | 静态资源文件 |
| fresh | 始终获取最新 | 实时数据 |
| performance | 优先使用缓存 | 静态内容 |

## Web App Manifest

### 配置文件

**文件**: [`src/public/manifest.webmanifest`](src/public/manifest.webmanifest:1)

```json
{
  "name": "Angular v20 Web Application",
  "short_name": "Angular Web",
  "description": "基于 Angular v20 的现代化 Web 应用程序",
  "theme_color": "#1976d2",
  "background_color": "#fafafa",
  "display": "standalone",
  "scope": "/",
  "start_url": "/",
  "icons": [
    {
      "src": "assets/icons/icon-72x72.png",
      "sizes": "72x72",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-96x96.png",
      "sizes": "96x96",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-128x128.png",
      "sizes": "128x128",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-144x144.png",
      "sizes": "144x144",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-152x152.png",
      "sizes": "152x152",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-384x384.png",
      "sizes": "384x384",
      "type": "image/png",
      "purpose": "maskable any"
    },
    {
      "src": "assets/icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "maskable any"
    }
  ],
  "categories": ["business", "productivity"],
  "lang": "zh-CN",
  "dir": "ltr",
  "orientation": "portrait-primary",
  "prefer_related_applications": false
}
```

### 配置属性说明

| 属性 | 类型 | 描述 |
|------|------|------|
| name | string | 应用完整名称 |
| short_name | string | 应用短名称 |
| description | string | 应用描述 |
| theme_color | string | 主题颜色 |
| background_color | string | 背景颜色 |
| display | string | 显示模式 |
| scope | string | 应用范围 |
| start_url | string | 启动 URL |
| icons | array | 应用图标数组 |
| categories | array | 应用分类 |
| lang | string | 应用语言 |

### 显示模式

| 模式 | 描述 | 特性 |
|------|------|------|
| fullscreen | 全屏模式 | 隐藏所有浏览器 UI |
| standalone | 独立模式 | 类似原生应用，有状态栏 |
| minimal-ui | 最小 UI | 基本的浏览器控件 |
| browser | 浏览器模式 | 普通浏览器窗口 |

## Angular PWA 配置

### 应用配置

**文件**: [`src/app/app.config.ts`](src/app/app.config.ts:1)

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    // ... 其他配置
    provideServiceWorker('ngsw-worker.js', {
      enabled: !isDevMode(),                    // 生产环境启用
      registrationStrategy: 'registerWhenStable:30000' // 30秒后注册
    })
  ]
};
```

### Service Worker 注册策略

1. **registerWhenStable** - 应用稳定时注册
2. **registerImmediately** - 立即注册
3. **registerWithDelay** - 延迟注册

## PWA 功能实现

### 离线支持

#### 缓存策略实现

```typescript
// 网络优先策略
export const networkFirstStrategy = {
  cacheName: 'api-cache',
  plugins: [
    {
      cacheWillUpdate: async ({ response }) => {
        if (response && response.ok) {
          return response;
        }
        return null;
      }
    }
  ]
};

// 缓存优先策略
export const cacheFirstStrategy = {
  cacheName: 'static-cache',
  plugins: [
    {
      cacheKeyWillBeUsed: async ({ request }) => {
        return request.url;
      }
    }
  ]
};
```

#### 离线页面处理

```typescript
// 离线检测服务
@Injectable({
  providedIn: 'root'
})
export class OfflineService {
  private isOnline = signal(true);
  
  constructor() {
    window.addEventListener('online', () => this.updateOnlineStatus());
    window.addEventListener('offline', () => this.updateOnlineStatus());
  }
  
  private updateOnlineStatus() {
    this.isOnline.set(navigator.onLine);
  }
  
  getOnlineStatus() {
    return this.isOnline.asReadonly();
  }
}
```

### 应用安装

#### 安装提示组件

```typescript
@Component({
  selector: 'app-install-prompt',
  template: `
    @if (showInstallPrompt()) {
      <div class="install-banner">
        <div class="install-content">
          <span>安装应用到主屏幕以获得更好体验</span>
          <button (click)="installApp()">安装</button>
          <button (click)="dismissPrompt()">稍后</button>
        </div>
      </div>
    }
  `
})
export class InstallPromptComponent {
  private deferredPrompt: any;
  showInstallPrompt = signal(false);
  
  constructor() {
    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      this.deferredPrompt = e;
      this.showInstallPrompt.set(true);
    });
  }
  
  async installApp() {
    if (this.deferredPrompt) {
      this.deferredPrompt.prompt();
      const { outcome } = await this.deferredPrompt.userChoice;
      
      if (outcome === 'accepted') {
        console.log('用户接受了安装提示');
      }
      
      this.deferredPrompt = null;
      this.showInstallPrompt.set(false);
    }
  }
  
  dismissPrompt() {
    this.showInstallPrompt.set(false);
  }
}
```

### 推送通知

#### 通知服务

```typescript
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private swRegistration: ServiceWorkerRegistration | null = null;
  
  async initialize() {
    if ('serviceWorker' in navigator && 'PushManager' in window) {
      this.swRegistration = await navigator.serviceWorker.ready;
    }
  }
  
  async requestPermission(): Promise<NotificationPermission> {
    return await Notification.requestPermission();
  }
  
  async subscribeToNotifications(): Promise<PushSubscription | null> {
    if (!this.swRegistration) return null;
    
    try {
      const subscription = await this.swRegistration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: this.urlBase64ToUint8Array(environment.vapidPublicKey)
      });
      
      return subscription;
    } catch (error) {
      console.error('订阅推送通知失败:', error);
      return null;
    }
  }
  
  async showNotification(title: string, options?: NotificationOptions) {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.ready.then(registration => {
        registration.showNotification(title, {
          body: options?.body || '',
          icon: '/assets/icons/icon-192x192.png',
          badge: '/assets/icons/icon-72x72.png',
          vibrate: [200, 100, 200],
          ...options
        });
      });
    }
  }
  
  private urlBase64ToUint8Array(base64String: string): Uint8Array {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding)
      .replace(/\-/g, '+')
      .replace(/_/g, '/');
    
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    
    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
  }
}
```

## 缓存管理

### 缓存服务

```typescript
@Injectable({
  providedIn: 'root'
})
export class CacheService {
  private cache = new Map<string, CacheEntry>();
  
  async cacheRequest(request: Request, response: Response): Promise<void> {
    const cacheKey = this.generateCacheKey(request);
    const cacheEntry: CacheEntry = {
      response: response.clone(),
      timestamp: Date.now(),
      expires: Date.now() + this.getCacheTimeout(request)
    };
    
    this.cache.set(cacheKey, cacheEntry);
  }
  
  async getCachedResponse(request: Request): Promise<Response | null> {
    const cacheKey = this.generateCacheKey(request);
    const entry = this.cache.get(cacheKey);
    
    if (!entry) return null;
    
    if (Date.now() > entry.expires) {
      this.cache.delete(cacheKey);
      return null;
    }
    
    return entry.response.clone();
  }
  
  private generateCacheKey(request: Request): string {
    return `${request.method}:${request.url}`;
  }
  
  private getCacheTimeout(request: Request): number {
    const url = new URL(request.url);
    
    // API 请求缓存 5 分钟
    if (url.pathname.startsWith('/api/')) {
      return 5 * 60 * 1000;
    }
    
    // 静态资源缓存 1 小时
    if (url.pathname.match(/\.(css|js|png|jpg|jpeg|gif|svg)$/)) {
      return 60 * 60 * 1000;
    }
    
    // 默认缓存 1 分钟
    return 60 * 1000;
  }
}

interface CacheEntry {
  response: Response;
  timestamp: number;
  expires: number;
}
```

## 更新管理

### 更新检测服务

```typescript
@Injectable({
  providedIn: 'root'
})
export class UpdateService {
  private updateAvailable = signal(false);
  
  constructor() {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.ready.then(registration => {
        // 监听更新
        registration.addEventListener('updatefound', () => {
          const newWorker = registration.installing;
          
          newWorker?.addEventListener('statechange', () => {
            if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
              // 新版本可用
              this.updateAvailable.set(true);
            }
          });
        });
      });
    }
  }
  
  async checkForUpdates(): Promise<boolean> {
    if ('serviceWorker' in navigator) {
      const registration = await navigator.serviceWorker.ready;
      try {
        await registration.update();
        return this.updateAvailable();
      } catch (error) {
        console.error('检查更新失败:', error);
        return false;
      }
    }
    return false;
  }
  
  async activateUpdate(): Promise<void> {
    if ('serviceWorker' in navigator) {
      const registration = await navigator.serviceWorker.ready;
      
      if (registration.waiting) {
        // 跳过等待，立即激活新版本
        registration.waiting.postMessage({ type: 'SKIP_WAITING' });
        
        // 刷新页面
        window.location.reload();
      }
    }
  }
  
  getUpdateAvailable() {
    return this.updateAvailable.asReadonly();
  }
}
```

### 更新提示组件

```typescript
@Component({
  selector: 'app-update-prompt',
  template: `
    @if (updateAvailable()) {
      <div class="update-banner">
        <div class="update-content">
          <span>新版本可用，是否立即更新？</span>
          <button (click)="updateNow()">立即更新</button>
          <button (click)="dismissUpdate()">稍后</button>
        </div>
      </div>
    }
  `
})
export class UpdatePromptComponent {
  private readonly updateService = inject(UpdateService);
  updateAvailable = this.updateService.getUpdateAvailable();
  
  updateNow() {
    this.updateService.activateUpdate();
  }
  
  dismissUpdate() {
    // 可以设置延迟再次提醒
    setTimeout(() => {
      this.updateService.checkForUpdates();
    }, 60 * 60 * 1000); // 1小时后再次检查
  }
}
```

## 性能监控

### 性能指标收集

```typescript
@Injectable({
  providedIn: 'root'
})
export class PWAPerformanceService {
  async collectPerformanceMetrics(): Promise<PerformanceMetrics> {
    const metrics: PerformanceMetrics = {
      pageLoadTime: 0,
      firstPaint: 0,
      firstContentfulPaint: 0,
      serviceWorkerLoadTime: 0,
      cacheHitRate: 0
    };
    
    // 收集页面加载时间
    if (performance.timing) {
      metrics.pageLoadTime = performance.timing.loadEventEnd - performance.timing.navigationStart;
    }
    
    // 收集 Paint 指标
    const paintEntries = performance.getEntriesByType('paint');
    paintEntries.forEach(entry => {
      if (entry.name === 'first-paint') {
        metrics.firstPaint = entry.startTime;
      } else if (entry.name === 'first-contentful-paint') {
        metrics.firstContentfulPaint = entry.startTime;
      }
    });
    
    // 收集 Service Worker 加载时间
    if ('serviceWorker' in navigator) {
      const swEntries = performance.getEntriesByType('resource')
        .filter(entry => entry.name.includes('ngsw-worker.js'));
      
      if (swEntries.length > 0) {
        metrics.serviceWorkerLoadTime = swEntries[0].duration;
      }
    }
    
    return metrics;
  }
  
  async calculateCacheHitRate(): Promise<number> {
    // 计算缓存命中率
    const totalRequests = await this.getTotalRequests();
    const cachedRequests = await this.getCachedRequests();
    
    return totalRequests > 0 ? (cachedRequests / totalRequests) * 100 : 0;
  }
  
  private async getTotalRequests(): Promise<number> {
    // 实现请求统计逻辑
    return 100; // 示例值
  }
  
  private async getCachedRequests(): Promise<number> {
    // 实现缓存命中统计逻辑
    return 75; // 示例值
  }
}

interface PerformanceMetrics {
  pageLoadTime: number;
  firstPaint: number;
  firstContentfulPaint: number;
  serviceWorkerLoadTime: number;
  cacheHitRate: number;
}
```

## 调试和测试

### Service Worker 调试

```typescript
// 调试工具
export class SWDebugService {
  async getServiceWorkerStatus(): Promise<string> {
    if ('serviceWorker' in navigator) {
      const registration = await navigator.serviceWorker.ready;
      
      if (registration.active) {
        return `Service Worker 已激活: ${registration.active.state}`;
      } else if (registration.installing) {
        return 'Service Worker 安装中...';
      } else if (registration.waiting) {
        return 'Service Worker 等待激活...';
      }
    }
    
    return 'Service Worker 不支持或未注册';
  }
  
  async clearAllCaches(): Promise<void> {
    if ('caches' in window) {
      const cacheNames = await caches.keys();
      await Promise.all(cacheNames.map(name => caches.delete(name)));
      console.log('所有缓存已清除');
    }
  }
  
  async unregisterServiceWorker(): Promise<void> {
    if ('serviceWorker' in navigator) {
      const registration = await navigator.serviceWorker.ready;
      await registration.unregister();
      console.log('Service Worker 已注销');
    }
  }
}
```

### PWA 测试清单

- [ ] 应用可以离线访问
- [ ] 离线时显示适当的错误页面
- [ ] 网络恢复后自动同步数据
- [ ] 应用安装提示正常工作
- [ ] 推送通知功能正常
- [ ] 缓存更新机制正常
- [ ] 性能指标符合预期
- [ ] 跨浏览器兼容性良好

## 最佳实践

### 1. 缓存策略选择

```typescript
// 静态资源 - 缓存优先
const staticCacheStrategy = {
  cacheName: 'static-resources',
  strategy: 'cache-first',
  maxAge: 30 * 24 * 60 * 60 * 1000 // 30天
};

// API 请求 - 网络优先
const apiCacheStrategy = {
  cacheName: 'api-responses',
  strategy: 'network-first',
  maxAge: 5 * 60 * 1000 // 5分钟
};

// 图片资源 - 缓存优先，带网络回退
const imageCacheStrategy = {
  cacheName: 'images',
  strategy: 'cache-first',
  maxAge: 7 * 24 * 60 * 60 * 1000, // 7天
  fallback: '/assets/images/offline-placeholder.png'
};
```

### 2. 用户体验优化

```typescript
// 渐进式增强
if ('serviceWorker' in navigator) {
  // 注册 Service Worker
  navigator.serviceWorker.register('/ngsw-worker.js');
  
  // 添加离线事件监听
  window.addEventListener('offline', () => {
    this.toasts.warning('您已进入离线模式', '网络连接');
  });
  
  window.addEventListener('online', () => {
    this.toasts.success('网络连接已恢复', '网络连接');
  });
}
```

### 3. 性能优化

```typescript
// 预加载关键资源
const criticalResources = [
  '/',
  '/styles.css',
  '/main.js',
  '/assets/icons/icon-192x192.png'
];

// 在 Service Worker 安装时预缓存
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open('critical-cache').then(cache => {
      return cache.addAll(criticalResources);
    })
  );
});
```

---

**文档版本**: 1.0.0  
**最后更新**: 2025年
