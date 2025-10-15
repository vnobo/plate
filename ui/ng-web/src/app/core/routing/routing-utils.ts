/**
 * 路由工具函数
 *
 * 提供路由相关的工具函数，包括错误处理、性能优化等
 */

import { Routes } from '@angular/router';

/**
 * 安全的懒加载路由配置
 * @param importFn 导入函数
 * @param routeName 路由名称（用于错误日志）
 * @returns 配置好的懒加载路由
 */
export function createLazyRoute(importFn: () => Promise<any>, routeName: string): any {
  return {
    loadChildren: () =>
      importFn()
        .then(module => {
          //console.debug(`✅ ${routeName}路由加载成功`);
          return module;
        })
        .catch(error => {
          //console.error(`❌ ${routeName}路由加载失败:`, error);
          return { default: [] };
        }),
  };
}

/**
 * 创建重定向路由
 * @param redirectTo 重定向路径
 * @param skipLocationChange 是否跳过位置变更
 * @returns 重定向路由配置
 */
export function createRedirectRoute(redirectTo: string, skipLocationChange: boolean = true): any {
  return {
    path: '',
    pathMatch: 'full' as const,
    redirectTo,
    data: { skipLocationChange },
  };
}

/**
 * 创建通配符路由
 * @param redirectTo 重定向路径
 * @param skipLocationChange 是否跳过位置变更
 * @returns 通配符路由配置
 */
export function createWildcardRoute(redirectTo: string, skipLocationChange: boolean = true): any {
  return {
    path: '**',
    redirectTo,
    data: { skipLocationChange },
  };
}

/**
 * 路由配置验证
 * @param routes 路由配置数组
 * @returns 验证结果
 */
export function validateRoutes(routes: Routes): { isValid: boolean; errors: string[] } {
  const errors: string[] = [];

  // 检查是否有重复路径
  const pathMap = new Map<string, number>();
  routes.forEach((route, index) => {
    if (route.path) {
      if (pathMap.has(route.path)) {
        errors.push(`重复的路由路径: ${route.path} (位置: ${index})`);
      }
      pathMap.set(route.path, index);
    }
  });

  // 检查通配符路由位置（应该放在最后）
  const wildcardIndex = routes.findIndex(route => route.path === '**');
  if (wildcardIndex !== -1 && wildcardIndex !== routes.length - 1) {
    errors.push('通配符路由 (**) 应该放在路由配置的最后');
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * 路由配置性能优化
 * 预加载策略配置
 */
export const PreloadStrategies = {
  /**
   * 立即预加载
   */
  IMMEDIATE: 'immediate',

  /**
   * 按需预加载（默认）
   */
  ON_DEMAND: 'on-demand',

  /**
   * 网络空闲时预加载
   */
  IDLE: 'idle',
} as const;

/**
 * 获取路由预加载配置
 * @param strategy 预加载策略
 * @returns 预加载配置
 */
export function getPreloadConfig(strategy: string = PreloadStrategies.ON_DEMAND): any {
  return {
    preloadingStrategy: strategy,
  };
}
