# 项目架构文档

## 概述

本文档详细描述了 Angular v20 Web 应用程序的架构设计，包括技术选型、模块结构、数据流和最佳实践。

## 技术架构

### 前端架构

```
┌─────────────────────────────────────────────────────────────┐
│                     表示层 (Presentation Layer)               │
├─────────────────────────────────────────────────────────────┤
│  Components  │  Directives  │  Pipes  │  Templates        │
├─────────────────────────────────────────────────────────────┤
│                     业务逻辑层 (Business Logic Layer)         │
├─────────────────────────────────────────────────────────────┤
│  Services  │  Guards  │  Interceptors  │  Resolvers       │
├─────────────────────────────────────────────────────────────┤
│                     数据访问层 (Data Access Layer)            │
├─────────────────────────────────────────────────────────────┤
│  HTTP Client  │  Storage  │  Caching  │  State Management │
└─────────────────────────────────────────────────────────────┘
```

### 核心特性

- **Zoneless Change Detection** - 无区域变更检测机制
- **Standalone Components** - 独立组件架构
- **Signals** - Angular 信号系统用于状态管理
- **Server-Side Rendering** - 服务端渲染支持
- **Progressive Web App** - PWA 功能支持

## 模块架构

### 核心模块 (`src/app/core/`)

核心模块包含应用程序的基础服务和配置：

#### 网络层 (`net/`)

- **HTTP 拦截器** ([`http.Interceptor.ts`](src/app/core/net/http.Interceptor.ts:1))
  - 请求/响应拦截
  - 错误处理
  - Token 注入
  - 进度条控制

#### 服务层 (`services/`)

- **TokenService** ([`token.service.ts`](src/app/core/services/token.service.ts:1))
  - JWT Token 管理
  - 认证状态维护
  - Token 验证和刷新

- **PageTitleStrategy** ([`page-title-strategy.ts`](src/app/core/services/page-title-strategy.ts:1))
  - 动态页面标题管理
  - 路由标题策略

- **ProgressBar** ([`progress-bar.ts`](src/app/core/services/progress-bar.ts:1))
  - 全局进度条控制
  - HTTP 请求进度显示

- **ThemeService** ([`theme.service.ts`](src/app/core/services/theme.service.ts:1))
  - 主题切换管理
  - 主题配置维护

#### 存储层 (`storage/`)

- **BrowserStorage** ([`browser-storage.ts`](src/app/core/storage/browser-storage.ts:1))
  - 浏览器存储抽象
  - localStorage/sessionStorage 封装

- **SessionStorageService** ([`session-storage.ts`](src/app/core/storage/session-storage.ts:1))
  - 会话存储服务
  - 类型安全的存储操作

### 页面模块 (`src/app/pages/`)

#### 认证模块 (`passport/`)

- **登录页面** ([`login/`](src/app/pages/passport/login/))
  - 用户认证表单
  - 登录状态管理
  - 表单验证

#### 主页模块 (`home/`)

- **用户管理** ([`users/`](src/app/pages/home/users/))
  - 用户列表展示
  - 用户表单编辑
  - 用户类型定义

#### 仪表板模块 (`dashboard/`)

- **欢迎页面** ([`welcome/`](src/app/pages/dashboard/welcome/))
  - 仪表板主页
  - 系统概览

#### 错误处理模块 (`error/`)

- **404 页面** ([`404.ts`](src/app/pages/error/404.ts:1))
- **500 页面** ([`500.ts`](src/app/pages/error/500.ts:1))
- **512 页面** ([`512.ts`](src/app/pages/error/512.ts:1))

### 插件系统 (`src/app/plugins/`)

#### 警告插件 (`alerts/`)

- **Alerts 组件** ([`alerts.ts`](src/app/plugins/alerts/alerts.ts:1))
  - 警告消息显示
  - 多种警告类型
  - 可配置选项

#### 模态框插件 (`modals/`)

- **Modals 组件** ([`modals.ts`](src/app/plugins/modals/modals.ts:1))
  - 模态框管理
  - 动态内容加载
  - 回调处理

#### 消息提示插件 (`toasts/`)

- **Toasts 组件** ([`toasts.ts`](src/app/plugins/toasts/toasts.ts:1))
  - 消息提示显示
  - 自动消失功能
  - 多种消息类型

## 路由架构

### 路由配置 ([`app.routes.ts`](src/app/app.routes.ts:1))

