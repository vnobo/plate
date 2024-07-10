import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsersComponent } from './users/users.component';
import { MenusComponent } from './menus/menus.component';
import { authGuard } from '../../../core/auth.service';
import { GroupsComponent } from './groups/groups.component';

const routes: Routes = [
  {
    path: 'users',
    component: UsersComponent,
    title: '用户管理',
    canActivate: [authGuard],
  },
  {
    path: 'menus',
    component: MenusComponent,
    title: '菜单管理',
    canActivate: [authGuard],
  },
  {
    path: 'roles',
    component: GroupsComponent,
    title: '角色管理',
    canActivate: [authGuard],
  },
  { path: '', pathMatch: 'full', redirectTo: 'menus' },
  { path: '**', component: MenusComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SystemRoutingModule {}
