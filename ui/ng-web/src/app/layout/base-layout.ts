import { afterNextRender, Component, inject, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TokenService } from '@app/core';
import { UserDetails } from '@plate/types';
import { LayoutHeader } from './layout-header';

@Component({
  selector: 'app-layout-base',
  imports: [RouterModule, LayoutHeader],
  template: `
    <div class="page">
      <app-layout-header></app-layout-header>
      <div class="page-wrapper">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
})
export class BaseLayout {}
