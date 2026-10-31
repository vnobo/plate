import { Routes } from '@angular/router';
import { BaseLayout } from '@app/layout';

export const PLATFORM_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    title: '系统管理',
    children: [
      {
        path: 'users',
        loadComponent: () => import('./users/users').then((m) => m.Users),
        title: '用户管理',
      },
      {
        path: 'tenant',
        loadComponent: () => import('./tenant/tenant').then((m) => m.Tenants),
        title: '租户管理',
      },
      {
        path: 'role',
        loadComponent: () => import('./role/role').then((m) => m.Role),
        title: '角色管理',
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'tenant',
      },
    ],
  },
];
