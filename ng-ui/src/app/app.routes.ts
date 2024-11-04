import { Routes } from '@angular/router';
import { LoginComponent } from './pages';

export const routes: Routes = [
  {
    path: 'welcome',
    loadChildren: () => import('./pages/index').then(m => m.WELCOME_ROUTES),
    data: { title: '欢迎主页' },
  },
  {
    path: 'home',
    loadChildren: () => import('./pages/index').then(m => m.HOME_ROUTES),
    data: { title: '管理平台' },
  },
  {
    path: 'passport',
    loadChildren: () => import('./pages/index').then(m => m.PASSPORT_ROUTES),
    data: { title: '管理平台' },
  },
  { path: '', pathMatch: 'full', redirectTo: '/passport' },
  { path: 'exception', loadChildren: () => import('./pages/index').then(m => m.EXCEPTION_ROUTES) },
  { path: '**', redirectTo: 'exception/404' },
];
