import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {MenusComponent} from "./menus/menus.component";
import {authGuard} from "../../core/auth.service";

const routes: Routes = [
  {
    path: 'menus',
    canActivate: [authGuard],
    component: MenusComponent,
    title: '菜单管理'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SystemRoutingModule {
}
