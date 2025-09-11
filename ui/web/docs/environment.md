# 环境配置文档

## 概述

本文档详细描述了 Angular v20 Web 应用程序的环境配置管理，包括开发环境、测试环境、预生产环境和生产环境的配置方法。

## 环境架构

### 环境层次

```
环境架构
├── 开发环境 (Development)     # 本地开发
├── 测试环境 (Testing)         # 自动化测试
├── 预生产环境 (Staging)       # 预发布测试
└── 生产环境 (Production)      # 正式环境
```

### 环境配置对比

| 特性 | 开发环境 | 测试环境 | 预生产环境 | 生产环境 |
|------|----------|----------|------------|----------|
| **调试模式** | ✅ 开启 | ✅ 开启 | ❌ 关闭 | ❌ 关闭 |
| **Service Worker** | ❌ 禁用 | ❌ 禁用 | ✅ 启用 | ✅ 启用 |
| **API 地址** | 本地/开发 | 测试服务器 | 预生产服务器 | 生产服务器 |
| **日志级别** | debug | info | warn | error |
| **错误报告** | ❌ 禁用 | ✅ 启用 | ✅ 启用 | ✅ 启用 |
| **性能监控** | ❌ 禁用 | ✅ 启用 | ✅ 启用 | ✅ 启用 |

## 环境文件结构

### 环境文件位置

```
src/envs/
├── env.ts              # 生产环境配置
├── env.dev.ts          # 开发环境配置
├── env.test.ts         # 测试环境配置
└── env.staging.ts      # 预生产环境配置
```

### 环境配置接口

```typescript
// environment.interface.ts
export interface AppEnvironment {
  production: boolean;
  apiUrl: string;
  websocketUrl: string;
  appVersion: string;
  enableServiceWorker: boolean;
  enableAnalytics: boolean;
  enableErrorReporting: boolean;
  logLevel: 'debug' | 'info' | 'warn' | 'error';
  features: {
    darkMode: boolean;
    notifications: boolean;
    offlineMode: boolean;
    experimentalFeatures: boolean;
  };
  security: {
    enableCSRF: boolean;
    enableXSS: boolean;
    contentSecurityPolicy: string;
  };
  performance: {
    enablePrefetching: boolean;
    enableLazyLoading: boolean;
    cacheTimeout: number;
  };
}
```

## 环境配置详解

### 开发环境配置

**文件**: [`src/envs/env.dev.ts`](src/envs/env.dev.ts:1)

```typescript
import { AppEnvironment } from './environment.interface';

export const environment: AppEnvironment = {
  production: false,
  apiUrl: 'http://localhost:3000/api',
  websocketUrl: 'ws://localhost:3000',
  appVersion: '1.0.0-dev',
  enableServiceWorker: false,
  enableAnalytics: false,
  enableErrorReporting: false,
  logLevel: 'debug',
  features: {
    darkMode: true,
    notifications: true,
    offlineMode: false,
    experimentalFeatures: true
  },
  security: {
    enableCSRF: true,
    enableXSS: true,
    contentSecurityPolicy: "default-src 'self' 'unsafe-inline' 'unsafe-eval' localhost:* ws://localhost:*"
  },
  performance: {
    enablePrefetching: false,
    enableLazyLoading: false,
    cacheTimeout: 0
  }
};
```

### 生产环境配置

**文件**: [`src/envs/env.ts`](src/envs/env.ts:1)

```typescript
import { AppEnvironment } from './environment.interface';

export const environment: AppEnvironment = {
  production: true,
  apiUrl: 'https://api.yourdomain.com',
  websocketUrl: 'wss://ws.yourdomain.com',
  appVersion: process.env['npm_package_version'] || '1.0.0',
  enableServiceWorker: true,
  enableAnalytics: true,
  enableErrorReporting: true,
  logLevel: 'error',
  features: {
    darkMode: true,
    notifications: true,
    offlineMode: true,
    experimentalFeatures: false
  },
  security: {
    enableCSRF: true,
    enableXSS: true,
    contentSecurityPolicy: "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' https:"
  },
  performance: {
    enablePrefetching: true,
    enableLazyLoading: true,
    cacheTimeout: 300000 // 5分钟
  }
};
```

## Angular 环境配置

### Angular CLI 配置

**文件**: [`angular.json`](angular.json:101-107)

