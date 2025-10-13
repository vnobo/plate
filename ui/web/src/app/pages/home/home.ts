import { Routes } from '@angular/router';
import { BaseLayout } from '@app/layout';
import { Users } from './users/users';
import { authGuard } from '@app/core';

export const HOME_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'users',
        component: Users,
        canActivate: [authGuard],
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'users',
      },
    ],
  },
];