```typescript
const routes: Routes = [
  { path: 'home', loadChildren: () => import('./pages/index').then(m => m.HOME_ROUTES) },
  { path: 'passport', loadChildren: () => import('./pages/index').then(m => m.PASSPORT_ROUTES) },
  { path: 'exception', loadChildren: () => import('./pages/index').then(m => m.EXCEPTION_ROUTES) },
  { path: '', pathMatch: 'full', redirectTo: '/passport' },
  { path: '**', redirectTo: 'exception/404' },
];
```

### 路由守卫 ([`pages.guard.ts`](src/app/core/pages.guard.ts:1))

- **认证守卫** - 保护需要认证的页面
- **权限守卫** - 基于角色的访问控制
- **数据预加载** - 路由数据预加载

## 状态管理

### 信号系统 (Signals)

使用 Angular Signals 进行状态管理：

```typescript
// 示例：进度条状态管理
progress = signal(false);

// 更新状态
this.progress.set(true);

// 订阅状态变化
effect(() => {
  console.log('Progress:', this.progress());
});
```

### 服务状态

通过服务维护应用状态：

- **全局状态** - 应用级状态管理
- **组件状态** - 组件内部状态
- **路由状态** - 路由相关状态

## 数据流

### HTTP 数据流

```
Component -> Service -> HTTP Interceptor -> Backend API
    ↑                                        ↓
    └─────────── Response Data ─────────────┘
```

### 组件通信

- **输入输出** - @Input/@Output 装饰器
- **服务共享** - 共享服务实例
- **信号通信** - Signals 状态共享

## 性能优化

### 变更检测优化

- **Zoneless 模式** - 禁用 Zone.js
- **信号系统** - 精确的变更检测
- **OnPush 策略** - 组件级优化

### 代码分割

- **懒加载** - 路由级代码分割
- **动态导入** - 运行时加载
- **预加载策略** - 智能预加载

### 缓存策略

- **HTTP 缓存** - 响应缓存
- **Service Worker** - 离线缓存
- **浏览器缓存** - 静态资源缓存

## 安全架构

### 认证安全

- **JWT Token** - 无状态认证
- **Token 刷新** - 自动刷新机制
- **路由保护** - 认证路由守卫

### 数据安全

- **XSRF 防护** - 跨站请求伪造防护
- **输入验证** - 表单验证
- **数据加密** - 敏感数据加密

### 通信安全

- **HTTPS 强制** - 安全通信
- **CORS 配置** - 跨域资源共享
- **内容安全策略** - CSP 配置

## 部署架构

### 构建配置

- **生产优化** - 代码压缩和优化
- **资源哈希** - 缓存破坏
- **预算控制** - 包大小限制

### SSR 部署

- **Node.js 服务器** - Express 服务器
- **增量水合** - 渐进式水合
- **预渲染** - 静态页面生成

### PWA 部署

- **Service Worker** - 离线支持
- **Web Manifest** - 应用元数据
- **图标资源** - 多尺寸图标

## 监控和日志

### 错误监控

- **全局错误处理** - 错误边界
- **HTTP 错误** - 请求错误处理
- **客户端错误** - 运行时错误

### 性能监控

- **Core Web Vitals** - 核心性能指标
- **加载时间** - 页面加载性能
- **运行时性能** - 内存和 CPU 使用

## 最佳实践

### 代码规范

- **TypeScript 严格模式** - 类型安全
- **ESLint 配置** - 代码质量
- **Prettier 格式化** - 代码格式

### 组件设计

- **单一职责** - 组件职责分离
- **可复用性** - 高复用组件
- **可测试性** - 易于测试

### 服务设计

- **依赖注入** - IoC 容器
- **接口隔离** - 服务接口
- **错误处理** - 统一错误处理

## 扩展性

### 模块化设计

- **功能模块** - 业务功能分离
- **共享模块** - 通用组件共享
- **核心模块** - 基础服务提供

### 插件架构

- **插件系统** - 可扩展插件
- **动态加载** - 运行时插件加载
- **配置驱动** - 配置化插件

## 维护性

### 代码组织

- **目录结构** - 清晰的目录层次
- **命名规范** - 统一的命名约定
- **文档化** - 完整的代码注释

### 测试策略

- **单元测试** - 组件和服务测试
- **集成测试** - 模块集成测试
- **端到端测试** - 用户场景测试

---

**文档版本**: 1.0.0  
**最后更新**: 2025年