```json
{
  "configurations": {
    "development": {
      "optimization": false,
      "extractLicenses": false,
      "sourceMap": true,
      "fileReplacements": [
        {
          "replace": "src/envs/env.ts",
          "with": "src/envs/env.dev.ts"
        }
      ]
    },
    "staging": {
      "optimization": true,
      "outputHashing": "all",
      "sourceMap": false,
      "namedChunks": false,
      "extractLicenses": true,
      "vendorChunk": false,
      "buildOptimizer": true,
      "fileReplacements": [
        {
          "replace": "src/envs/env.ts",
          "with": "src/envs/env.staging.ts"
        }
      ]
    }
  }
}
```

### 环境服务

```typescript
// environment.service.ts
import { Injectable, inject } from '@angular/core';
import { AppEnvironment } from '../envs/environment.interface';
import { environment } from '../envs/env';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  private readonly env = environment;

  getEnvironment(): AppEnvironment {
    return this.env;
  }

  isProduction(): boolean {
    return this.env.production;
  }

  isDevelopment(): boolean {
    return !this.env.production;
  }

  getApiUrl(): string {
    return this.env.apiUrl;
  }

  getWebsocketUrl(): string {
    return this.env.websocketUrl;
  }

  getLogLevel(): string {
    return this.env.logLevel;
  }

  isFeatureEnabled(feature: keyof AppEnvironment['features']): boolean {
    return this.env.features[feature];
  }

  getSecurityConfig(): AppEnvironment['security'] {
    return this.env.security;
  }

  getPerformanceConfig(): AppEnvironment['performance'] {
    return this.env.performance;
  }
}
```

## 环境变量管理

### 系统环境变量

```bash
# .env 文件 (开发环境)
NODE_ENV=development
API_URL=http://localhost:3000/api
WEBSOCKET_URL=ws://localhost:3000
LOG_LEVEL=debug
ENABLE_ANALYTICS=false
ENABLE_ERROR_REPORTING=false

# .env.production 文件 (生产环境)
NODE_ENV=production
API_URL=https://api.yourdomain.com
WEBSOCKET_URL=wss://ws.yourdomain.com
LOG_LEVEL=error
ENABLE_ANALYTICS=true
ENABLE_ERROR_REPORTING=true
JWT_SECRET=your-jwt-secret-key
ENCRYPTION_KEY=your-encryption-key
```

### 环境变量加载

```typescript
// env-loader.ts
import { config } from 'dotenv';

export function loadEnvironmentVariables(): void {
  const envFile = process.env.NODE_ENV ? `.env.${process.env.NODE_ENV}` : '.env';
  
  config({ path: envFile });
  
  // 验证必需的环境变量
  const requiredEnvVars = [
    'API_URL',
    'WEBSOCKET_URL',
    'JWT_SECRET',
    'ENCRYPTION_KEY'
  ];
  
  const missingVars = requiredEnvVars.filter(varName => !process.env[varName]);
  
  if (missingVars.length > 0) {
    throw new Error(`Missing required environment variables: ${missingVars.join(', ')}`);
  }
}
```

## 动态环境配置

### 运行时配置

```typescript
// runtime-config.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RuntimeConfigService {
  private config: RuntimeConfig | null = null;

  constructor(private http: HttpClient) {}

  loadConfig(): Observable<RuntimeConfig> {
    if (this.config) {
      return of(this.config);
    }

    return this.http.get<RuntimeConfig>('/assets/config.json').pipe(
      tap(config => {
        this.config = config;
      }),
      catchError(error => {
        console.error('Failed to load runtime config:', error);
        return of(this.getDefaultConfig());
      })
    );
  }

  getConfig(): RuntimeConfig {
    return this.config || this.getDefaultConfig();
  }

  private getDefaultConfig(): RuntimeConfig {
    return {
      apiUrl: 'https://api.yourdomain.com',
      websocketUrl: 'wss://ws.yourdomain.com',
      features: {
        enableExperimentalFeatures: false,
        maxUploadSize: 10485760, // 10MB
        sessionTimeout: 1800000 // 30分钟
      }
    };
  }
}

interface RuntimeConfig {
  apiUrl: string;
  websocketUrl: string;
  features: {
    enableExperimentalFeatures: boolean;
    maxUploadSize: number;
    sessionTimeout: number;
  };
}
```

### 应用初始化

