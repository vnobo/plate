import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { PageNotFoundComponent } from './pages/page-not-found/page-not-found.component';
import { authGuard } from './core/auth.service';

export const routes: Routes = [
  {
    path: 'welcome',
    title: '其他系统',
    canActivate: [authGuard],
    loadChildren: () => import('./pages/welcome/welcome.routes').then(m => m.WELCOME_ROUTES),
  },
  {
    path: 'home',
    title: '业务系统',
    canActivate: [authGuard],
    loadChildren: () => import('./pages/home/home.routes').then(m => m.HOME_ROUTES),
  },
  { path: 'login', component: LoginComponent, title: '登录系统' },
  { path: '', pathMatch: 'full', redirectTo: '/login' },
  { path: '**', component: PageNotFoundComponent },
];
