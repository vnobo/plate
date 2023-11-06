import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PageNotFoundComponent} from "./pages/page-not-found/page-not-found.component";

const routes: Routes = [
  {
    path: 'index', loadChildren: () => import('./pages/pages.module').then(m => m.PagesModule),
    title: '主页'
  },
  {
    path: 'auth', loadChildren: () => import('./pages/security/security.module').then(m => m.SecurityModule),
    title: '认证'
  },
  {path: '', pathMatch: 'full', redirectTo: '/auth'},
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