```typescript
// app-initializer.ts
import { RuntimeConfigService } from './services/runtime-config.service';

export function initializeApp(runtimeConfigService: RuntimeConfigService): () => Promise<void> {
  return () => runtimeConfigService.loadConfig().toPromise();
}

// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [RuntimeConfigService],
      multi: true
    }
  ]
};
```

## 环境特定功能

### 开发环境特性

```typescript
// development-tools.service.ts
import { Injectable } from '@angular/core';
import { EnvironmentService } from './environment.service';

@Injectable({
  providedIn: 'root'
})
export class DevelopmentToolsService {
  constructor(private envService: EnvironmentService) {
    if (this.envService.isDevelopment()) {
      this.enableDevelopmentTools();
    }
  }

  private enableDevelopmentTools(): void {
    // 启用 Redux DevTools
    if (typeof window !== 'undefined' && (window as any).__REDUX_DEVTOOLS_EXTENSION__) {
      // 配置 Redux DevTools
    }

    // 启用 Angular DevTools
    if (typeof window !== 'undefined' && (window as any).__ngDevMode) {
      console.log('Angular DevTools enabled');
    }

    // 添加调试工具
    this.addDebugTools();
  }

  private addDebugTools(): void {
    if (typeof window !== 'undefined') {
      (window as any).debug = {
        environment: () => this.envService.getEnvironment(),
        clearStorage: () => localStorage.clear(),
        showRoutes: () => console.log('Available routes:', this.getRoutes())
      };
    }
  }

  private getRoutes(): string[] {
    // 返回应用路由列表
    return [];
  }
}
```

### 生产环境优化

```typescript
// production-optimizer.service.ts
import { Injectable } from '@angular/core';
import { EnvironmentService } from './environment.service';

@Injectable({
  providedIn: 'root'
})
export class ProductionOptimizerService {
  constructor(private envService: EnvironmentService) {
    if (this.envService.isProduction()) {
      this.enableProductionOptimizations();
    }
  }

  private enableProductionOptimizations(): void {
    // 禁用控制台日志
    this.disableConsoleLogs();
    
    // 启用性能监控
    this.enablePerformanceMonitoring();
    
    // 启用错误报告
    this.enableErrorReporting();
  }

  private disableConsoleLogs(): void {
    if (typeof console !== 'undefined') {
      console.log = () => {};
      console.debug = () => {};
      console.info = () => {};
    }
  }

  private enablePerformanceMonitoring(): void {
    // 配置性能监控
    if (typeof window !== 'undefined') {
      // 监听性能指标
      window.addEventListener('load', () => {
        const perfData = performance.getEntriesByType('navigation')[0];
        console.log('Page load time:', perfData.loadEventEnd - perfData.loadEventStart);
      });
    }
  }

  private enableErrorReporting(): void {
    // 配置错误报告服务
    if (typeof window !== 'undefined') {
      window.addEventListener('error', (event) => {
        // 发送错误到错误报告服务
        console.error('Global error:', event.error);
      });
      
      window.addEventListener('unhandledrejection', (event) => {
        // 处理未处理的 Promise 拒绝
        console.error('Unhandled promise rejection:', event.reason);
      });
    }
  }
}
```

## 环境切换

### 环境切换服务

```typescript
// environment-switcher.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentSwitcherService {
  private currentEnv = new BehaviorSubject<string>('development');
  currentEnv$ = this.currentEnv.asObservable();

  switchEnvironment(env: string): void {
    if (this.isValidEnvironment(env)) {
      this.currentEnv.next(env);
      this.reloadApplication();
    } else {
      console.error(`Invalid environment: ${env}`);
    }
  }

  private isValidEnvironment(env: string): boolean {
    const validEnvironments = ['development', 'testing', 'staging', 'production'];
    return validEnvironments.includes(env);
  }

  private reloadApplication(): void {
    // 重新加载应用以应用新的环境配置
    if (typeof window !== 'undefined') {
      window.location.reload();
    }
  }

  getCurrentEnvironment(): string {
    return this.currentEnv.value;
  }
}
```

### 环境切换组件

