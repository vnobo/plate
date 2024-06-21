import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {LoginV1Component} from "./loginv1/login.component";

const routes: Routes = [
  {path: 'login', component: LoginComponent, title: '登录'},
  {path: 'login1', component: LoginV1Component, title: '登录V1'},
  {path: '', redirectTo: 'login', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SecurityRoutingModule {
}
