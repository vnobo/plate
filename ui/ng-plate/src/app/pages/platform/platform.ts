import { Routes } from '@angular/router';
import { canActivateGuard } from '@app/core';
import { BaseLayout } from '@app/layout';

export const PLATFORM_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    canActivate: [canActivateGuard],
    title: 'Platform Management',
    children: [
      {
        path: 'welcome',
        loadComponent: () => import('./welcome/welcome').then((m) => m.PlatformWelcome),
        title: 'Platform Welcome',
      },
      {
        path: 'users',
        loadComponent: () => import('./users/users').then((m) => m.Users),
        title: 'User Management',
      },
      {
        path: 'tenant',
        loadComponent: () => import('./tenant/tenant').then((m) => m.Tenants),
        title: 'Tenant Management',
      },
      {
        path: 'roles',
        loadComponent: () => import('./role/role').then((m) => m.Role),
        title: 'Role Management',
      },
      {
        path: 'menus',
        loadComponent: () => import('./menus/menus').then((m) => m.Menus),
        title: 'Menu Management',
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'welcome',
      },
    ],
  },
];
