import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PageNotFoundComponent} from "./shared/page-not-found/page-not-found.component";

const routes: Routes = [
  {
    path: 'system',
    loadChildren: () => import('./modules/system/system.module').then(m => m.SystemModule)
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule)
  },
  {
    path: 'auth',
    loadChildren: () => import('./security/security.module').then(m => m.SecurityModule)
  },
  {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
