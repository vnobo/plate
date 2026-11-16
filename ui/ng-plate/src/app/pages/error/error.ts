import { Routes } from '@angular/router';

export const EXCEPTION_ROUTES: Routes = [
  {
    path: '403',
    loadComponent: () => import('./error-page').then((m) => m.ErrorPage),
    title: $localize`:@@route.error.403:403 · 无权访问`,
    data: { type: 403 },
  },
  {
    path: '404',
    loadComponent: () => import('./error-page').then((m) => m.ErrorPage),
    title: $localize`:@@route.error.404:404 · 页面不存在`,
    data: { type: 404 },
  },
  {
    path: '500',
    loadComponent: () => import('./error-page').then((m) => m.ErrorPage),
    title: $localize`:@@route.error.500:500 · 服务器错误`,
    data: { type: 500 },
  },
  {
    path: 'trigger',
    loadComponent: () => import('./error-page').then((m) => m.ErrorPage),
    title: $localize`:@@route.error.512:请求被阻止`,
    data: { type: 512 },
  },
  {
    path: '',
    redirectTo: '404',
    pathMatch: 'full',
  },
];
