import { Routes } from '@angular/router';

export const PASSPORT_ROUTES: Routes = [
  {
    path: '',
    children: [
      {
        path: 'lock',
        loadComponent: () => import('./auth-lock/auth-lock').then((m) => m.AuthLock),
        title: 'Lock Screen',
      },
      {
        path: 'login',
        loadComponent: () => import('./login/login').then((m) => m.Login),
        title: 'Sign In',
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login',
      },
    ],
  },
];
