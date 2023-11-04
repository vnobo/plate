import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PageNotFoundComponent} from "./page-not-found/page-not-found.component";

const routes: Routes = [
  {path: 'welcome', loadChildren: () => import('./pages.module').then(m => m.PagesModule)},
  {
    path: 'auth', loadChildren: () => import('./security/security.module').then(m => m.SecurityModule),
    title: "系统登录"
  },
  {path: '', pathMatch: 'full', redirectTo: '/auth'},
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PagesRoutingModule {
}
