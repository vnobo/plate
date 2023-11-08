import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {IndexComponent} from "./index/index.component";
import {HomeComponent} from "./home/home.component";
import {authGuard} from "../core/auth.service";

const routes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    component: HomeComponent, title: '主页',
    children: [
      {
        path: 'system',
        canActivateChild: [authGuard],
        loadChildren: () => import('./system/system.module').then(m => m.SystemModule),
        title: '系统管理',
      },
      {
        path: '',
        canActivateChild: [authGuard],
        component: IndexComponent, title: '首页'
      }
    ]
  },

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PagesRoutingModule {
}
