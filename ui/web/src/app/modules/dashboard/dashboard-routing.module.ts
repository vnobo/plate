import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {IndexComponent} from "./index/index.component";
import {authGuard} from "../../security/auth.service";

const routes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {path: 'index', component: IndexComponent},
      {path: '', component: IndexComponent, pathMatch: 'full'}
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [RouterModule]
})
export class DashboardRoutingModule {
}
