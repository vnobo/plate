import { Routes } from '@angular/router';
import { BaseLayout } from '@app/layout';
import { TenantManagementComponent } from './tenant/tenant';

export const SYSTEM_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    title: '系统管理',
    children: [
      {
        path: 'tenant',
        component: TenantManagementComponent,
        title: '租户管理',
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'tenant',
      },
    ],
  },
];
