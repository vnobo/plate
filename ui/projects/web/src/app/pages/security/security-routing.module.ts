import {NgModule} from '@angular/core';
import {RouterModule, Routes, TitleStrategy} from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {PageTitleStrategy} from "../../core/title-strategy.service";
import {LoginV1Component} from "./loginv1/login.component";

const routes: Routes = [
  {path: 'login', component: LoginComponent, title: "系统登录"},
  {path: 'login1', component: LoginV1Component, title: "系统登录"},
  {path: '', component: LoginComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [
    {provide: TitleStrategy, useClass: PageTitleStrategy},
  ]
})
export class SecurityRoutingModule {
}
