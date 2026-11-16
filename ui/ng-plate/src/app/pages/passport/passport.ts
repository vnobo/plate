import { Routes } from '@angular/router';

export const PASSPORT_ROUTES: Routes = [
  {
    path: '',
    children: [
      {
        path: 'lock',
        loadComponent: () => import('./auth-lock/auth-lock').then((m) => m.AuthLock),
        title: $localize`:@@route.passport.lock:锁屏`,
      },
      {
        path: 'login',
        loadComponent: () => import('./login/login').then((m) => m.Login),
        title: $localize`:@@route.passport.login:登录`,
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login',
      },
    ],
  },
];
