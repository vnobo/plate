import { Routes } from '@angular/router';

export const PASSPORT_ROUTES: Routes = [
  {
    path: '',
    children: [
      {
        path: 'lock',
        loadComponent: () => import('./auth-lock/auth-lock').then((m) => m.AuthLock),
        title: '锁屏',
      },
      {
        path: 'login',
        loadComponent: () => import('./login/login').then((m) => m.Login),
        title: '登录',
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login',
      },
    ],
  },
];
