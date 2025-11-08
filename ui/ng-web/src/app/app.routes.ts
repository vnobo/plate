import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'passport',
    loadChildren: () => import('@app/pages').then((m) => m.PASSPORT_ROUTES),
  },
  {
    path: 'dashboard',
    loadChildren: () => import('@app/pages').then((m) => m.DASHBOARD_ROUTES),
  },
  {
    path: 'examples',
    loadChildren: () => import('@app/pages').then((m) => m.EXAMPLES_ROUTES),
  },
  {
    path: 'error',
    loadChildren: () => import('@app/pages').then((m) => m.EXCEPTION_ROUTES),
  },
  {
    path: '',
    redirectTo: '/passport',
    pathMatch: 'full',
  },
  { path: '**', redirectTo: '/error' },
];
