import { Routes } from '@angular/router';
import { BaseLayout } from '@app/layout';
import { Tenants } from './tenant/tenant';
import { Role } from './role/role';

export const PLATFORM_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    title: '系统管理',
    children: [
      {
        path: 'tenant',
        component: Tenants,
        title: '租户管理',
      },
      {
        path: 'role',
        component: Role,
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
