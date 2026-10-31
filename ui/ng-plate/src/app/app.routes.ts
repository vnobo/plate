import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'platform',
    loadChildren: () => import('./pages/platform/platform').then((m) => m.PLATFORM_ROUTES),
  },
  {
    path: 'passport',
    loadChildren: () => import('./pages/passport/passport').then((m) => m.PASSPORT_ROUTES),
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./pages/dashboard/dashboard').then((m) => m.DASHBOARD_ROUTES),
  },
  {
    path: 'examples',
    loadChildren: () => import('./pages/examples/examples').then((m) => m.EXAMPLES_ROUTES),
  },
  {
    path: 'error',
    loadChildren: () => import('./pages/error/error').then((m) => m.EXCEPTION_ROUTES),
  },
  {
    path: '',
    redirectTo: '/passport',
    pathMatch: 'full',
  },
  { path: '**', redirectTo: '/error' },
];
