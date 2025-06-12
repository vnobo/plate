import {Routes} from '@angular/router';

export const routes: Routes = [
  {
    path: 'passport',
    loadChildren: () => import('./pages/index').then(m => m.PASSPORT_ROUTES),
    data: { title: '管理平台' },
  },
  { path: 'exception', loadChildren: () => import('./pages/index').then(m => m.EXCEPTION_ROUTES) },
  { path: '', pathMatch: 'full', redirectTo: '/passport' },
  { path: '**', redirectTo: 'exception/404' },
];
