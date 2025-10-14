import { Routes } from '@angular/router';
import {
  createLazyRoute,
  createRedirectRoute,
  createWildcardRoute,
  validateRoutes,
} from './core/routing/routing-utils';
import { canActivateGuard, roleChildGuard } from './core';

/**
 * 应用路由配置
 *
 * 路由结构说明：
 * - home: 主应用页面（需要认证）
 * - passport: 认证相关页面（登录、注册等）
 * - exception: 异常页面（404、500等错误页面）
 * - 默认重定向到 passport 页面
 * - 通配符路由处理未匹配的路由
 */
export const routes: Routes = [
  // 主应用路由 - 懒加载（需要认证）
  {
    path: 'home',
    ...createLazyRoute(() => import('./pages/index').then(m => m.HOME_ROUTES), '平台管理后台'),
    title: '平台管理后台',
    canActivate: [canActivateGuard, roleChildGuard],
  },

  // 认证路由 - 懒加载（无需认证）
  {
    path: 'passport',
    ...createLazyRoute(() => import('./pages/index').then(m => m.PASSPORT_ROUTES), '登录页面'),
    title: '欢迎登陆PLATE系统综合管理平台',
  },

  // 异常页面路由 - 懒加载（无需认证）
  {
    path: 'exception',
    ...createLazyRoute(() => import('./pages/index').then(m => m.EXCEPTION_ROUTES), '异常页面'),
    data: {
      requiresAuth: false,
      title: '错误页面',
      breadcrumb: '错误',
    },
  },

  // 默认路由重定向
  createRedirectRoute('/passport'),

  // 通配符路由 - 处理未匹配的路由
  createWildcardRoute('exception/404'),
];

// 路由配置验证（开发环境）
if (typeof ngDevMode === 'undefined' || ngDevMode) {
  const validation = validateRoutes(routes);
  if (!validation.isValid) {
    console.warn('路由配置验证警告:', validation.errors);
  }
}
