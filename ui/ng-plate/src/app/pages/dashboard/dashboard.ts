import { Routes } from '@angular/router';
import { BaseLayout } from '@app/layout';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    children: [
      {
        path: 'welcome',
        loadComponent: () => import('./welcome/welcome').then((m) => m.Welcome),
        title: '欢迎',
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'welcome',
      },
    ],
  },
];
