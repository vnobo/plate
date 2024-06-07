import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {MenusComponent} from "./menus/menus.component";
import {authGuard} from "../../core/auth.service";
import {GroupsComponent} from './groups/groups.component';
import {UsersComponent} from './users/users.component';

const routes: Routes = [
  {
    path: 'users',
    canActivate: [authGuard],
    component: UsersComponent,
    title: '用户管理',
  },
  {
    path: 'menus',
    canActivate: [authGuard],
    component: MenusComponent,
    title: '菜单管理',
  },
  {
    path: 'groups',
    canActivate: [authGuard],
    component: GroupsComponent,
    title: '角色管理',
  },
  {
    path: '',
    redirectTo: 'menus',
    pathMatch: 'full',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SystemRoutingModule {
}
