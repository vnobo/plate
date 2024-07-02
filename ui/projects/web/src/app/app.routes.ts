import {Routes} from '@angular/router';
import {NotFoundComponent} from './not-found.component';

export const routes: Routes = [
  {
    path: 'home',
    loadChildren: () =>
      import('../pages/pages.module').then(m => m.PagesModule),
  },
  {
    path: 'auth',
    loadChildren: () =>
      import('../core/security.module').then(m => m.SecurityModule),
  },
  {path: '', pathMatch: 'full', redirectTo: 'auth'},
  {path: '**', component: NotFoundComponent},
];
