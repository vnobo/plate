import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {IndexComponent} from "./index/index.component";

const routes: Routes = [
  {path: 'index', component: IndexComponent, title: '欢迎主页'},
  {path: '', pathMatch: 'full', redirectTo: '/index'}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PagesRoutingModule {
}
