import { Routes } from '@angular/router';
import { canActivateGuard } from '@app/core';
import { BaseLayout } from '@app/layout';

export const PLATFORM_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    canActivate: [canActivateGuard],
    title: $localize`:@@route.platform:平台管理`,
    children: [
      {
        path: 'welcome',
        loadComponent: () => import('./welcome/welcome').then((m) => m.PlatformWelcome),
        title: $localize`:@@route.platform.welcome:平台概览`,
      },
      {
        path: 'users',
        loadComponent: () => import('./users/users').then((m) => m.Users),
        title: $localize`:@@route.platform.users:用户管理`,
      },
      {
        path: 'tenant',
        loadComponent: () => import('./tenant/tenant').then((m) => m.Tenants),
        title: $localize`:@@route.platform.tenant:租户管理`,
      },
      {
        path: 'roles',
        loadComponent: () => import('./role/role').then((m) => m.Role),
        title: $localize`:@@route.platform.roles:角色管理`,
      },
      {
        path: 'menus',
        loadComponent: () => import('./menus/menus').then((m) => m.Menus),
        title: $localize`:@@route.platform.menus:菜单管理`,
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'welcome',
      },
    ],
  },
];