```typescript
// environment-switcher.component.ts
import { Component, inject } from '@angular/core';
import { EnvironmentSwitcherService } from '../services/environment-switcher.service';
import { EnvironmentService } from '../services/environment.service';

@Component({
  selector: 'app-environment-switcher',
  template: `
    <div class="environment-switcher" *ngIf="showSwitcher()">
      <select [(ngModel)]="selectedEnv" (change)="onEnvironmentChange()">
        <option value="development">Development</option>
        <option value="testing">Testing</option>
        <option value="staging">Staging</option>
        <option value="production">Production</option>
      </select>
      <span class="current-env">Current: {{ currentEnv }}</span>
    </div>
  `,
  styles: [`
    .environment-switcher {
      position: fixed;
      top: 10px;
      right: 10px;
      z-index: 9999;
      background: rgba(0, 0, 0, 0.8);
      color: white;
      padding: 10px;
      border-radius: 5px;
    }
  `]
})
export class EnvironmentSwitcherComponent {
  private envSwitcher = inject(EnvironmentSwitcherService);
  private envService = inject(EnvironmentService);
  
  selectedEnv = this.envSwitcher.getCurrentEnvironment();
  currentEnv = this.envSwitcher.getCurrentEnvironment();

  showSwitcher(): boolean {
    return this.envService.isDevelopment() || this.envService.isTesting();
  }

  onEnvironmentChange(): void {
    this.envSwitcher.switchEnvironment(this.selectedEnv);
  }
}
```

## 环境验证

### 环境验证服务

```typescript
// environment-validator.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { EnvironmentService } from './environment.service';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentValidatorService {
  constructor(
    private http: HttpClient,
    private envService: EnvironmentService
  ) {}

  validateEnvironment(): Observable<boolean> {
    const checks = [
      this.validateApiConnection(),
      this.validateWebSocketConnection(),
      this.validateSecurityConfig()
    ];

    return new Observable(observer => {
      Promise.all(checks).then(results => {
        const allValid = results.every(result => result);
        observer.next(allValid);
        observer.complete();
      });
    });
  }

  private async validateApiConnection(): Promise<boolean> {
    try {
      const response = await this.http.get(`${this.envService.getApiUrl()}/health`).toPromise();
      return !!response;
    } catch (error) {
      console.error('API connection validation failed:', error);
      return false;
    }
  }

  private async validateWebSocketConnection(): Promise<boolean> {
    return new Promise(resolve => {
      const wsUrl = this.envService.getWebsocketUrl();
      const ws = new WebSocket(wsUrl);
      
      ws.onopen = () => {
        ws.close();
        resolve(true);
      };
      
      ws.onerror = () => {
        resolve(false);
      };
      
      // 5秒超时
      setTimeout(() => {
        ws.close();
        resolve(false);
      }, 5000);
    });
  }

  private async validateSecurityConfig(): Promise<boolean> {
    const securityConfig = this.envService.getSecurityConfig();
    
    // 验证 CSP 配置
    if (this.envService.isProduction() && !securityConfig.contentSecurityPolicy) {
      console.error('Content Security Policy not configured for production');
      return false;
    }
    
    return true;
  }
}
```

## 环境配置最佳实践

### 1. 敏感信息管理

```typescript
// 使用环境变量存储敏感信息
export const environment = {
  production: true,
  apiUrl: process.env['API_URL'] || 'https://api.yourdomain.com',
  jwtSecret: process.env['JWT_SECRET'], // 从环境变量读取
  encryptionKey: process.env['ENCRYPTION_KEY'], // 从环境变量读取
  // 不要在代码中硬编码敏感信息
};
```

### 2. 配置验证

```typescript
// 配置验证函数
function validateEnvironmentConfig(config: AppEnvironment): void {
  const requiredFields = ['apiUrl', 'websocketUrl', 'logLevel'];
  
  for (const field of requiredFields) {
    if (!config[field as keyof AppEnvironment]) {
      throw new Error(`Missing required environment configuration: ${field}`);
    }
  }
  
  // 验证 URL 格式
  try {
    new URL(config.apiUrl);
    new URL(config.websocketUrl);
  } catch (error) {
    throw new Error('Invalid URL format in environment configuration');
  }
}
```

### 3. 环境文档化

```typescript
// 为每个环境配置添加详细注释
export const environment: AppEnvironment = {
  /**
   * 生产环境标志
   * 控制是否启用生产模式优化
   */
  production: true,
  
  /**
   * API 基础地址
   * 用于所有 HTTP 请求
   * 示例: https://api.yourdomain.com
   */
  apiUrl: 'https://api.yourdomain.com',
  
  /**
   * WebSocket 连接地址
   * 用于实时通信
   * 示例: wss://ws.yourdomain.com
   */
  websocketUrl: 'wss://ws.yourdomain.com'
};
```

---

**文档版本**: 1.0.0  
**最后更新**: 2025年
