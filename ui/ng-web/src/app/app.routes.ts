import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'passport',
    loadChildren: () => import('@app/pages').then((m) => m.PASSPORT_ROUTES),
  },
  {
    path: '',
    redirectTo: 'passport',
    pathMatch: 'full',
  },
  { path: '**', redirectTo: '/passport' },
];
