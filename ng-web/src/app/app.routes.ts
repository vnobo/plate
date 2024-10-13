import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { PageNotFoundComponent } from './pages/page-not-found/page-not-found.component';

export const routes: Routes = [
  {
    path: 'welcome',
    loadChildren: () => import('./pages/welcome/welcome.routes').then(m => m.WELCOME_ROUTES),
  },
  {
    path: 'home',
    loadChildren: () => import('./pages/home/home.routes').then(m => m.HOME_ROUTES),
  },
  { path: 'login', component: LoginComponent },
  { path: '', pathMatch: 'full', redirectTo: '/login' },
  { path: '**', component: PageNotFoundComponent },
];
