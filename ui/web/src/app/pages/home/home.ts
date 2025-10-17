import { Routes } from '@angular/router';
import { BaseLayout } from '@app/layout';
import { Users } from './users/users';
import { roleChildGuard } from '@app/core';

export const HOME_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    data: { role: 'ROLE_SYSTEM_ADMINISTRATORS' },
    canActivate: [roleChildGuard],
    children: [
      {
        path: 'users',
        component: Users,
        title: '用户管理',
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'users',
      },
    ],
  },
];
