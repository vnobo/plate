import {Routes} from '@angular/router';
import {PageNotFoundComponent} from '../pages/page-not-found/page-not-found.component';

export const routes: Routes = [
  {
    path: 'home',
    loadChildren: () =>
      import('../pages/pages.module').then(m => m.PagesModule),
  },
  {
    path: 'auth',
    loadChildren: () =>
      import('../core/security/security.module').then(
        m => m.SecurityModule
      ),
  },
  {path: '', pathMatch: 'full', redirectTo: 'auth'},
  {path: '**', component: PageNotFoundComponent},
];
