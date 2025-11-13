import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  OnDestroy,
} from '@angular/core';
import { RouterModule } from '@angular/router';
import { LayoutAside } from './layout-aside';
import { LayoutHeader } from './layout-header';

@Component({
  selector: 'app-layout-base',
  imports: [RouterModule, LayoutHeader, LayoutAside],
  template: `
    <app-layout-header></app-layout-header>
    <app-layout-aside></app-layout-aside>
    <div class="page-wrapper">
      <div class="container-fluid">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: contents;
      }
    `,
  ],
  host: {
    width: '100%',
    height: '100%',
    class: 'page',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BaseLayout implements OnDestroy {
  constructor() {
    afterNextRender(() => {
      document.body.classList.add('layout-fluid');
    });
  }
  ngOnDestroy(): void {
    document.body.classList.remove('layout-fluid');
  }
}
