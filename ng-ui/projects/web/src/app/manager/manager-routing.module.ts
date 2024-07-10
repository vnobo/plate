import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ManagerComponent } from './manager.component';
import { authGuard } from '../../core/auth.service';

const routes: Routes = [
  {
    path: 'system',
    component: ManagerComponent,
    title: '管理',
    canActivate: [authGuard],
    loadChildren: () => import('./system/system.module').then(m => m.SystemModule),
  },
  { path: '', pathMatch: 'full', redirectTo: 'system' },
  { path: '**', component: ManagerComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ManagerRoutingModule {}
