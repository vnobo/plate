import { Routes } from '@angular/router';
import { LoginComponent, PageNotFoundComponent } from './pages';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    data: { title: '登录页面' },
  },
  { path: '', pathMatch: 'full', redirectTo: '/login' },
  { path: '**', component: PageNotFoundComponent },
];
