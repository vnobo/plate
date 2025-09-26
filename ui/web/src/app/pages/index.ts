/**
 * 页面路由导出索引
 *
 * 集中管理所有页面路由配置的导出
 * 便于路由配置的统一管理和维护
 */

// 路由配置导出
export { PASSPORT_ROUTES } from './passport/passport';
export { EXCEPTION_ROUTES } from './error/error';
export { WELCOME_ROUTES } from './dashboard/dashboard';
export { HOME_ROUTES } from './home/home';

// 组件导出（用于路由配置）
export { UserForm } from './home/users/user-form';
