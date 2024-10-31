import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { MenusComponent } from './menus/menus.component';
import { UsersComponent } from './users/users.component';
import { GroupsComponent } from './groups/groups.component';
import { authGuard } from '../../core/pages.guard';

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
        path: 'roles',
        data: {
          title: '角色管理',
        },
        component: GroupsComponent,
      },
      {
        path: 'users',
        data: {
          title: '用户管理',
        },
        component: UsersComponent,
      },
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
