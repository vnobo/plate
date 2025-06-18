import {Routes} from '@angular/router';
import {MenusComponent} from './menus/menus';
import {UsersComponent} from './users/users';
import {GroupsComponent} from './groups/groups';
import {authGuard} from '@app/core/pages.guard';
import {Base} from '@app/layout';

export const HOME_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    component: Base,
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
        pathMatch: 'full',
        redirectTo: 'users',
      },
    ],
  },
];
