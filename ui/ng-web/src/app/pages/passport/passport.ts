import { Routes } from '@angular/router';
import { Login } from './login/login';
import { AuthLock } from './auth-lock/auth-lock';

export const PASSPORT_ROUTES: Routes = [
  {
    path: '',
    children: [
      {
        path: 'lock',
        component: AuthLock,
        title: '锁屏',
      },
      {
        path: 'login',
        component: Login,
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
