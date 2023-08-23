import {NgModule} from '@angular/core';
import {RouterModule, Routes, TitleStrategy} from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {PageTitleStrategy} from "../shared/page-title-strategy.service";

const routes: Routes = [
  {path: 'login', component: LoginComponent, title: "系统登录"},
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
