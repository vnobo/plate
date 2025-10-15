import { Routes } from '@angular/router';
import { Users } from './users/users';

export const WELCOME_ROUTES: Routes = [
  {
    path: '',
    data: { role: 'ROLE_SYSTEM_ADMINISTRATORS' },
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
