import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {authGuard} from "../../security/auth.service";
import {MenusComponent} from "./menus/menus.component";
import {UsersComponent} from "./users/users.component";

const routes: Routes = [{
  path: '',
  canActivate: [authGuard],
  children: [
    {path: 'menus', component: MenusComponent},
    {path: 'users', component: UsersComponent},
    {path: '', component: MenusComponent, pathMatch: 'full'}
  ]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SystemRoutingModule {
}
