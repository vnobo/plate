import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IndexComponent } from './index/index.component';
import { authGuard } from '../../core/auth.service';

const routes: Routes = [
  {
    path: 'index',
    component: IndexComponent,
    title: '首页',
    canActivate: [authGuard],
  },
  { path: '', pathMatch: 'full', redirectTo: 'index' },
  { path: '**', component: IndexComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class HomeRoutingModule {}
