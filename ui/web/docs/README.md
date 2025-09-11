# Angular v20 Web 应用文档

欢迎来到 Angular v20 Web 应用程序的完整文档库。本文档提供了项目的全面指南，从基础架构到高级特性的详细说明。

## 📚 文档导航

### 🏗️ 架构与设计

- [项目架构](architecture.md) - 系统架构、技术栈和设计原则
- [组件文档](components.md) - 所有组件的详细说明和使用指南
- [路由与导航](routing.md) - 路由配置、守卫和导航策略

### 🔧 核心功能

- [服务 API](services.md) - 核心服务的 API 文档和使用方法
- [插件系统](plugins.md) - 警告、消息提示和模态框插件
- [PWA 功能](pwa.md) - Progressive Web App 配置和功能实现

### ⚙️ 配置与部署

- [环境配置](environment.md) - 多环境配置管理和最佳实践
- [部署指南](deployment.md) - 完整的部署流程和服务器配置
- [测试策略](testing.md) - 单元测试、集成测试和 E2E 测试

### 🛠️ 开发与运维

- [故障排除](troubleshooting.md) - 常见问题解决方案和调试技巧

## 🚀 快速开始

### 环境要求

- Node.js v18+
- npm 或 yarn
- Angular CLI v20

### 安装和运行

```bash
# 克隆项目
git clone <repository-url>
cd web

# 安装依赖
npm install

# 启动开发服务器
ng serve

# 运行测试
ng test

# 构建项目
ng build --configuration=production
```

## 📖 核心特性

### 🎯 现代 Angular 特性

- **Angular v20** - 最新框架版本
- **Standalone Components** - 独立组件架构
- **Signals** - 响应式状态管理
- **Zoneless Change Detection** - 无区域变更检测

### 🎨 UI 框架

- **Angular Material** - Material Design 组件库
- **Tabler UI** - 现代化管理界面主题
- **响应式设计** - 移动端适配

### 🔒 安全特性

- **JWT 认证** - 无状态认证机制
- **路由守卫** - 页面访问控制
- **XSRF 防护** - 跨站请求伪造防护
- **内容安全策略** - CSP 配置

### 📱 PWA 功能

- **离线支持** - Service Worker 缓存
- **应用安装** - Web App Manifest
- **推送通知** - 实时消息推送
- **后台同步** - 数据同步机制

### ⚡ 性能优化

- **服务端渲染 (SSR)** - 首屏加载优化
- **代码分割** - 懒加载模块
- **Tree Shaking** - 无用代码消除
- **缓存策略** - 智能缓存管理

## 🏗️ 项目结构

```
src/
├── app/                          # 应用程序核心代码
│   ├── core/                     # 核心模块（服务、拦截器、守卫）
│   ├── layout/                   # 布局组件
│   ├── pages/                    # 页面组件
│   └── plugins/                  # 插件系统
├── envs/                         # 环境配置
├── public/                       # 静态资源
└── docs/                         # 项目文档
```

## 🔧 开发指南

### 代码规范

- 使用 TypeScript 严格模式
- 遵循 Angular 风格指南
- 使用 ESLint 和 Prettier
- 编写单元测试

### 组件开发

- 使用独立组件架构
- 实现 OnPush 变更检测
- 使用信号进行状态管理
- 编写组件文档

### 服务开发

- 使用依赖注入
- 实现错误处理
- 添加日志记录
- 编写服务测试

## 📊 性能指标

### 构建性能

- 初始包大小：< 500KB
- 构建时间：< 30秒
- 测试覆盖率：> 80%

### 运行时性能

- 首屏加载：< 2秒
- 交互时间：< 100ms
- 内存使用：< 100MB

## 🔍 调试和监控

### 开发工具

- Angular DevTools
- Redux DevTools
- Chrome Performance

### 生产监控

- 错误报告
- 性能监控
- 用户行为分析

## 🤝 贡献指南

1. Fork 项目仓库
2. 创建特性分支
3. 编写测试用例
4. 提交代码变更
5. 创建 Pull Request

## 📞 支持

### 获取帮助

- 📖 查看完整文档
- 🐛 提交 Issue
- 💬 社区讨论
- 📧 邮件支持

### 报告问题

请提供以下信息：

- 问题描述
- 重现步骤
- 环境信息
- 错误日志

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](../LICENSE) 文件了解详情。

## 🔄 更新日志

### v1.0.0 (2025-01)

- ✨ 初始版本发布
- 🎯 Angular v20 升级
- 📱 PWA 功能完整实现
- 🔒 安全特性增强
- ⚡ 性能优化

---

**文档维护团队** | **最后更新**: 2025年1月

> 💡 **提示**: 本文档会随项目更新而持续维护，建议定期查看最新版本。
