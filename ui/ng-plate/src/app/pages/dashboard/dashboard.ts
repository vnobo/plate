import { Routes } from '@angular/router';
import { canActivateGuard } from '@app/core';
import { BaseLayout } from '@app/layout';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    canActivate: [canActivateGuard],
    children: [
      {
        path: 'welcome',
        loadComponent: () => import('./welcome/welcome').then((m) => m.Welcome),
        title: 'Welcome',
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'welcome',
      },
    ],
  },
];
