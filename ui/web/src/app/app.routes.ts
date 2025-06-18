import {Routes} from '@angular/router';

export const routes: Routes = [
  {
    path: 'home',
    loadChildren: () => import('./pages/index').then(m => m.HOME_ROUTES),
  },
  {
    path: 'passport',
    loadChildren: () => import('./pages/index').then(m => m.PASSPORT_ROUTES),
  },
  { path: 'exception', loadChildren: () => import('./pages/index').then(m => m.EXCEPTION_ROUTES) },
  { path: '', pathMatch: 'full', redirectTo: '/passport' },
  { path: '**', redirectTo: 'exception/404' },
];
