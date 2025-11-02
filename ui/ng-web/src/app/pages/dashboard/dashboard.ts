import { Routes } from '@angular/router';
import { Users } from './users/users';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
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
