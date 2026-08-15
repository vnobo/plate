import { Routes } from '@angular/router';
import { Welcome } from './welcome/welcome';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
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
