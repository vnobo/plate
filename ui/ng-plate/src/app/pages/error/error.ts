import { Routes } from '@angular/router';

export const EXCEPTION_ROUTES: Routes = [
  { path: '403', loadComponent: () => import('./error-page').then((m) => m.ErrorPage), data: { type: 403 } },
  { path: '404', loadComponent: () => import('./error-page').then((m) => m.ErrorPage), data: { type: 404 } },
  { path: '500', loadComponent: () => import('./error-page').then((m) => m.ErrorPage), data: { type: 500 } },
  { path: 'trigger', loadComponent: () => import('./error-page').then((m) => m.ErrorPage), data: { type: 512 } },
  {
    path: '',
    redirectTo: '404',
    pathMatch: 'full',
  },
];
