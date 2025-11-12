import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { LayoutHeader } from './layout-header';
import { LayoutAside } from './layout-aside';

@Component({
  selector: 'app-layout-base',
  imports: [RouterModule, LayoutHeader, LayoutAside],
  template: `
    <div class="page">
      <app-layout-aside></app-layout-aside>
      <app-layout-header></app-layout-header>
      <main class="page-wrapper">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
})
export class BaseLayout {}
