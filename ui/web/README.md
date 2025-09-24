# Angular v20 Web Application

一个基于 Angular v20 构建的现代化 Web 应用程序，采用最新的 Angular 特性和最佳实践。

## 🚀 项目概述

本项目是一个功能完整的 Angular v20 应用程序，集成了以下核心特性：

- **Angular v20** - 使用最新的 Angular 框架版本
- **Server-Side Rendering (SSR)** - 支持服务端渲染和增量水合
- **Progressive Web App (PWA)** - 离线支持和应用壳架构
- **Material Design** - Angular Material 组件库
- **Tabler UI** - 现代化的管理界面主题
- **Zoneless Change Detection** - 无区域变更检测
- **Standalone Components** - 独立组件架构
- **Signals** - Angular 信号系统
- **TypeScript** - 强类型支持

## 📁 项目结构

```text
src/
├── app/                          # 应用程序核心代码
│   ├── core/                     # 核心模块（服务、拦截器、守卫）
│   │   ├── net/                  # HTTP 网络层
│   │   ├── services/             # 核心服务
│   │   └── storage/              # 存储服务
│   ├── layout/                   # 布局组件
│   ├── pages/                    # 页面组件
│   │   ├── dashboard/            # 仪表板页面
│   │   ├── home/                 # 主页和用户管理
│   │   ├── passport/             # 认证相关页面
│   │   └── error/                # 错误页面
│   └── plugins/                  # 插件系统
│       ├── alerts/               # 警告提示
│       ├── modals/               # 模态框
│       └── toasts/               # 消息提示
├── envs/                         # 环境配置
└── public/                       # 静态资源
```

## 🛠️ 技术栈

### 核心框架

- **Angular v20.0.0** - 前端框架
- **TypeScript 5.8.2** - 编程语言
- **RxJS 7.8.0** - 响应式编程

### UI 框架

- **Angular Material v20.0.0** - Material Design 组件
- **Tabler Core** - 管理界面主题
- **Tabler Icons** - 图标库
- **Day.js** - 日期处理库

### 构建工具

- **Angular CLI v20.0.0** - 命令行工具
- **Angular Build** - 构建系统

### 开发工具

- **Karma** - 单元测试运行器
- **Jasmine** - 测试框架
- **TypeScript** - 类型检查

## 🚦 快速开始

### 环境要求

- Node.js (推荐 v18 或更高版本)
- npm 或 yarn 包管理器
- Angular CLI v20

### 安装依赖

```bash
# 克隆项目
git clone <repository-url>
cd web

# 安装依赖
npm install
```

### 开发服务器

```bash
# 启动开发服务器
ng serve

# 或使用 npm 脚本
npm start
```

应用将在 `http://localhost:4200` 启动，并支持热重载。

### 构建项目

```bash
# 生产环境构建
ng build

# 开发环境构建
ng build --configuration=development

# 监听模式构建
npm run watch
```

## 🧪 测试

### 单元测试

```bash
# 运行单元测试
ng test

# 或使用 npm 脚本
npm test
```

### 端到端测试

```bash
# 运行端到端测试
ng e2e
```

## 📱 PWA 特性

本项目配置了 Progressive Web App，包含以下特性：

- **离线支持** - 通过 Service Worker 缓存资源
- **应用壳** - 快速加载的应用外壳
- **Web Manifest** - 添加到主屏幕支持
- **推送通知** - 支持推送通知（需额外配置）

Service Worker 配置位于 [`ngsw-config.json`](ngsw-config.json:1)。

## 🔧 核心功能

### 认证系统

- 登录/登出功能
- JWT Token 管理
- 路由守卫保护

### 用户管理

- 用户列表展示
- 用户表单编辑
- 用户类型定义

### 插件系统

- **Alerts** - 警告提示组件
- **Modals** - 模态框组件
- **Toasts** - 消息提示组件

### 主题系统

- 支持多种主题切换
- Tabler 主题集成
- Material Design 主题

## 🌐 SSR 支持

项目支持服务端渲染 (SSR) 和增量水合：

```bash
# 构建 SSR 版本
ng build --configuration=production

# 运行 SSR 服务器
npm run serve:ssr:web
```

SSR 配置详情：

- 服务端入口：[`src/server.ts`](src/server.ts:1)
- 客户端水合：[`src/main.server.ts`](src/main.server.ts:1)
- 应用配置：[`src/app/app.config.server.ts`](src/app/app.config.server.ts:1)

## 🔒 安全特性

- **XSRF 保护** - 跨站请求伪造防护
- **HTTP 拦截器** - 请求/响应拦截处理
- **路由守卫** - 页面访问控制
- **内容安全策略** - CSP 配置

## 📊 性能优化

- **代码分割** - 懒加载模块
- **Tree Shaking** - 无用代码消除
- **资源压缩** - 生产环境优化
- **缓存策略** - 智能缓存配置

## 🌍 国际化

项目配置了中文本地化支持：

```typescript
// 在 app.config.ts 中配置
{ provide: LOCALE_ID, useValue: 'zh_CN' }
```

## 🔧 开发指南

### 代码生成

使用 Angular CLI 生成代码：

```bash
# 生成组件
ng generate component component-name

# 生成服务
ng generate service service-name

# 生成模块
ng generate module module-name

# 查看所有可用命令
ng generate --help
```

### 项目配置

主要配置文件：

- [`angular.json`](angular.json:1) - Angular CLI 配置
- [`package.json`](package.json:1) - 依赖管理
- [`tsconfig.json`](tsconfig.json:1) - TypeScript 配置

### 环境变量

环境配置位于 [`src/envs/`](src/envs/) 目录：

- [`env.ts`](src/envs/env.ts:1) - 生产环境
- [`env.dev.ts`](src/envs/env.dev.ts:1) - 开发环境

## 📚 详细文档

我们提供了完整的文档体系，涵盖项目的各个方面：

### 🏗️ 架构与设计

- [项目架构文档](docs/architecture.md) - 系统架构、技术栈和设计原则
- [组件文档](docs/components.md) - 所有组件的详细说明和使用指南
- [路由与导航](docs/routing.md) - 路由配置、守卫和导航策略

### 🔧 功能实现

- [服务 API 文档](docs/services.md) - 核心服务的 API 文档和使用方法
- [插件系统文档](docs/plugins.md) - 警告、消息提示和模态框插件系统
- [PWA 功能文档](docs/pwa.md) - Progressive Web App 配置和功能实现

### ⚙️ 配置与部署

- [环境配置文档](docs/environment.md) - 多环境配置管理和最佳实践
- [部署指南](docs/deployment.md) - 完整的部署流程和服务器配置
- [测试策略文档](docs/testing.md) - 单元测试、集成测试和 E2E 测试

### 🛠️ 开发与运维

- [故障排除指南](docs/troubleshooting.md) - 常见问题解决方案和调试技巧

### 📖 文档导航

- [文档首页](docs/README.md) - 完整的文档导航和快速入门指南

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 📝 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🆘 支持

如遇到问题，请：

1. 查看 [故障排除指南](docs/troubleshooting.md)
2. 在 [Issues](https://github.com/your-repo/issues) 中搜索类似问题
3. 创建新的 Issue 描述问题

## 🔗 相关链接

- [Angular 官方文档](https://angular.dev/)
- [Angular CLI 文档](https://angular.dev/tools/cli)
- [Angular Material](https://material.angular.io/)
- [Tabler 文档](https://tabler.io/docs)

---

**开发团队** | **最后更新**: 2025 年
