import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {WelcomeComponent} from "./welcome/welcome.component";
import {PageNotFoundComponent} from "./page-not-found/page-not-found.component";

const routes: Routes = [
  {path: 'welcome', component: WelcomeComponent, title: '欢迎主页'},
  {
    path: 'auth', loadChildren: () => import('./security/security.module').then(m => m.SecurityModule),
    title: '系统登录'
  },
  {path: '', pathMatch: 'full', redirectTo: '/auth'},
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class PagesRoutingModule {
}
