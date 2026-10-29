import { Routes } from '@angular/router';
import { BaseLayout } from '@app/layout';
import { Welcome } from './welcome/welcome';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    children: [
      {
        path: 'welcome',
        component: Welcome,
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
