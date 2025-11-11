import { Routes } from '@angular/router';
import { BaseLayout } from '@app/layout';
import { TenantComponent } from './tenant/tenant.component';

export const SYSTEM_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    title: '系统管理',
    children: [
      {
        path: 'tenant',
        component: TenantComponent,
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
