import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { MenusComponent } from './menus/menus.component';
import { authGuard } from '../../core/auth.service';

export const HOME_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    component: HomeComponent,
    data: {
      title: '管理后台',
    },
    children: [
      {
        path: 'menus',
        data: {
          title: '菜单管理',
        },
        component: MenusComponent,
      },
      {
        path: '',
        data: {
          title: '菜单管理',
        },
        component: MenusComponent,
      },
    ],
  },
];
