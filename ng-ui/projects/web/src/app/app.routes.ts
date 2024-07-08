import { Routes } from '@angular/router';
import { NotFoundComponent } from '../core/not-found.component';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('../core/security.module').then(m => m.SecurityModule),
  },
  {
    path: 'home',
    loadChildren: () => import('./home/home.module').then(m => m.HomeModule),
  },
  { path: '', pathMatch: 'full', redirectTo: 'auth' },
  { path: '**', component: NotFoundComponent },
];
