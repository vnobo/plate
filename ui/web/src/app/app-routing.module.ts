import {NgModule} from '@angular/core';
import {RouterModule, Routes, TitleStrategy} from '@angular/router';
import {PageNotFoundComponent} from "./shared/page-not-found/page-not-found.component";
import {PageTitleStrategy} from "./shared/page-title-strategy.service";

const routes: Routes = [
  {path: 'welcome', loadChildren: () => import('./pages/welcome/welcome.module').then(m => m.WelcomeModule)},
  {
    path: 'auth', loadChildren: () => import('./security/security.module').then(m => m.SecurityModule),
    title: "系统登录"
  },
  {path: '', pathMatch: 'full', redirectTo: '/auth'},
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [
    {provide: TitleStrategy, useClass: PageTitleStrategy},
  ]
})
export class AppRoutingModule {
}
