import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { MenusComponent } from './menus/menus.component';
import { authGuard } from '../../core/auth.service';

export const HOME_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    title: '管理后台',
    component: HomeComponent,
    children: [
      {
        path: 'menus',
        title: '菜单管理',
        component: MenusComponent,
      },
      {
        path: '',
        title: '菜单管理',
        component: MenusComponent,
      },
    ],
  },
];
