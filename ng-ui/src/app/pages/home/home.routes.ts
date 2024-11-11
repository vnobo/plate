import { Routes } from '@angular/router';
import { MenusComponent } from './menus/menus.component';
import { UsersComponent } from './users/users.component';
import { GroupsComponent } from './groups/groups.component';
import { authGuard } from '@app/core/pages.guard';
import { BasicLayoutComponent } from '@app/layout';

export const HOME_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    component: BasicLayoutComponent,
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
